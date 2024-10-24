package net.blay09.mods.cookingforblockheads.client.gui;

import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.blay09.mods.cookingforblockheads.api.ISortButton;
import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;
import net.blay09.mods.cookingforblockheads.menu.comparator.ComparatorSaturation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

public class SaturationSortButton implements ISortButton {

    private static final ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "textures/gui/gui.png");

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip.cookingforblockheads.sort_by_saturation");
    }

    @Override
    public Comparator<CraftableWithStatus> getComparator(Player player) {
        return new ComparatorSaturation(player);
    }

    @Override
    public int getIconTextureX() {
        return 236;
    }

    @Override
    public int getIconTextureY() {
        return 0;
    }

}
