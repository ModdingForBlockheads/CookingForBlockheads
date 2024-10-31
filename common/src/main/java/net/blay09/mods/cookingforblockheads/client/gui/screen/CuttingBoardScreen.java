package net.blay09.mods.cookingforblockheads.client.gui.screen;

import net.blay09.mods.cookingforblockheads.menu.CuttingBoardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CuttingBoardScreen extends AbstractRecipeBookScreen<CuttingBoardMenu> {

    private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    public CuttingBoardScreen(CuttingBoardMenu menu, Inventory inventory, Component component) {
        super(menu, new CraftingRecipeBookComponent(menu), inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 29;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int x = leftPos;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(RenderType::guiTextured, CRAFTING_TABLE_LOCATION, x, y, 0f, 0f, imageWidth, imageHeight, 256, 256);
    }
}
