package net.blay09.mods.cookingforblockheads.block.entity;

import com.google.common.collect.Lists;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.block.entity.CustomRenderBoundingBox;
import net.blay09.mods.balm.api.container.*;
import net.blay09.mods.balm.api.energy.BalmEnergyStorageProvider;
import net.blay09.mods.balm.api.energy.EnergyStorage;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.api.tag.BalmItemTags;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.api.IngredientToken;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.KitchenOperation;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProcessor;
import net.blay09.mods.cookingforblockheads.block.entity.util.TransferableBlockEntity;
import net.blay09.mods.cookingforblockheads.block.entity.util.TransferableContainer;
import net.blay09.mods.cookingforblockheads.kitchen.ContainerKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.recipe.ModRecipes;
import net.blay09.mods.cookingforblockheads.sound.ModSounds;
import net.blay09.mods.cookingforblockheads.api.event.OvenCookedEvent;
import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.blay09.mods.cookingforblockheads.block.OvenBlock;
import net.blay09.mods.cookingforblockheads.menu.OvenMenu;
import net.blay09.mods.cookingforblockheads.block.entity.util.DoorAnimator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OvenBlockEntity extends BalmBlockEntity implements KitchenItemProcessor, BalmMenuProvider<BlockPos>, IMutableNameable, BalmContainerProvider, BalmEnergyStorageProvider, CustomRenderBoundingBox, TransferableBlockEntity<TransferableContainer> {

    private static final int COOK_TIME = 200;

    private final DefaultContainer container = new DefaultContainer(20) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack itemStack) {
            if (slot < 3) {
                return !getSmeltingResult(itemStack).isEmpty();
            } else if (slot == 3) {
                return isItemFuel(level, itemStack);
            }
            return true;
        }

        @Override
        public void slotChanged(int slot) {
            if (slot >= 7 && slot < 16) {
                slotCookTime[slot - 7] = 0;
            }
            isDirty = true;
            OvenBlockEntity.this.setChanged();
        }
    };

    private final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            if (id == 0) {
                return OvenBlockEntity.this.furnaceBurnTime;
            } else if (id == 1) {
                return OvenBlockEntity.this.currentItemBurnTime;
            } else if (id >= 2 && id <= 11) {
                return OvenBlockEntity.this.slotCookTime[id - 2];
            }
            return 0;
        }

        public void set(int id, int value) {
            if (id == 0) {
                OvenBlockEntity.this.furnaceBurnTime = value;
            } else if (id == 1) {
                OvenBlockEntity.this.currentItemBurnTime = value;
            } else if (id >= 2 && id <= 11) {
                OvenBlockEntity.this.slotCookTime[id - 2] = value;
            }

        }

        public int getCount() {
            return 11;
        }
    };

    private final EnergyStorage energyStorage = new EnergyStorage(10000) {
        @Override
        public int fill(int maxReceive, boolean simulate) {
            if (!simulate) {
                OvenBlockEntity.this.setChanged();
            }

            return super.fill(maxReceive, simulate);
        }

        @Override
        public int drain(int maxExtract, boolean simulate) {
            if (!simulate) {
                OvenBlockEntity.this.setChanged();
            }

            return super.drain(maxExtract, simulate);
        }
    };

    private final SubContainer inputContainer = new SubContainer(container, 0, 3);
    private final SubContainer fuelContainer = new SubContainer(container, 3, 4);
    private final SubContainer outputContainer = new SubContainer(container, 4, 7);
    private final SubContainer processingContainer = new SubContainer(container, 7, 16);
    private final SubContainer toolsContainer = new SubContainer(container, 16, 20);
    private final KitchenItemProvider itemProvider = new ContainerKitchenItemProvider(new CombinedContainer(toolsContainer, outputContainer));
    private final DoorAnimator doorAnimator = new DoorAnimator(this, 1, 2);

    private Component customName;

    private boolean isFirstTick = true;

    public int[] slotCookTime = new int[9];
    public int furnaceBurnTime;
    public int currentItemBurnTime;
    private boolean isDirty;

    private boolean hasPowerUpgrade;
    private Direction facing;

    public OvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.oven.get(), pos, state);
        doorAnimator.setSoundEventOpen(ModSounds.ovenOpen.get());
        doorAnimator.setSoundEventClose(ModSounds.ovenClose.get());
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        return doorAnimator.receiveClientEvent(id, type) || super.triggerEvent(id, type);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, OvenBlockEntity blockEntity) {
        blockEntity.clientTick(level, pos, state);
    }

    public void clientTick(Level level, BlockPos pos, BlockState state) {
        doorAnimator.update();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OvenBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (isFirstTick) {
            if (state.hasProperty(OvenBlock.FACING)) {
                facing = state.getValue(OvenBlock.FACING);
                isFirstTick = false;
            }
        }

        if (isDirty) {
            sync();
            isDirty = false;
        }

        boolean hasChanged = false;

        int burnPotential = 200 - furnaceBurnTime;
        if (hasPowerUpgrade && burnPotential > 0 && shouldConsumeFuel()) {
            furnaceBurnTime += energyStorage.drain(burnPotential, false);
        }

        if (furnaceBurnTime > 0) {
            furnaceBurnTime--;
        }

        if (!level.isClientSide) {
            if (furnaceBurnTime == 0 && shouldConsumeFuel()) {
                // Check for fuel items in side slots
                for (int i = 0; i < fuelContainer.getContainerSize(); i++) {
                    ItemStack fuelItem = fuelContainer.getItem(i);
                    if (!fuelItem.isEmpty()) {
                        currentItemBurnTime = furnaceBurnTime = (int) Math.max(1,
                                (float) getBurnTime(level, fuelItem) * CookingForBlockheadsConfig.getActive().ovenFuelTimeMultiplier);
                        if (furnaceBurnTime != 0) {
                            ItemStack containerItem = Balm.getHooks().getCraftingRemainingItem(fuelItem);
                            fuelItem.shrink(1);
                            if (fuelItem.isEmpty()) {
                                fuelContainer.setItem(i, containerItem);
                            }
                            hasChanged = true;
                        }
                        break;
                    }
                }
            }

            int firstEmptySlot = -1;
            int firstTransferSlot = -1;
            for (int i = 0; i < processingContainer.getContainerSize(); i++) {
                ItemStack itemStack = processingContainer.getItem(i);

                if (!itemStack.isEmpty()) {
                    if (slotCookTime[i] != -1) {
                        double maxCookTime = COOK_TIME * CookingForBlockheadsConfig.getActive().ovenCookTimeMultiplier;
                        if (slotCookTime[i] >= maxCookTime && firstTransferSlot == -1) {
                            firstTransferSlot = i;
                            continue;
                        }

                        if (furnaceBurnTime > 0) {
                            slotCookTime[i]++;
                        }

                        if (slotCookTime[i] >= maxCookTime) {
                            ItemStack smeltingResult = getSmeltingResult(itemStack);
                            if (!smeltingResult.isEmpty()) {
                                ItemStack resultStack = smeltingResult.copy();
                                processingContainer.setItem(i, resultStack);
                                Balm.getEvents().fireEvent(new OvenCookedEvent(level, worldPosition, resultStack));
                                slotCookTime[i] = -1;
                                if (firstTransferSlot == -1) {
                                    firstTransferSlot = i;
                                }
                            }
                        }
                    } else if (firstTransferSlot == -1) {
                        firstTransferSlot = i;
                    }
                } else if (firstEmptySlot == -1) {
                    firstEmptySlot = i;
                }
            }

            // Move cooked items from processing to output
            if (firstTransferSlot != -1) {
                ItemStack transferStack = processingContainer.getItem(firstTransferSlot);
                transferStack = ContainerUtils.insertItemStacked(outputContainer, transferStack, false);
                processingContainer.setItem(firstTransferSlot, transferStack);
                if (transferStack.isEmpty()) {
                    slotCookTime[firstTransferSlot] = 0;
                }
                hasChanged = true;
            }

            // Move cookable items from input to processing
            if (firstEmptySlot != -1) {
                for (int j = 0; j < inputContainer.getContainerSize(); j++) {
                    ItemStack itemStack = inputContainer.getItem(j);
                    if (!itemStack.isEmpty()) {
                        processingContainer.setItem(firstEmptySlot, itemStack.split(1));
                        if (itemStack.getCount() <= 0) {
                            inputContainer.setItem(j, ItemStack.EMPTY);
                        }
                        break;
                    }
                }
            }
        }

        if (hasChanged) {
            setChanged();
        }
    }

    public ItemStack getSmeltingResult(ItemStack itemStack) {
        final var recipeInput = new SingleRecipeInput(itemStack);
        final var ovenRecipeResult = getSmeltingResult(ModRecipes.ovenRecipeType, recipeInput);
        if (!ovenRecipeResult.isEmpty()) {
            return ovenRecipeResult;
        }

        return getSmeltingResult(RecipeType.SMELTING, recipeInput);
    }

    public <T extends RecipeInput> ItemStack getSmeltingResult(RecipeType<? extends Recipe<T>> recipeType, T recipeInput) {
        final var server = level.getServer();
        if (server != null) {
            final var recipe = server.getRecipeManager().getRecipeFor(recipeType, recipeInput, level).orElse(null);
            if (recipe != null) {
                final var result = recipe.value().assemble(recipeInput, level.registryAccess());
                if (!result.isEmpty() && result.has(DataComponents.FOOD)) {
                    return result;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isItemFuel(Level level, ItemStack itemStack) {
        if (CookingForBlockheadsConfig.getActive().ovenRequiresCookingOil) {
            return itemStack.is(BalmItemTags.COOKING_OIL);
        }

        return getBurnTime(level, itemStack) > 0;
    }

    protected static int getBurnTime(Level level, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        if (CookingForBlockheadsConfig.getActive().ovenRequiresCookingOil && itemStack.is(BalmItemTags.COOKING_OIL)) {
            return 800;
        }

        return Balm.getHooks().getBurnTime(level, itemStack);
    }

    private boolean shouldConsumeFuel() {
        for (int i = 0; i < processingContainer.getContainerSize(); i++) {
            ItemStack cookingStack = processingContainer.getItem(i);
            if (!cookingStack.isEmpty() && slotCookTime[i] != -1) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        final var customNameComponent = input.get(DataComponents.CUSTOM_NAME);
        if (customNameComponent != null) {
            customName = customNameComponent;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        builder.set(DataComponents.CUSTOM_NAME, customName);
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        container.deserialize(tagCompound.getCompound("ItemHandler"), provider);
        furnaceBurnTime = tagCompound.getShort("BurnTime");
        currentItemBurnTime = tagCompound.getShort("CurrentItemBurnTime");
        slotCookTime = tagCompound.getIntArray("CookTimes");
        if (slotCookTime.length != 9) {
            slotCookTime = new int[9];
        }

        hasPowerUpgrade = tagCompound.getBoolean("HasPowerUpgrade");
        energyStorage.setEnergy(tagCompound.getInt("EnergyStored"));

        if (tagCompound.contains("CustomName", Tag.TAG_STRING)) {
            customName = Component.Serializer.fromJson(tagCompound.getString("CustomName"), provider);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("ItemHandler", container.serialize(provider));
        tag.putShort("BurnTime", (short) furnaceBurnTime);
        tag.putShort("CurrentItemBurnTime", (short) currentItemBurnTime);
        tag.putIntArray("CookTimes", ArrayUtils.clone(slotCookTime));

        tag.putBoolean("HasPowerUpgrade", hasPowerUpgrade);
        tag.putInt("EnergyStored", energyStorage.getEnergy());

        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName, provider));
        }

        if (tag.contains("IsForcedOpen", Tag.TAG_BYTE)) {
            doorAnimator.setForcedOpen(tag.getBoolean("IsForcedOpen"));
        }

        if (tag.contains("NumPlayersUsing", Tag.TAG_BYTE)) {
            doorAnimator.setNumPlayersUsing(tag.getByte("NumPlayersUsing"));
        }
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        saveAdditional(tag, level.registryAccess());
        tag.putBoolean("IsForcedOpen", doorAnimator.isForcedOpen());
        tag.putByte("NumPlayersUsing", (byte) doorAnimator.getNumPlayersUsing());
    }

    public boolean hasPowerUpgrade() {
        return hasPowerUpgrade;
    }

    public void setHasPowerUpgrade(boolean hasPowerUpgrade) {
        this.hasPowerUpgrade = hasPowerUpgrade;
        setChanged();
    }

    public boolean isBurning() {
        return furnaceBurnTime > 0;
    }

    public float getBurnTimeProgress() {
        if (currentItemBurnTime == 0 && furnaceBurnTime > 0) {
            return 1f;
        }

        return (float) furnaceBurnTime / (float) currentItemBurnTime;
    }

    public float getCookProgress(int i) {
        return (float) slotCookTime[i] / ((float) (COOK_TIME * CookingForBlockheadsConfig.getActive().ovenCookTimeMultiplier));
    }

    @Override
    public boolean canProcess(RecipeType<?> recipeType) {
        return recipeType == RecipeType.SMELTING;
    }

    @Override
    public KitchenOperation processRecipe(Recipe<?> recipe, List<IngredientToken> ingredientTokens) {
        for (final var ingredientToken : ingredientTokens) {
            final var itemStack = ingredientToken.consume();
            final var restStack = ContainerUtils.insertItemStacked(inputContainer, itemStack, false);
            if (!restStack.isEmpty()) {
                ingredientToken.restore(restStack);
            }
        }
        return KitchenOperation.EMPTY;
    }

    public DoorAnimator getDoorAnimator() {
        return doorAnimator;
    }

    public ItemStack getToolItem(int i) {
        return toolsContainer.getItem(i);
    }

    public void setToolItem(int i, ItemStack itemStack) {
        toolsContainer.setItem(i, itemStack);
    }

    @Override
    public Container getContainer(Direction side) {
        if (CookingForBlockheadsConfig.getActive().disallowOvenAutomation) {
            return null;
        }

        if (side == null) {
            return getInternalContainer();
        }

        return switch (side) {
            case UP -> inputContainer;
            case DOWN -> outputContainer;
            default -> fuelContainer;
        };
    }

    @Override
    public List<BalmProvider<?>> getProviders() {
        return Lists.newArrayList(
                new BalmProvider<>(KitchenItemProvider.class, itemProvider),
                new BalmProvider<>(KitchenItemProcessor.class, this)
        );
    }

    public Container getInputContainer() {
        return inputContainer;
    }

    public Container getFuelContainer() {
        return fuelContainer;
    }

    public Direction getFacing() {
        return facing == null ? Direction.NORTH : facing;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new OvenMenu(i, playerInventory, this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1).getCenter(), worldPosition.offset(2, 1, 2).getCenter());
    }

    @Override
    public Component getName() {
        return customName != null ? customName : getDefaultName();
    }

    @Override
    public void setCustomName(Component customName) {
        this.customName = customName;
        setChanged();
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("container.cookingforblockheads.oven");
    }

    public Container getInternalContainer() {
        return container;
    }

    @Override
    public Container getContainer() {
        if (CookingForBlockheadsConfig.getActive().disallowOvenAutomation) {
            return null;
        }

        return container;
    }

    public ContainerData getContainerData() {
        return dataAccess;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BlockPos> getScreenStreamCodec() {
        return BlockPos.STREAM_CODEC.cast();
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
        return worldPosition;
    }

    @Override
    public TransferableContainer snapshotDataForTransfer() {
        return TransferableContainer.copyAndClear(container);
    }

    @Override
    public void restoreFromTransferSnapshot(TransferableContainer data) {
        data.applyTo(container);
    }
}
