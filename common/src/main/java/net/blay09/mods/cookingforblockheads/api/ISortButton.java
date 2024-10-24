package net.blay09.mods.cookingforblockheads.api;

import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

public interface ISortButton {
    ResourceLocation getIcon();

    Component getTooltip();

    Comparator<CraftableWithStatus> getComparator(Player player);

    int getIconTextureX();

    int getIconTextureY();
}
