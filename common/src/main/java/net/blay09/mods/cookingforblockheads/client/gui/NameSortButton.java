package net.blay09.mods.cookingforblockheads.client.gui;

import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.blay09.mods.cookingforblockheads.api.ISortButton;
import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;
import net.blay09.mods.cookingforblockheads.menu.comparator.ComparatorName;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

public class NameSortButton implements ISortButton {

    private static final ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "textures/gui/gui.png");

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip.cookingforblockheads.sort_by_name");
    }

    @Override
    public Comparator<CraftableWithStatus> getComparator(Player player) {
        return new ComparatorName();
    }

    @Override
    public int getIconTextureX() {
        return 196;
    }

    @Override
    public int getIconTextureY() {
        return 0;
    }

}
