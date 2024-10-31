package net.blay09.mods.cookingforblockheads.menu;

import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CuttingBoardMenu extends AbstractCraftingMenu {
    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    public static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_END = 10;
    private static final int INV_SLOT_START = 10;
    private static final int INV_SLOT_END = 37;
    private static final int USE_ROW_SLOT_START = 37;
    private static final int USE_ROW_SLOT_END = 46;

    private final ContainerLevelAccess access;
    private final Player player;
    private boolean placingRecipe;

    public CuttingBoardMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.cuttingBoard.get(), containerId, CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT);
        this.access = access;
        this.player = inventory.player;
        addResultSlot(player, 124, 35);
        addCraftingGridSlots(30, 17);
        addStandardInventorySlots(inventory, 8, 84);
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu menu, ServerLevel level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, @Nullable RecipeHolder<CraftingRecipe> recipeHolder) {
        final var craftInput = craftingContainer.asCraftInput();
        final var serverPlayer = (ServerPlayer) player;
        var resultStack = ItemStack.EMPTY;
        final var foundRecipe = level.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftInput, level, recipeHolder);
        if (foundRecipe.isPresent()) {
            final var foundRecipeHolder = foundRecipe.get();
            final var craftingRecipe = foundRecipeHolder.value();
            if (resultContainer.setRecipeUsed(serverPlayer, foundRecipeHolder)) {
                final var assembledStack = craftingRecipe.assemble(craftInput, level.registryAccess());
                if (assembledStack.isItemEnabled(level.enabledFeatures())) {
                    resultStack = assembledStack;
                }
            }
        }

        resultContainer.setItem(0, resultStack);
        menu.setRemoteSlot(RESULT_SLOT, resultStack);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), RESULT_SLOT, resultStack));
    }

    public void slotsChanged(Container container) {
        if (!placingRecipe) {
            access.execute((level, pos) -> {
                if (level instanceof ServerLevel serverlevel) {
                    slotChangedCraftingGrid(this, serverlevel, player, craftSlots, resultSlots, null);
                }
            });
        }
    }

    public void beginPlacingRecipe() {
        this.placingRecipe = true;
    }

    public void finishPlacingRecipe(ServerLevel level, RecipeHolder<CraftingRecipe> recipeHolder) {
        this.placingRecipe = false;
        slotChangedCraftingGrid(this, level, player, craftSlots, resultSlots, recipeHolder);
    }

    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> clearContainer(player, craftSlots));
    }

    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.cuttingBoard);
    }

    public ItemStack quickMoveStack(Player player, int index) {
        var itemstack = ItemStack.EMPTY;
        final var slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            final var slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index == RESULT_SLOT) {
                access.execute((level, pos) -> slotStack.getItem().onCraftedBy(slotStack, level, player));
                if (!moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(slotStack, itemstack);
            } else if (index >= INV_SLOT_START && index < USE_ROW_SLOT_END) {
                if (!moveItemStackTo(slotStack, CRAFT_SLOT_START, CRAFT_SLOT_END, false)) {
                    if (index < INV_SLOT_END) {
                        if (!moveItemStackTo(slotStack, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(slotStack, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
            if (index == RESULT_SLOT) {
                player.drop(slotStack, false);
            }
        }

        return itemstack;
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    public Slot getResultSlot() {
        //noinspection SequencedCollectionMethodCanBeUsed
        return this.slots.get(RESULT_SLOT);
    }

    public List<Slot> getInputGridSlots() {
        return this.slots.subList(CRAFT_SLOT_START, CRAFT_SLOT_END);
    }

    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    protected Player owner() {
        return this.player;
    }
}
