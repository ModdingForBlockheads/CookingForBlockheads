package net.blay09.mods.cookingforblockheads.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.balm.api.energy.EnergyStorage;
import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.blay09.mods.cookingforblockheads.menu.OvenMenu;
import net.blay09.mods.cookingforblockheads.block.entity.OvenBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class OvenScreen extends AbstractContainerScreen<OvenMenu> {

    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "textures/gui/oven.png");

    public OvenScreen(OvenMenu container, Inventory playerInventory, Component displayName) {
        super(container, playerInventory, displayName);
        this.imageWidth += 22;
        this.imageHeight = 193;

        this.titleLabelX += 22;
        this.inventoryLabelX += 22;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);

        OvenBlockEntity tileEntity = menu.getOven();
        if (tileEntity.hasPowerUpgrade() && mouseX >= leftPos + imageWidth - 25 && mouseY >= topPos + 22 && mouseX < leftPos + imageWidth - 25 + 35 + 18 && mouseY < topPos + 22 + 72) {
            EnergyStorage energyStorage = tileEntity.getEnergyStorage();
            guiGraphics.renderTooltip(font, Component.translatable("tooltip.cookingforblockheads.energy_stored", energyStorage.getEnergy(), energyStorage.getCapacity()), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        final var oven = menu.getOven();
        for (int i = 0; i < 9; i++) {
            Slot slot = menu.slots.get(i + 7);
            if (slot.hasItem()) {
                ItemStack itemStack = oven.getSmeltingResult(slot.getItem());
                if (!itemStack.isEmpty()) {
                    final var pose = guiGraphics.pose();
                    pose.pushPose();
                    pose.translate(0f, 0f, 200f);
                    RenderSystem.setShaderColor(1f, 1f, 1f, oven.getCookProgress(i));
                    guiGraphics.renderItem(itemStack, slot.x, slot.y);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    pose.popPose();
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // Draw background
        guiGraphics.blit(RenderType::guiTextured, texture, leftPos + 22, topPos, 0, 0, imageWidth - 22, imageHeight, 256, 256);

        // Draw tool slots
        guiGraphics.blit(RenderType::guiTextured, texture, leftPos, topPos + 10, 176, 30, 25, 87, 256, 256);

        OvenBlockEntity tileEntity = menu.getOven();
        int offsetX = tileEntity.hasPowerUpgrade() ? -5 : 0;

        // Draw main slots
        guiGraphics.blit(RenderType::guiTextured, texture, leftPos + 22 + 61 + offsetX, topPos + 18, 176, 117, 76, 76, 256, 256);

        // Draw fuel slot
        guiGraphics.blit(RenderType::guiTextured, texture, leftPos + 22 + 38 + offsetX, topPos + 43, 205, 84, 18, 33, 256, 256);

        // Draw fuel bar
        if (tileEntity.isBurning()) {
            int burnTime = (int) (12 * tileEntity.getBurnTimeProgress());
            guiGraphics.blit(RenderType::guiTextured, texture, leftPos + 22 + 40 + offsetX, topPos + 43 + 12 - burnTime, 176, 12 - burnTime, 14, burnTime + 1, 256, 256);
        }

        // Draw power bar
        if (tileEntity.hasPowerUpgrade()) {
            guiGraphics.blit(RenderType::guiTextured, texture, leftPos + imageWidth - 25, topPos + 22, 205, 0, 18, 72, 256, 256);
            EnergyStorage energyStorage = tileEntity.getEnergyStorage();
            float energyPercentage = energyStorage.getEnergy() / (float) energyStorage.getCapacity();
            guiGraphics.blit(RenderType::guiTextured, texture, leftPos + imageWidth - 25 + 1, topPos + 22 + 1 + 70 - (int) (energyPercentage * 70), 223, 0, 16, (int) (energyPercentage * 70), 256, 256);
        }
    }

}
