package net.blay09.mods.cookingforblockheads.client.gui.screen;

import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.blay09.mods.cookingforblockheads.menu.SpiceRackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SpiceRackScreen extends AbstractContainerScreen<SpiceRackMenu> {

    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "textures/gui/spice_rack.png");

    public SpiceRackScreen(SpiceRackMenu container, Inventory playerInventory, Component displayName) {
        super(container, playerInventory, displayName);
        imageHeight = 132;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(RenderType::guiTextured, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

}
