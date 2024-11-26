package net.blay09.mods.cookingforblockheads.menu.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class SlotOvenTool extends Slot {

    private static final ResourceLocation[] ovenToolIcons = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("container/slot/bakeware"),
            ResourceLocation.withDefaultNamespace("container/slot/pot"),
            ResourceLocation.withDefaultNamespace("container/slot/saucepan"),
            ResourceLocation.withDefaultNamespace("container/slot/skillet")
    };

    private final int iconIndex;

    public SlotOvenTool(Container container, int id, int x, int y, int iconIndex) {
        super(container, id, x, y);
        this.iconIndex = iconIndex;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Nullable
    @Override
    public ResourceLocation getNoItemIcon() {
        return ovenToolIcons[iconIndex];
    }
}

