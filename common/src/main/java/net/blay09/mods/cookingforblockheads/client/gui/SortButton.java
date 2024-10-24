package net.blay09.mods.cookingforblockheads.client.gui;

import net.blay09.mods.cookingforblockheads.api.ISortButton;
import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

public class SortButton extends Button {

    private final ISortButton button;

    public SortButton(int x, int y, ISortButton button, OnPress onPress) {
        super(x, y, 20, 20, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.button = button;
        setTooltip(Tooltip.create(this.button.getTooltip()));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        int texY = button.getIconTextureY();
        if (!active) {
            texY += 40;
        } else if (isHovered) {
            texY += 20;
        }

        guiGraphics.blit(RenderType::guiTextured, button.getIcon(), getX(), getY(), button.getIconTextureX(), texY, width, height, 256, 256);
    }

    public Comparator<CraftableWithStatus> getComparator(Player player) {
        return button.getComparator(player);
    }
}
