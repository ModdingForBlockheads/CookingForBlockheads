package net.blay09.mods.cookingforblockheads.block;

import com.mojang.serialization.MapCodec;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.cookingforblockheads.block.entity.CuttingBoardBlockEntity;
import net.blay09.mods.cookingforblockheads.menu.CuttingBoardMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CuttingBoardBlock extends BaseKitchenBlock {

    public static final MapCodec<CuttingBoardBlock> CODEC = simpleCodec(CuttingBoardBlock::new);
    private static final Component CONTAINER_TITLE = Component.translatable("container.cookingforblockheads.cutting_board");

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 1.6, 14);

    public CuttingBoardBlock(Properties properties) {
        super(properties.sound(SoundType.WOOD).strength(2.5f));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CuttingBoardBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            Balm.getNetworking().openGui(player, state.getMenuProvider(level, pos));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new BalmMenuProvider<BlockPos>() {
            @Override
            public Component getDisplayName() {
                return CONTAINER_TITLE;
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
                return new CuttingBoardMenu(windowId, inventory, ContainerLevelAccess.create(level, pos));
            }

            @Override
            public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
                return pos;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, BlockPos> getScreenStreamCodec() {
                return BlockPos.STREAM_CODEC.cast();
            }
        };
    }
}
