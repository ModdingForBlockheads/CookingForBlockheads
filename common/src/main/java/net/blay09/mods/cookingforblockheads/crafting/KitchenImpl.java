package net.blay09.mods.cookingforblockheads.crafting;

import net.blay09.mods.balm.api.container.BalmContainerProvider;
import net.blay09.mods.balm.api.provider.ProviderUtils;
import net.blay09.mods.cookingforblockheads.api.Kitchen;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProcessor;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.block.entity.CookingTableBlockEntity;
import net.blay09.mods.cookingforblockheads.item.ModItems;
import net.blay09.mods.cookingforblockheads.kitchen.ContainerKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KitchenImpl implements Kitchen {

    private final ItemStack activatingItemStack;
    private final BlockState activatingBlockState;
    private final BlockEntity activatingBlockEntity;
    private final Set<BlockPos> checkedPos = new HashSet<>();
    private final List<KitchenItemProvider> itemProviderList = new ArrayList<>();
    private final List<KitchenItemProcessor> itemProcessorList = new ArrayList<>();

    public KitchenImpl(ItemStack itemStack) {
        activatingItemStack = itemStack;
        activatingBlockState = Blocks.AIR.defaultBlockState();
        activatingBlockEntity = null;
    }

    public KitchenImpl(Level level, BlockPos pos) {
        activatingBlockState = level.getBlockState(pos);
        activatingItemStack = ItemStack.EMPTY;
        activatingBlockEntity = level.getBlockEntity(pos);
        findNeighbourCraftingBlocks(level, pos, true);
    }

    private void findNeighbourCraftingBlocks(Level level, BlockPos pos, boolean extendedUpSearch) {
        for (Direction direction : Direction.values()) {
            int upSearch = (extendedUpSearch && direction == Direction.UP) ? 2 : 1;
            for (int n = 1; n <= upSearch; n++) {
                BlockPos position = pos.relative(direction, n);
                if (!checkedPos.contains(position)) {
                    checkedPos.add(position);

                    BlockState state = level.getBlockState(position);
                    BlockEntity blockEntity = level.getBlockEntity(position);
                    if (blockEntity != null) {
                        var itemProvider = ProviderUtils.getProvider(blockEntity, KitchenItemProvider.class);
                        if (itemProvider == null && state.is(ModBlockTags.KITCHEN_ITEM_PROVIDERS)) {
                            // We need to do this again because NeoForge has no fallback capability providers...
                            if (blockEntity instanceof Container container) {
                                itemProvider = new ContainerKitchenItemProvider(container);
                            } else if (blockEntity instanceof BalmContainerProvider containerProvider) {
                                itemProvider = new ContainerKitchenItemProvider(containerProvider.getContainer());
                            }
                        }

                        if (itemProvider != null) {
                            itemProviderList.add(itemProvider);
                        }

                        final var itemProcessor = ProviderUtils.getProvider(blockEntity, KitchenItemProcessor.class);
                        if (itemProcessor != null) {
                            itemProcessorList.add(itemProcessor);
                        }

                        if (itemProvider != null || itemProcessor != null || state.is(ModBlockTags.KITCHEN_CONNECTORS)) {
                            findNeighbourCraftingBlocks(level, position, true);
                        }
                    } else if (state.is(ModBlockTags.KITCHEN_CONNECTORS)) {
                        findNeighbourCraftingBlocks(level, position, false);
                    }
                }
            }
        }
    }

    @Override
    public List<KitchenItemProvider> getItemProviders(@Nullable Player player) {
        final var sourceInventories = new ArrayList<>(itemProviderList);
        if (player != null) {
            sourceInventories.add(new ContainerKitchenItemProvider(player.getInventory()));
        }
        return sourceInventories;
    }

    @Override
    public List<KitchenItemProcessor> getItemProcessors() {
        return itemProcessorList;
    }

    @Override
    public boolean canProcess(RecipeType<?> recipeType) {
        if (recipeType == RecipeType.CRAFTING) {
            return activatingBlockState.is(ModBlockTags.COOKING_TABLES) || activatingItemStack.is(ModItems.craftingBook);
        }

        return itemProcessorList.stream().anyMatch(it -> it.canProcess(recipeType));
    }

    public boolean isRecipeAvailable(CraftingOperation operation) {
        final var isNoFilter = activatingItemStack.is(ModItems.noFilterBook) || (activatingBlockEntity instanceof CookingTableBlockEntity cookingTable && cookingTable.hasNoFilterBook());
        if (isNoFilter) {
            return true;
        }

        return operation.canCraft();
    }
}
