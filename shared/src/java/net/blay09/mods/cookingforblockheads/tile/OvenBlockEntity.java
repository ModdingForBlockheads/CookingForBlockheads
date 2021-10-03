package net.blay09.mods.cookingforblockheads.tile;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.BalmHooks;
import net.blay09.mods.balm.api.block.entity.BalmBlockEntity;
import net.blay09.mods.balm.api.container.*;
import net.blay09.mods.balm.api.energy.EnergyStorage;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.ModSounds;
import net.blay09.mods.cookingforblockheads.api.capability.*;
import net.blay09.mods.cookingforblockheads.api.event.OvenCookedEvent;
import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.blay09.mods.cookingforblockheads.block.OvenBlock;
import net.blay09.mods.cookingforblockheads.compat.Compat;
import net.blay09.mods.cookingforblockheads.menu.OvenMenu;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.blay09.mods.cookingforblockheads.tile.util.DoorAnimator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public class OvenBlockEntity extends BalmBlockEntity implements IKitchenSmeltingProvider, BalmMenuProvider, IMutableNameable, BalmContainerProvider {

    private static final int COOK_TIME = 200;

    private final DefaultContainer container = new DefaultContainer(20) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack itemStack) {
            if (slot < 3) {
                return !getSmeltingResult(itemStack).isEmpty();
            } else if (slot == 3) {
                return isItemFuel(itemStack);
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
    private final DefaultKitchenItemProvider itemProvider = new DefaultKitchenItemProvider(new CombinedContainer(toolsContainer, outputContainer));
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

    public void tick() { // TODO
        if (isFirstTick) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() == ModBlocks.oven) {
                facing = state.getValue(OvenBlock.FACING);
                isFirstTick = false;
            }
        }

        doorAnimator.update();

        if (isDirty) {
            balmSync();
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
                        currentItemBurnTime = furnaceBurnTime = (int) Math.max(1, (float) getBurnTime(fuelItem) * CookingForBlockheadsConfig.getActive().ovenFuelTimeMultiplier);
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

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private final Container singleSlotRecipeWrapper = new DefaultContainer(1);

    public ItemStack getSmeltingResult(ItemStack itemStack) {
        ItemStack result = CookingRegistry.getSmeltingResult(itemStack);
        if (!result.isEmpty()) {
            return result;
        }

        singleSlotRecipeWrapper.setItem(0, itemStack);
        Recipe<?> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, singleSlotRecipeWrapper, this.level).orElse(null);
        if (recipe != null) {
            result = recipe.getResultItem();
            if (!result.isEmpty() && result.getItem().isEdible()) {
                return result;
            }
        }

        if (!result.isEmpty() && CookingRegistry.isNonFoodRecipe(result)) {
            return result;
        }

        return ItemStack.EMPTY;
    }

    public static boolean isItemFuel(ItemStack itemStack) {
        if (CookingForBlockheadsConfig.getActive().ovenRequiresCookingOil) {
            return Compat.getCookingOilTag().contains(itemStack.getItem());
        }

        return getBurnTime(itemStack) > 0;
    }

    protected static int getBurnTime(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        if (CookingForBlockheadsConfig.getActive().ovenRequiresCookingOil && Compat.getCookingOilTag().contains(itemStack.getItem())) {
            return 800;
        }

        return Balm.getHooks().getBurnTime(itemStack);
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
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        container.deserialize(tagCompound.getCompound("ItemHandler"));
        furnaceBurnTime = tagCompound.getShort("BurnTime");
        currentItemBurnTime = tagCompound.getShort("CurrentItemBurnTime");
        slotCookTime = tagCompound.getIntArray("CookTimes");

        hasPowerUpgrade = tagCompound.getBoolean("HasPowerUpgrade");
        energyStorage.setEnergy(tagCompound.getInt("EnergyStored"));

        if (tagCompound.contains("CustomName", Tag.TAG_STRING)) {
            customName = Component.Serializer.fromJson(tagCompound.getString("CustomName"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tagCompound) {
        super.save(tagCompound);
        tagCompound.put("ItemHandler", container.serialize());
        tagCompound.putShort("BurnTime", (short) furnaceBurnTime);
        tagCompound.putShort("CurrentItemBurnTime", (short) currentItemBurnTime);
        tagCompound.putIntArray("CookTimes", ArrayUtils.clone(slotCookTime));

        tagCompound.putBoolean("HasPowerUpgrade", hasPowerUpgrade);
        tagCompound.putInt("EnergyStored", energyStorage.getEnergy());

        if (customName != null) {
            tagCompound.putString("CustomName", Component.Serializer.toJson(customName));
        }

        return tagCompound;
    }

    @Override
    public void balmFromClientTag(CompoundTag tag) {
        doorAnimator.setForcedOpen(tag.getBoolean("IsForcedOpen"));
        doorAnimator.setNumPlayersUsing(tag.getByte("NumPlayersUsing"));
    }

    @Override
    public CompoundTag balmToClientTag(CompoundTag tag) {
        tag.putBoolean("IsForcedOpen", doorAnimator.isForcedOpen());
        tag.putByte("NumPlayersUsing", (byte) doorAnimator.getNumPlayersUsing());
        return tag;
    }

    public boolean hasPowerUpgrade() {
        return hasPowerUpgrade;
    }

    public void setHasPowerUpgrade(boolean hasPowerUpgrade) {
        this.hasPowerUpgrade = hasPowerUpgrade;
        BlockState state = level.getBlockState(worldPosition);
        level.setBlockAndUpdate(worldPosition, state.setValue(OvenBlock.POWERED, hasPowerUpgrade));
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
    public ItemStack smeltItem(ItemStack itemStack) {
        return ContainerUtils.insertItemStacked(inputContainer, itemStack, false);
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
        if (side == null) {
            return getContainer();
        }

        return switch (side) {
            case UP -> inputContainer;
            case DOWN -> outputContainer;
            default -> fuelContainer;
        };
    }

    /*@Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return itemHandlerCap.cast();
            }

            if (!CookingForBlockheadsConfigData.COMMON.disallowOvenAutomation.get()) {
                switch (facing) {
                    // TODO
                }
            }
        }

        if (hasPowerUpgrade && capability == CapabilityEnergy.ENERGY) {
            return energyStorageCap.cast();
        }

        if (capability == CapabilityKitchenItemProvider.CAPABILITY) {
            return itemProviderCap.cast();
        }

        if (capability == CapabilityKitchenSmeltingProvider.CAPABILITY) {
            return smeltingProviderCap.cast();
        }

        return super.getCapability(capability, facing);
    }*/

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
    public AABB balmGetRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 1, 2));
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
        return new TranslatableComponent("container.cookingforblockheads.oven");
    }

    @Override
    public Container getContainer() {
        return container;
    }
}