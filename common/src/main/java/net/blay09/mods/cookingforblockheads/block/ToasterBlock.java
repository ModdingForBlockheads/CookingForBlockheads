package net.blay09.mods.cookingforblockheads.block;


import com.mojang.serialization.MapCodec;
import net.blay09.mods.cookingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.cookingforblockheads.block.entity.ToasterBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToasterBlock extends BaseKitchenBlock {

    public static final MapCodec<ToasterBlock> CODEC = simpleCodec(ToasterBlock::new);

    private static final VoxelShape SHAPE = Block.box(4.4, 0, 4.4, 11.6, 6.4, 11.6);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ToasterBlock(Properties properties) {
        super(properties.sound(SoundType.METAL).strength(2.5f));
        registerDefaultState(getStateDefinition().any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ToasterBlockEntity toaster) {
            if (!toaster.getContainer().getItem(0).isEmpty() && !toaster.getContainer().getItem(1).isEmpty()) {
                if (!toaster.isActive() && (!toaster.getContainer().getItem(0).isEmpty() || !toaster.getContainer().getItem(1).isEmpty())) {
                    toaster.setActive(!toaster.isActive());
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (itemStack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ToasterBlockEntity toaster) {
            if (!toaster.getContainer().getItem(0).isEmpty() && !toaster.getContainer().getItem(1).isEmpty()) {
                if (!toaster.isActive() && (!toaster.getContainer().getItem(0).isEmpty() || !toaster.getContainer().getItem(1).isEmpty())) {
                    toaster.setActive(!toaster.isActive());
                }
            } else {
                final var heldItem = player.getItemInHand(hand);
                if (toaster.canToast(heldItem)) {
                    for (int i = 0; i < toaster.getContainer().getContainerSize(); i++) {
                        if (toaster.getContainer().getItem(i).isEmpty()) {
                            toaster.getContainer().setItem(i, player.getAbilities().instabuild ? heldItem.copy().split(1) : heldItem.split(1));
                            return InteractionResult.SUCCESS;
                        }
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToasterBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        ToasterBlockEntity tileEntity = (ToasterBlockEntity) world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity.isActive()) {
            boolean burning = tileEntity.isBurningToast();
            float particleChance = tileEntity.getToastProgress();
            int particleCount = 1;
            if (burning) {
                particleChance *= 3;
                particleCount = random.nextInt(1, Math.max(2, (int) Math.ceil(5 * tileEntity.getToastProgress())));
            }
            if (random.nextFloat() < particleChance) {
                for (int i = 0; i < particleCount; i++) {
                    double x = (float) pos.getX() + 0.5f + (random.nextFloat() - 0.5f) * 0.25f;
                    double y = (float) pos.getY() + 0.2f + random.nextFloat() * 6f / 16f;
                    double z = (float) pos.getZ() + 0.5f + (random.nextFloat() - 0.5f) * 0.25f;
                    if (burning && Math.random() <= 0.5) {
                        world.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
                    } else {
                        world.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.toaster.get(), ToasterBlockEntity::serverTick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    @Override
    protected void appendHoverDescriptionText(ItemStack itemStack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cookingforblockheads.toaster.description").withStyle(ChatFormatting.GRAY));
    }
}
