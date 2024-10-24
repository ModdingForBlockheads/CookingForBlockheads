package net.blay09.mods.cookingforblockheads.menu.slot;

import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CraftableListingFakeSlot extends AbstractFakeSlot {

    private CraftableWithStatus craftable;

    public CraftableListingFakeSlot(Container container, int slotId, int x, int y) {
        super(container, slotId, x, y);
    }

    @Override
    public ItemStack getItem() {
        return craftable != null ? craftable.itemStack() : ItemStack.EMPTY;
    }

    @Override
    public boolean hasItem() {
        return craftable != null;
    }

    @Override
    public boolean isActive() {
        return craftable != null;
    }

    public void setCraftable(@Nullable CraftableWithStatus craftable) {
        this.craftable = craftable;
    }

    @Nullable
    public CraftableWithStatus getCraftable() {
        return craftable;
    }

    @Override
    public boolean isFake() {
        return true;
    }
}

