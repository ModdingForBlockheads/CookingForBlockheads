package net.blay09.mods.cookingforblockheads.block.entity;

import com.google.common.collect.Lists;
import net.blay09.mods.balm.api.fluid.BalmFluidTankProvider;
import net.blay09.mods.balm.api.fluid.FluidTank;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.api.CacheHint;
import net.blay09.mods.cookingforblockheads.api.IngredientToken;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.block.entity.util.TransferableBlockEntity;
import net.blay09.mods.cookingforblockheads.compat.Compat;
import net.blay09.mods.cookingforblockheads.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Collection;
import java.util.List;

public class SinkBlockEntity extends BalmBlockEntity implements BalmFluidTankProvider, TransferableBlockEntity<Integer> {

    private static final int SYNC_INTERVAL = 10;

    private record SinkIngredientToken(SinkBlockEntity milkJar, ItemStack itemStack) implements IngredientToken {
        @Override
        public ItemStack peek() {
            final var drained = milkJar.getFluidTank().drain(Compat.getMilkFluid(), 1000, true);
            return drained >= 1000 ? itemStack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack consume() {
            final var drained = milkJar.getFluidTank().drain(Compat.getMilkFluid(), 1000, false);
            return drained >= 1000 ? itemStack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack restore(ItemStack itemStack) {
            milkJar.getFluidTank().fill(Compat.getMilkFluid(), 1000, false);
            return ItemStack.EMPTY;
        }
    }

    private record SinkItemProvider(SinkBlockEntity sink) implements KitchenItemProvider {
        @Override
        public IngredientToken findIngredient(Ingredient ingredient, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
            for (final var waterItem : BuiltInRegistries.ITEM.getTagOrEmpty(ModItemTags.WATER))
                if (ingredient.acceptsItem(waterItem)) {
                    final var waterUnitsUsed = ingredientTokens.size();
                    final var waterUnitsAvailable = sink.getFluidTank().getAmount() / 1000 - waterUnitsUsed;
                    if (waterUnitsAvailable > 1) {
                        return new SinkIngredientToken(sink, new ItemStack(waterItem));
                    } else {
                        return null;
                    }
                }

            return null;
        }

        @Override
        public IngredientToken findIngredient(ItemStack itemStack, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
            if (!itemStack.is(ModItemTags.WATER)) {
                return null;
            }

            final var waterUnitsUsed = ingredientTokens.size();
            final var waterUnitsAvailable = sink.getFluidTank().getAmount() / 1000 - waterUnitsUsed;
            if (waterUnitsAvailable > 1) {
                return new SinkIngredientToken(sink, itemStack);
            } else {
                return null;
            }
        }

        @Override
        public CacheHint getCacheHint(IngredientToken ingredientToken) {
            return CacheHint.NONE;
        }
    }

    private final FluidTank sinkTank = new FluidTank(16000) {

        @Override
        public Fluid getFluid() {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return Fluids.WATER;
            }

            return super.getFluid();
        }

        @Override
        public int getAmount() {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return Integer.MAX_VALUE;
            }

            return super.getAmount();
        }

        @Override
        public int getCapacity() {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return Integer.MAX_VALUE;
            }

            return super.getCapacity();
        }

        @Override
        public int drain(Fluid fluid, int maxDrain, boolean simulate) {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater && fluid == Fluids.WATER) {
                return maxDrain;
            }

            if (fluid.isSame(Fluids.EMPTY) || !fluid.isSame(fluid)) {
                return 0;
            }

            SinkBlockEntity.this.setChanged();

            return super.drain(fluid, maxDrain, simulate);
        }

        @Override
        public int fill(Fluid fluid, int maxFill, boolean simulate) {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return maxFill;
            }

            SinkBlockEntity.this.setChanged();

            return super.fill(fluid, maxFill, simulate);
        }
    };

    private final SinkItemProvider itemProvider = new SinkItemProvider(this);

    private int ticksSinceSync;
    private boolean isDirty;

    private DyeColor color = DyeColor.WHITE;

    public SinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.sink.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("FluidTank", sinkTank.serialize());
        tag.putByte("Color", (byte) color.getId());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        sinkTank.deserialize(tag.getCompound("FluidTank"));
        color = DyeColor.byId(tag.getByte("Color"));
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        saveAdditional(tag, level.registryAccess());
    }

    @Override
    public List<BalmProvider<?>> getProviders() {
        return Lists.newArrayList(new BalmProvider<>(KitchenItemProvider.class, itemProvider));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SinkBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        // Sync to clients
        ticksSinceSync++;
        if (ticksSinceSync >= SYNC_INTERVAL) {
            ticksSinceSync = 0;
            if (isDirty) {
                sync();
                isDirty = false;
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        isDirty = true;
    }

    @Override
    public FluidTank getFluidTank() {
        return sinkTank;
    }

    @Override
    public Integer snapshotDataForTransfer() {
        return sinkTank.getAmount();
    }

    @Override
    public void restoreFromTransferSnapshot(Integer data) {
        sinkTank.setFluid(Fluids.WATER, data);
    }
}
