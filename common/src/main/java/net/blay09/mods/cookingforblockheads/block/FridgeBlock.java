package net.blay09.mods.cookingforblockheads.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.balm.api.Balm;

import net.blay09.mods.cookingforblockheads.util.ItemUtils;
import net.blay09.mods.cookingforblockheads.item.ModItems;
import net.blay09.mods.cookingforblockheads.block.entity.FridgeBlockEntity;
import net.blay09.mods.cookingforblockheads.block.entity.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FridgeBlock extends BaseKitchenBlock {

    public static final MapCodec<FridgeBlock> CODEC = RecordCodecBuilder.mapCodec((it) -> it.group(DyeColor.CODEC.fieldOf("color")
                    .forGetter(FridgeBlock::getColor),
            propertiesCodec()).apply(it, FridgeBlock::new));

    public enum FridgeModelType implements StringRepresentable {
        SMALL,
        LARGE_LOWER,
        LARGE_UPPER;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static final EnumProperty<FridgeModelType> MODEL_TYPE = EnumProperty.create("model", FridgeModelType.class);
    public static final BooleanProperty PRESERVATION_CHAMBER = BooleanProperty.create("preservation_chamber");
    public static final BooleanProperty ICE_UNIT = BooleanProperty.create("ice_unit");

    private static final VoxelShape BOUNDING_BOX_X = Block.box(1, 0, 1, 15, 16, 15);
    private static final VoxelShape BOUNDING_BOX_Z = Block.box(1, 0, 1, 15, 16, 15);

    private final DyeColor color;

    public FridgeBlock(DyeColor color, Properties properties) {
        super(properties.pushReaction(PushReaction.BLOCK).sound(SoundType.METAL).strength(5f, 10f));
        this.color = color;
        registerDefaultState(getStateDefinition().any().setValue(PRESERVATION_CHAMBER, false).setValue(ICE_UNIT, false));
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.X) {
            return BOUNDING_BOX_X;
        } else {
            return BOUNDING_BOX_Z;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODEL_TYPE, FLIPPED, PRESERVATION_CHAMBER, ICE_UNIT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FridgeBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (itemStack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        final var blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof FridgeBlockEntity fridge)) {
            return InteractionResult.FAIL;
        }

        if (itemStack.getItem() == ModItems.preservationChamber || itemStack.getItem() == ModItems.iceUnit) {
            return InteractionResult.PASS;
        }

        if (tryRecolorBlock(state, itemStack, level, pos, player, blockHitResult)) {
            return InteractionResult.SUCCESS;
        }

        Direction frontFace = state.getValue(FACING);
        if (blockHitResult.getDirection() == frontFace) {
            if (fridge.getBaseFridge().getDoorAnimator().isForcedOpen()) {
                itemStack = fridge.insertItemStacked(itemStack, false);
                player.setItemInHand(hand, itemStack);
                return InteractionResult.SUCCESS;
            }
        }

        if (Block.byItem(itemStack.getItem()) instanceof FridgeBlock && blockHitResult.getDirection() != frontFace) {
            return InteractionResult.FAIL;
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, blockHitResult);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        final var blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof FridgeBlockEntity fridge)) {
            return InteractionResult.FAIL;
        }

        Direction frontFace = state.getValue(FACING);
        if (blockHitResult.getDirection() == frontFace) {
            if (player.isShiftKeyDown()) {
                fridge.getBaseFridge().getDoorAnimator().toggleForcedOpen();
                return InteractionResult.SUCCESS;
            }
        }

        if (!level.isClientSide) {
            Balm.getNetworking().openGui(player, fridge);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        boolean below = level.getBlockState(pos.below()).getBlock() == this;
        boolean above = level.getBlockState(pos.above()).getBlock() == this;
        return !(below && above)
                && !(below && level.getBlockState(pos.below(2)).getBlock() == this)
                && !(above && level.getBlockState(pos.above(2)).getBlock() == this)
                && super.canSurvive(state, level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = super.getStateForPlacement(context);
        final var level = context.getLevel();
        final var pos = context.getClickedPos();//.relative(context.getClickedFace());
        final var posBelow = pos.below();
        final var stateBelow = level.getBlockState(posBelow);
        final var posAbove = pos.above();
        final var stateAbove = level.getBlockState(posAbove);
        if (stateBelow.getBlock() == this && stateBelow.getValue(MODEL_TYPE) == FridgeModelType.SMALL) {
            state = state.setValue(MODEL_TYPE, FridgeModelType.LARGE_UPPER);
        } else if (stateAbove.getBlock() == this && stateAbove.getValue(MODEL_TYPE) == FridgeModelType.SMALL) {
            state = state.setValue(MODEL_TYPE, FridgeModelType.LARGE_LOWER);
        }
        return state.setValue(FLIPPED, shouldBePlacedFlipped(context, state.getValue(FACING)));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource randomSource) {
        final var posBelow = pos.below();
        final var stateBelow = level.getBlockState(posBelow);
        final var posAbove = pos.above();
        final var stateAbove = level.getBlockState(posAbove);
        if (stateBelow.getBlock() == this && stateBelow.getValue(MODEL_TYPE) == FridgeModelType.LARGE_LOWER) {
            return state.setValue(MODEL_TYPE, FridgeModelType.LARGE_UPPER);
        } else if (stateAbove.getBlock() == this && stateAbove.getValue(MODEL_TYPE) == FridgeModelType.LARGE_UPPER) {
            return state.setValue(MODEL_TYPE, FridgeModelType.LARGE_LOWER);
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, facing, facingPos, facingState, randomSource);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FridgeBlockEntity) {
                if (((FridgeBlockEntity) blockEntity).hasIceUpgrade()) {
                    ItemUtils.spawnItemStack(level, pos.getX() + 0.5f, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.iceUnit));
                }

                if (((FridgeBlockEntity) blockEntity).hasPreservationUpgrade()) {
                    ItemUtils.spawnItemStack(level, pos.getX() + 0.5f, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.preservationChamber));
                }
            }

            BlockPos posAbove = pos.above();
            BlockState stateAbove = level.getBlockState(posAbove);
            BlockPos posBelow = pos.below();
            BlockState stateBelow = level.getBlockState(posBelow);
            if (stateAbove.getBlock() == this && stateAbove.getValue(MODEL_TYPE) == FridgeModelType.LARGE_UPPER) {
                level.setBlock(posAbove, stateAbove.setValue(MODEL_TYPE, FridgeModelType.SMALL), 3);
            } else if (stateBelow.getBlock() == this && stateBelow.getValue(MODEL_TYPE) == FridgeModelType.LARGE_LOWER) {
                level.setBlock(posBelow, stateBelow.setValue(MODEL_TYPE, FridgeModelType.SMALL), 3);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.fridge.get(), FridgeBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.fridge.get(), FridgeBlockEntity::serverTick);
    }

    @Override
    protected boolean recolorBlock(BlockState state, LevelAccessor world, BlockPos pos, Direction facing, DyeColor color) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FridgeBlockEntity fridge) {
            BlockPos bottomPos = fridge.getBaseFridge().getBlockPos();
            BlockPos topPos = bottomPos.above();
            return super.recolorBlock(world.getBlockState(bottomPos), world, bottomPos, facing, color) && super.recolorBlock(world.getBlockState(topPos),
                    world,
                    topPos,
                    facing,
                    color);
        }

        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void appendHoverDescriptionText(ItemStack itemStack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cookingforblockheads.fridge.description").withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected BlockState getDyedStateOf(BlockState state, @Nullable DyeColor color) {
        final var block = color == null ? ModBlocks.fridges[0] : ModBlocks.fridges[color.ordinal()];
        return block.defaultBlockState()
                .setValue(FACING, state.getValue(FACING))
                .setValue(MODEL_TYPE, state.getValue(MODEL_TYPE))
                .setValue(PRESERVATION_CHAMBER, state.getValue(PRESERVATION_CHAMBER))
                .setValue(ICE_UNIT, state.getValue(ICE_UNIT));
    }
}
