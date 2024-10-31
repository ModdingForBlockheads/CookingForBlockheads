package net.blay09.mods.cookingforblockheads.registry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.BalmEvents;
import net.blay09.mods.balm.api.event.server.ServerReloadFinishedEvent;
import net.blay09.mods.balm.api.event.server.ServerStartedEvent;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.api.ISortButton;
import net.blay09.mods.cookingforblockheads.api.KitchenRecipeGroup;
import net.blay09.mods.cookingforblockheads.api.KitchenRecipeHandler;
import net.blay09.mods.cookingforblockheads.mixin.RecipeManagerAccessor;
import net.blay09.mods.cookingforblockheads.tag.ModItemTags;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.*;

public class CookingForBlockheadsRegistry {

    private static final Multimap<ResourceLocation, RecipeHolder<?>> recipesByItemId = ArrayListMultimap.create();
    private static final Multimap<ResourceLocation, RecipeHolder<?>> recipesByGroup = ArrayListMultimap.create();
    private static final List<ISortButton> sortButtons = new ArrayList<>();
    private static final Map<ItemStack, Integer> ovenFuelItems = new HashMap<>();
    private static final Map<Class<? extends Recipe<?>>, KitchenRecipeHandler<?, ?>> kitchenRecipeHandlers = new HashMap<>();

    public static void initialize(BalmEvents events) {
        events.onEvent(ServerReloadFinishedEvent.class,
                (ServerReloadFinishedEvent event) -> reload(event.getServer().getRecipeManager(), event.getServer().registryAccess()));
        events.onEvent(ServerStartedEvent.class, event -> reload(event.getServer().getRecipeManager(), event.getServer().registryAccess()));
    }

    private static void reload(RecipeManager recipeManager, RegistryAccess registryAccess) {
        recipesByItemId.clear();
        loadRecipesByType(recipeManager, registryAccess, RecipeType.CRAFTING);
        loadRecipesByType(recipeManager, registryAccess, RecipeType.SMELTING);
    }

    private static <C extends RecipeInput, T extends Recipe<C>> void loadRecipesByType(RecipeManager recipeManager, RegistryAccess registryAccess, RecipeType<T> recipeType) {
        final var recipeMap = ((RecipeManagerAccessor) recipeManager).getRecipes();
        for (final var recipeHolder : recipeMap.byType(recipeType)) {
            if (!isEligibleRecipe(recipeHolder)) {
                continue;
            }

            final var recipe = recipeHolder.value();
            final var recipeHandler = getKitchenRecipeHandler(recipe);
            if (recipeHandler == null) {
                continue;
            }

            final var resultItem = recipeHandler.predictResultItem(recipe);
            if (isEligibleResultItem(resultItem)) {
                final var itemId = Balm.getRegistries().getKey(resultItem.getItem());
                recipesByItemId.put(itemId, recipeHolder);

                final var groups = getGroups();
                for (final var group : groups) {
                    for (final var ingredient : group.getChildren()) {
                        if (ingredient.test(resultItem)) {
                            final var groupItemId = Balm.getRegistries().getKey(group.getParentItem());
                            recipesByGroup.put(groupItemId, recipeHolder);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static List<KitchenRecipeGroup> getGroups() {
        return List.of();
    }

    private static boolean isEligibleResultItem(ItemStack itemStack) {
        if (itemStack.is(ModItemTags.EXCLUDED)) {
            return false;
        }

        return itemStack.has(DataComponents.FOOD) || itemStack.is(ModItemTags.FOODS) || itemStack.is(ModItemTags.INGREDIENTS);
    }

    private static <T extends RecipeInput> boolean isEligibleRecipe(RecipeHolder<? extends Recipe<T>> recipe) {
        return !CookingForBlockheadsConfig.getActive().excludedRecipes.contains(recipe.id().location());
    }

    public static <C extends RecipeInput, T extends Recipe<C>> void registerKitchenRecipeHandler(Class<T> recipeType, KitchenRecipeHandler<C, T> handler) {
        kitchenRecipeHandlers.put(recipeType, handler);
    }

    @SuppressWarnings("unchecked")
    public static <C extends RecipeInput, T extends Recipe<C>, V extends KitchenRecipeHandler<C, T>> V getKitchenRecipeHandler(T recipe) {
        for (Class<? extends Recipe<?>> handlerClass : kitchenRecipeHandlers.keySet()) {
            if (handlerClass.isAssignableFrom(recipe.getClass())) {
                return (V) kitchenRecipeHandlers.get(handlerClass);
            }
        }

        return (V) kitchenRecipeHandlers.get(recipe.getClass());
    }

    public static void addSortButton(ISortButton nameSortButton) {
        sortButtons.add(nameSortButton);
    }

    public static Collection<ISortButton> getSortButtons() {
        return sortButtons;
    }

    public static void addOvenFuel(ItemStack fuelItem, int fuelTime) {
        ovenFuelItems.put(fuelItem, fuelTime);
    }

    public static int getOvenFuelTime(ItemStack itemStack) {
        for (final var entry : ovenFuelItems.entrySet()) {
            if (ItemStack.isSameItem(entry.getKey(), itemStack)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public static Collection<RecipeHolder<?>> getRecipesFor(ItemStack resultItem) {
        final var itemId = Balm.getRegistries().getKey(resultItem.getItem());
        return recipesByItemId.get(itemId);
    }

    public static Collection<? extends RecipeHolder<?>> getRecipesInGroup(ItemStack resultItem) {
        final var itemId = Balm.getRegistries().getKey(resultItem.getItem());
        return recipesByGroup.get(itemId);
    }

    public static Multimap<ResourceLocation, RecipeHolder<?>> getRecipesByItemId() {
        return recipesByItemId;
    }
}
