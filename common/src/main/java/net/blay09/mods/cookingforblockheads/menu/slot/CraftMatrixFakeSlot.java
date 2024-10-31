package net.blay09.mods.cookingforblockheads.menu.slot;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.cookingforblockheads.menu.KitchenMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.Comparator;

public class CraftMatrixFakeSlot extends AbstractFakeSlot {

    private static final float ITEM_SWITCH_TIME = 40f;

    private final NonNullList<ItemStack> visibleStacks = NonNullList.create();
    private final KitchenMenu menu;

    private int ingredientIndex;
    private SlotDisplay slotDisplay;
    private float variantTimePassed;
    private int currentVariantIndex;
    private boolean isLocked;
    private boolean missing = true;

    public CraftMatrixFakeSlot(KitchenMenu menu, Container container, int slotId, int x, int y) {
        super(container, slotId, x, y);
        this.menu = menu;
    }

    public void setIngredient(final int ingredientIndex, final SlotDisplay slotDisplay, final ItemStack lockedInput) {
        this.ingredientIndex = ingredientIndex;

        final var previousIngredient = this.slotDisplay;
        var effectiveLockedInput = isLocked ? getItem() : ItemStack.EMPTY;
        if (!lockedInput.isEmpty()) {
            effectiveLockedInput = lockedInput;
        }
        visibleStacks.clear();
        this.slotDisplay = slotDisplay;
        final var itemStacks = slotDisplay.resolveForStacks(SlotDisplayContext.fromLevel(menu.player.level()));
        for (final var itemStack : itemStacks) {
            if (!itemStack.isEmpty()) {
                visibleStacks.add(itemStack.copyWithCount(1));
            }
        }
        visibleStacks.sort(Comparator.comparing(it -> Balm.getRegistries().getKey(it.getItem()).toString()));

        variantTimePassed = 0;
        if (previousIngredient != slotDisplay) {
            currentVariantIndex = 0;
        } else {
            currentVariantIndex = !visibleStacks.isEmpty() ? currentVariantIndex % visibleStacks.size() : 0;
        }
        isLocked = false;

        if (!effectiveLockedInput.isEmpty()) {
            for (int i = 0; i < visibleStacks.size(); i++) {
                if (ItemStack.isSameItemSameComponents(visibleStacks.get(i), effectiveLockedInput)) {
                    currentVariantIndex = i;
                    isLocked = true;
                }
            }
        }
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }

    public boolean isMissing() {
        return missing;
    }

    public void updateSlot(float partialTicks) {
        if (!isLocked) {
            variantTimePassed += partialTicks;
            if (variantTimePassed >= ITEM_SWITCH_TIME) {
                currentVariantIndex++;
                if (currentVariantIndex >= visibleStacks.size()) {
                    currentVariantIndex = 0;
                }
                variantTimePassed = 0;
            }
        }
    }

    @Override
    public ItemStack getItem() {
        return !visibleStacks.isEmpty() ? visibleStacks.get(currentVariantIndex) : ItemStack.EMPTY;
    }

    @Override
    public boolean hasItem() {
        return !visibleStacks.isEmpty();
    }

    @Override
    public boolean isActive() {
        return !visibleStacks.isEmpty();
    }

    public NonNullList<ItemStack> getVisibleStacks() {
        return visibleStacks;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public ItemStack scrollDisplayListAndLock(int i) {
        isLocked = true;
        currentVariantIndex += i;
        if (currentVariantIndex >= visibleStacks.size()) {
            currentVariantIndex = 0;
        } else if (currentVariantIndex < 0) {
            currentVariantIndex = visibleStacks.size() - 1;
        }
        return visibleStacks.get(currentVariantIndex);
    }

    public ItemStack toggleLock() {
        isLocked = !isLocked;
        return isLocked ? getItem() : ItemStack.EMPTY;
    }

    public int getIngredientIndex() {
        return ingredientIndex;
    }
}
