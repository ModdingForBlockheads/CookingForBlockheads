package net.blay09.mods.cookingforblockheads.menu;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.DefaultContainer;
import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.blay09.mods.cookingforblockheads.api.CookingForBlockheadsAPI;
import net.blay09.mods.cookingforblockheads.api.Kitchen;
import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;
import net.blay09.mods.cookingforblockheads.crafting.CraftingContext;
import net.blay09.mods.cookingforblockheads.crafting.KitchenImpl;
import net.blay09.mods.cookingforblockheads.crafting.RecipeWithStatus;
import net.blay09.mods.cookingforblockheads.menu.comparator.ComparatorName;
import net.blay09.mods.cookingforblockheads.menu.slot.CraftMatrixFakeSlot;
import net.blay09.mods.cookingforblockheads.menu.slot.CraftableListingFakeSlot;
import net.blay09.mods.cookingforblockheads.network.message.*;
import net.blay09.mods.cookingforblockheads.registry.CookingForBlockheadsRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.display.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KitchenMenu extends AbstractContainerMenu {

    public final Player player;
    private final KitchenImpl kitchen;

    private final List<CraftableListingFakeSlot> recipeListingSlots = new ArrayList<>();
    private final List<CraftMatrixFakeSlot> matrixSlots = new ArrayList<>();

    private final NonNullList<ItemStack> lockedInputs = NonNullList.withSize(9, ItemStack.EMPTY);

    private final List<CraftableWithStatus> filteredCraftables = new ArrayList<>();

    private String currentSearch;
    private Comparator<CraftableWithStatus> currentSorting = new ComparatorName();

    private List<CraftableWithStatus> craftables = new ArrayList<>();

    private boolean craftablesDirty = true;
    private boolean recipesDirty = true;
    private boolean scrollOffsetDirty;
    private int scrollOffset;

    private CraftableWithStatus selectedCraftable;
    private List<RecipeWithStatus> recipesForSelection;
    private int recipesForSelectionIndex;

    public KitchenMenu(MenuType<KitchenMenu> containerType, int windowId, Player player, KitchenImpl kitchen) {
        super(containerType, windowId);

        this.player = player;
        this.kitchen = kitchen;

        final var fakeInventory = new DefaultContainer(4 * 3 + 3 * 3);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                final var slot = new CraftableListingFakeSlot(fakeInventory, j + i * 3, 102 + j * 18, 11 + i * 18);
                recipeListingSlots.add(slot);
                addSlot(slot);
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final var slot = new CraftMatrixFakeSlot(this, fakeInventory, j + i * 3, 24 + j * 18, 20 + i * 18);
                matrixSlots.add(slot);
                addSlot(slot);
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(player.getInventory(), j + i * 9 + 9, 8 + j * 18, 92 + i * 18) {
                    @Override
                    public void setChanged() {
                        craftablesDirty = true;
                        recipesDirty = true;
                    }
                });
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(player.getInventory(), i, 8 + i * 18, 150) {
                @Override
                public void setChanged() {
                    craftablesDirty = true;
                    recipesDirty = true;
                }
            });
        }
    }

    @Override
    public void clicked(int slotNumber, int dragType, ClickType clickType, Player player) {
        var handled = false;
        if (slotNumber >= 0 && slotNumber < slots.size()) {
            Slot slot = slots.get(slotNumber);
            if (slot instanceof CraftableListingFakeSlot craftableSlot) {
                if (player.level().isClientSide) {
                    if (isSelectedSlot(craftableSlot)) {
                        if (clickType == ClickType.PICKUP || clickType == ClickType.PICKUP_ALL || clickType == ClickType.QUICK_MOVE || clickType == ClickType.CLONE) {
                            requestCraft(clickType == ClickType.QUICK_MOVE, clickType == ClickType.CLONE);
                            handled = true;
                        }
                    } else {
                        selectCraftable(craftableSlot.getCraftable());
                        handled = true;
                    }
                }
            }
        }

        if (!handled) {
            super.clicked(slotNumber, dragType, clickType, player);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (craftablesDirty) {
            broadcastAvailableRecipes();
            craftablesDirty = false;
        }

        if (recipesDirty) {
            if (selectedCraftable != null) {
                broadcastRecipesForResultItem(selectedCraftable.itemStack());
            }
            recipesDirty = false;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void slotsChanged(Container inventory) {
        // NOP, we don't want detectAndSendChanges called here, otherwise it will spam on crafting a stack of items
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (slotIndex >= 48 && slotIndex < 57) {
                if (!moveItemStackTo(slotStack, 21, 48, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= 21 && slotIndex < 48) {
                if (!moveItemStackTo(slotStack, 48, 57, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    public void selectCraftable(@Nullable CraftableWithStatus recipe) {
        selectedCraftable = recipe;
        resetSelectedRecipe();
        updateCraftableSlots();

        if (recipe != null) {
            if (player.level().isClientSide) {
                lockedInputs.clear();
                requestSelectionRecipes(recipe);
            }
        } else {
            resetSelectedRecipe();
            updateMatrixSlots();
        }
    }

    public void resetSelectedRecipe() {
        recipesForSelection = null;
        recipesForSelectionIndex = 0;
        updateMatrixSlots();
    }

    public void requestCraftables() {
        Balm.getNetworking().sendToServer(new RequestAvailableCraftablesMessage());
    }

    public void handleRequestCraftables() {
        craftablesDirty = true;
    }

    public void requestSelectionRecipes(CraftableWithStatus craftable) {
        Balm.getNetworking().sendToServer(new RequestSelectionRecipesMessage(craftable.itemStack(), lockedInputs));
    }

    public void handleRequestSelectionRecipes(ItemStack resultItem, NonNullList<ItemStack> lockedInputs) {
        selectedCraftable = findCraftableForResultItem(resultItem);
        this.lockedInputs.clear();
        for (int i = 0; i < lockedInputs.size(); i++) {
            this.lockedInputs.set(i, lockedInputs.get(i));
        }

        recipesDirty = true;
    }

    private void requestCraft(boolean craftFullStack, boolean addToInventory) {
        final var selectedRecipe = getSelectedRecipe();
        if (selectedRecipe != null) {
            Balm.getNetworking().sendToServer(new CraftRecipeMessage(selectedRecipe.recipeDisplayEntry().id(), lockedInputs, craftFullStack, addToInventory));
        }
    }

    public List<CraftableWithStatus> getAvailableCraftables() {
        final var result = new HashMap<ResourceLocation, CraftableWithStatus>();
        final var context = new CraftingContext(kitchen, player);
        final var recipesByItemId = CookingForBlockheadsRegistry.getRecipesByItemId();
        for (ResourceLocation itemId : recipesByItemId.keySet()) {
            for (final var recipeHolder : recipesByItemId.get(itemId)) {
                final var craftableWithStatus = craftableWithStatusFromRecipe(context, recipeHolder);
                if (craftableWithStatus != null) {
                    result.compute(itemId, (k, v) -> CraftableWithStatus.best(v, craftableWithStatus));
                }
            }
        }
        return result.values().stream().toList();
    }

    private <C extends RecipeInput, T extends Recipe<C>> @Nullable CraftableWithStatus craftableWithStatusFromRecipe(CraftingContext context, RecipeHolder<?> recipeHolder) {
        final var recipe = recipeHolder.value();
        final var recipeHandler = CookingForBlockheadsAPI.getKitchenRecipeHandler(recipe);
        final var resultItem = recipeHandler.predictResultItem(recipeHolder);
        if (isGroupItem(resultItem)) {
            return null;
        }

        final var operation = context.createOperation(recipeHolder).prepare();
        if (!kitchen.isRecipeAvailable(operation)) {
            return null;
        }

        final var missingIngredients = operation.getMissingIngredients();
        final var missingUtensils = operation.getMissingIngredients();
        return new CraftableWithStatus(resultItem, !missingIngredients.isEmpty(), !missingUtensils.isEmpty());
    }

    private boolean isGroupItem(ItemStack resultItem) {
        final var itemId = Balm.getRegistries().getKey(resultItem.getItem());
        for (final var group : CookingForBlockheadsRegistry.getGroups()) {
            final var groupItemId = Balm.getRegistries().getKey(group.getParentItem());
            if (groupItemId.equals(itemId)) {
                continue;
            }

            for (final var ingredient : group.getChildren()) {
                if (ingredient.test(resultItem)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Collection<RecipeHolder<?>> getRecipesFor(ItemStack resultItem) {
        final var recipes = new ArrayList<>(CookingForBlockheadsRegistry.getRecipesFor(resultItem));
        recipes.addAll(CookingForBlockheadsRegistry.getRecipesInGroup(resultItem));
        return recipes;
    }

    public void broadcastAvailableRecipes() {
        craftables = getAvailableCraftables();
        Balm.getNetworking().sendTo(player, new AvailableCraftablesListMessage(craftables));
    }

    public void broadcastRecipesForResultItem(ItemStack resultItem) {
        final List<RecipeWithStatus> result = new ArrayList<>();
        final var recipeManager = player.getServer().getRecipeManager();

        final var context = new CraftingContext(kitchen, player);
        final var recipesForResult = getRecipesFor(resultItem);
        for (final var recipe : recipesForResult) {
            final var operation = context.createOperation(recipe).withLockedInputs(lockedInputs).prepare();
            recipeManager.listDisplaysForRecipe(recipe.id(), recipeDisplayEntry -> result.add(new RecipeWithStatus(recipeDisplayEntry,
                    operation.getMissingIngredients(),
                    operation.getMissingIngredientsMask(),
                    operation.getLockedInputs())));
        }

        this.recipesForSelection = result;
        Balm.getNetworking().sendTo(player, new SelectionRecipesListMessage(result));
    }

    public void craft(RecipeDisplayId recipeDisplayId, NonNullList<ItemStack> lockedInputs, boolean craftFullStack, boolean addToInventory) {
        final var level = player.level();
        final var serverDisplayInfo = level.getServer().getRecipeManager().getRecipeFromDisplay(recipeDisplayId);
        if (serverDisplayInfo == null) {
            CookingForBlockheads.logger.error("Received invalid recipe from client: {}", recipeDisplayId);
            return;
        }

        final var recipe = serverDisplayInfo.parent();
        if (!kitchen.canProcess(recipe.value().getType())) {
            CookingForBlockheads.logger.error("Received invalid craft request, unprocessable recipe {}", recipeDisplayId);
            return;
        }

        final var context = new CraftingContext(kitchen, player);
        final var operation = context.createOperation(recipe).withLockedInputs(lockedInputs);
        final var recipeHandler = CookingForBlockheadsAPI.getKitchenRecipeHandler(recipe.value());
        final var resultItem = recipeHandler.predictResultItem(recipe);
        final var repeats = craftFullStack ? resultItem.getMaxStackSize() / resultItem.getCount() : 1;
        for (int i = 0; i < repeats; i++) {
            operation.prepare();
            if (operation.canCraft()) {
                final var carried = getCarried();
                if (!carried.isEmpty() && (!ItemStack.isSameItemSameComponents(carried, resultItem) || carried.getCount() >= carried.getMaxStackSize())) {
                    if (craftFullStack || addToInventory) {
                        addToInventory = true;
                    } else {
                        break;
                    }
                }
                final var itemStack = operation.craft(this, player.level().registryAccess());
                if (!itemStack.isEmpty()) {
                    if (addToInventory) {
                        if (!player.getInventory().add(itemStack)) {
                            player.drop(itemStack, false);
                        }
                    } else {

                        if (carried.isEmpty()) {
                            setCarried(itemStack);
                        } else if (ItemStack.isSameItemSameComponents(carried, itemStack) && carried.getCount() < carried.getMaxStackSize()) {
                            carried.grow(itemStack.getCount());
                        } else {
                            if (!player.getInventory().add(itemStack)) {
                                player.drop(itemStack, false);
                            }
                        }
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        craftablesDirty = true;
        recipesDirty = true;
    }

    public void setCraftables(List<CraftableWithStatus> craftables) {
        int previousSelectionIndex = selectedCraftable != null ? filteredCraftables.indexOf(selectedCraftable) : -1;

        this.craftables = craftables;
        updateFilteredRecipes();

        // Make sure the previously selected recipe stays in the same slot, even if others moved
        if (previousSelectionIndex != -1) {
            final var it = filteredCraftables.iterator();
            CraftableWithStatus found = null;
            while (it.hasNext()) {
                final var recipe = it.next();
                if (ItemStack.isSameItemSameComponents(recipe.itemStack(), selectedCraftable.itemStack())) {
                    found = recipe;
                    it.remove();
                    break;
                }
            }
            while (previousSelectionIndex > filteredCraftables.size()) {
                filteredCraftables.add(null);
            }
            filteredCraftables.add(previousSelectionIndex, found);
            selectedCraftable = found;
        }

        // Updates the items inside the recipe slots
        updateCraftableSlots();

        setScrollOffsetDirty(true);
    }

    public void updateCraftableSlots() {
        int i = scrollOffset * 5;
        for (final var slot : recipeListingSlots) {
            if (i < filteredCraftables.size()) {
                final var craftable = filteredCraftables.get(i);
                slot.setCraftable(craftable);
                i++;
            } else {
                slot.setCraftable(null);
            }
        }
    }

    private void updateMatrixSlots() {
        final var selectedRecipe = getSelectedRecipe();
        if (selectedRecipe != null) {
            updateMatrixSlots(selectedRecipe);
        } else {
            for (int i = 0; i < matrixSlots.size(); i++) {
                CraftMatrixFakeSlot matrixSlot = matrixSlots.get(i);
                matrixSlot.setIngredient(i, null, ItemStack.EMPTY);
                matrixSlot.setMissing(true);
            }
        }
    }

    private void updateMatrixSlots(RecipeWithStatus recipe) {
        final var recipeDisplay = recipe.recipeDisplayEntry().display();
        final var matrix = NonNullList.<SlotDisplay>withSize(9, SlotDisplay.Empty.INSTANCE);
        final var missingMatrix = new boolean[9];
        final var ingredientIndexMatrix = new int[9];
        switch (recipeDisplay) {
            case ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay -> {
                final var ingredients = shapedCraftingRecipeDisplay.ingredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    final var ingredient = ingredients.get(i);
                    final int recipeWidth = shapedCraftingRecipeDisplay.width();
                    final int origX = i % recipeWidth;
                    final int origY = i / recipeWidth;
                    final int offsetX = recipeWidth == 1 ? 1 : 0;
                    int matrixSlot = origY * 3 + origX + offsetX;
                    matrix.set(matrixSlot, ingredient);
                    missingMatrix[matrixSlot] = (recipe.missingIngredientsMask() & (1 << i)) == (1 << i);
                    ingredientIndexMatrix[matrixSlot] = i;
                }
            }
            case ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay -> {
                final var ingredients = shapelessCraftingRecipeDisplay.ingredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    final var ingredient = ingredients.get(i);
                    matrix.set(i, ingredient);
                    missingMatrix[i] = (recipe.missingIngredientsMask() & (1 << i)) == (1 << i);
                    ingredientIndexMatrix[i] = i;
                }
            }
            case FurnaceRecipeDisplay furnaceRecipeDisplay -> {
                final var ingredient = furnaceRecipeDisplay.ingredient();
                final var matrixSlot = 4;
                matrix.set(matrixSlot, ingredient);
                missingMatrix[matrixSlot] = (recipe.missingIngredientsMask() & (1)) == (1);
                ingredientIndexMatrix[matrixSlot] = 0;
            }
            default -> {
            }
        }

        for (int i = 0; i < matrixSlots.size(); i++) {
            final var matrixSlot = matrixSlots.get(i);
            final var lockedInputs = recipe.lockedInputs();
            final int ingredientIndex = ingredientIndexMatrix[i];
            final var lockedInput = lockedInputs.get(ingredientIndex);
            matrixSlot.setIngredient(ingredientIndex, matrix.get(i), lockedInput);
            matrixSlot.setMissing(missingMatrix[i]);
        }
    }

    public void setSortComparator(Comparator<CraftableWithStatus> comparator) {
        this.currentSorting = comparator;
        // When re-sorting, make sure to remove all null slots that were added to preserve layout
        filteredCraftables.removeIf(Objects::isNull);
        filteredCraftables.sort(comparator);
        updateCraftableSlots();
    }

    public int getItemListCount() {
        return filteredCraftables.size();
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
        updateCraftableSlots();
    }

    public void search(@Nullable String term) {
        this.currentSearch = term;
        updateFilteredRecipes();
        setScrollOffset(0);
    }

    private void updateFilteredRecipes() {
        filteredCraftables.clear();
        for (final var craftable : craftables) {
            if (searchMatches(craftable.itemStack())) {
                filteredCraftables.add(craftable);
            }
        }
        filteredCraftables.sort(currentSorting);
    }

    private boolean searchMatches(ItemStack resultItem) {
        if (currentSearch == null || currentSearch.trim().isEmpty()) {
            return true;
        }

        final var lowerCaseSearch = currentSearch.toLowerCase();
        if (resultItem.getDisplayName().getString().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearch)) {
            return true;
        } else {
            final var tooltips = resultItem.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.Default.NORMAL);
            for (final var tooltip : tooltips) {
                if (tooltip.getString().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearch)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public RecipeWithStatus getSelectedRecipe() {
        return recipesForSelection != null ? recipesForSelection.get(recipesForSelectionIndex) : null;
    }

    public boolean isSelectedSlot(CraftableListingFakeSlot slot) {
        return selectedCraftable != null
                && slot.getCraftable() != null
                && ItemStack.isSameItemSameComponents(slot.getCraftable().itemStack(), selectedCraftable.itemStack());
    }

    public boolean isScrollOffsetDirty() {
        return scrollOffsetDirty;
    }

    public void setScrollOffsetDirty(boolean dirty) {
        scrollOffsetDirty = dirty;
    }

    public void setRecipesForSelection(List<RecipeWithStatus> recipes) {
        recipesForSelection = !recipes.isEmpty() ? recipes : null;
        recipesForSelectionIndex = recipesForSelection != null ? Math.max(0, Math.min(recipesForSelection.size() - 1, recipesForSelectionIndex)) : 0;

        updateMatrixSlots();
    }

    public void nextRecipe(int dir) {
        if (recipesForSelection != null) {
            recipesForSelectionIndex = Math.max(0, Math.min(recipesForSelection.size() - 1, recipesForSelectionIndex + dir));
            updateCraftableSlots();
        }

        updateMatrixSlots();
    }

    public boolean selectionHasRecipeVariants() {
        return recipesForSelection != null && recipesForSelection.size() > 1;
    }

    public boolean selectionHasPreviousRecipe() {
        return recipesForSelectionIndex > 0;
    }

    public boolean selectionHasNextRecipe() {
        return recipesForSelection != null && recipesForSelectionIndex < recipesForSelection.size() - 1;
    }

    public List<CraftMatrixFakeSlot> getMatrixSlots() {
        return matrixSlots;
    }

    @Nullable
    public CraftableWithStatus findCraftableForResultItem(ItemStack resultItem) {
        return craftables.stream().filter(it -> ItemStack.isSameItemSameComponents(it.itemStack(), resultItem)).findAny().orElse(null);
    }

    public Kitchen getKitchen() {
        return kitchen;
    }

    public void setLockedInput(int i, ItemStack lockedInput) {
        lockedInputs.set(i, lockedInput);
        if (selectedCraftable != null) {
            requestSelectionRecipes(selectedCraftable);
        }
    }

    public int getRecipesForSelectionIndex() {
        return filteredCraftables.indexOf(selectedCraftable);
    }
}
