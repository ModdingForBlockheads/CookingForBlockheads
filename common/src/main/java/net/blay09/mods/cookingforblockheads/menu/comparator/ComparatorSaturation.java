package net.blay09.mods.cookingforblockheads.menu.comparator;

import net.blay09.mods.cookingforblockheads.api.CookingForBlockheadsAPI;
import net.blay09.mods.cookingforblockheads.crafting.RecipeWithStatus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.Comparator;

public class ComparatorSaturation implements Comparator<RecipeWithStatus> {

    private final ComparatorName fallback;
    private final Player player;

    public ComparatorSaturation(Player player) {
        this.fallback = new ComparatorName(player);
        this.player = player;
    }

    @Override
    public int compare(RecipeWithStatus o1, RecipeWithStatus o2) {
        final var contextMap = SlotDisplayContext.fromLevel(player.level());
        final var firstItem = o1.recipeDisplayEntry().display().result().resolveForFirstStack(contextMap);
        final var secondItem = o2.recipeDisplayEntry().display().result().resolveForFirstStack(contextMap);
        boolean isFirstFood = firstItem.has(DataComponents.FOOD);
        boolean isSecondFood = secondItem.has(DataComponents.FOOD);
        if (!isFirstFood && !isSecondFood) {
            return fallback.compare(o1, o2);
        } else if (!isFirstFood) {
            return 1;
        } else if (!isSecondFood) {
            return -1;
        }

        final var foodStatsProvider = CookingForBlockheadsAPI.getFoodStatsProvider();
        int result = (int) (foodStatsProvider.getSaturationModifier(secondItem, player) * 100
                - foodStatsProvider.getSaturationModifier(firstItem, player) * 100);
        if (result == 0) {
            return fallback.compare(o1, o2);
        }

        return result;
    }

}
