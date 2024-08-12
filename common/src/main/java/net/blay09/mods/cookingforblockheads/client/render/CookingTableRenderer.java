package net.blay09.mods.cookingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blay09.mods.cookingforblockheads.block.entity.CookingTableBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CookingTableRenderer implements BlockEntityRenderer<CookingTableBlockEntity> {

    public CookingTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CookingTableBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!blockEntity.hasLevel()) {
            return;
        }

        Level level = blockEntity.getLevel();
        BlockState state = blockEntity.getBlockState();

        ItemStack itemStack = blockEntity.getNoFilterBook();
        if (!itemStack.isEmpty()) {
            poseStack.pushPose();
            RenderUtils.applyBlockAngle(poseStack, state);
            poseStack.translate(0, 1.0725f, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.5f, 0.5f, 0.5f);
            RenderUtils.renderItem(itemStack, combinedLightIn, poseStack, bufferIn, level);
            poseStack.popPose();
        }
    }

}
