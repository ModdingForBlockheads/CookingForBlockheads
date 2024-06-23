package net.blay09.mods.cookingforblockheads.menu;

import net.blay09.mods.cookingforblockheads.block.entity.CounterBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CounterMenu extends AbstractContainerMenu implements IContainerWithDoor {

    private final CounterBlockEntity tileCounter;
    private final int numRows;

    public CounterMenu(int windowId, Inventory playerInventory, CounterBlockEntity counter) {
        super(ModMenus.counter.get(), windowId);
        this.tileCounter = counter;
        this.numRows = counter.getContainer().getContainerSize() / 9;
        int playerInventoryStart = numRows * 18;

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(counter.getContainer(), j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 31 + i * 18 + playerInventoryStart));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 89 + playerInventoryStart));
        }

        counter.getDoorAnimator().openContainer(playerInventory.player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        tileCounter.getDoorAnimator().closeContainer(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (slotIndex < numRows * 9) {
                if (!this.moveItemStackTo(slotStack, numRows * 9, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, numRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(tileCounter, player);
    }

    @Override
    public boolean isTileEntity(BlockEntity tileEntity) {
        return tileCounter == tileEntity;
    }

    public int getNumRows() {
        return numRows;
    }
}
