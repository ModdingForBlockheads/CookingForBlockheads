package net.blay09.mods.cookingforblockheads.menu.comparator;

import net.blay09.mods.cookingforblockheads.api.CookingForBlockheadsAPI;
import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

public class ComparatorSaturation implements Comparator<CraftableWithStatus> {

    private final ComparatorName fallback;
    private final Player player;

    public ComparatorSaturation(Player player) {
        this.fallback = new ComparatorName();
        this.player = player;
    }

    @Override
    public int compare(CraftableWithStatus o1, CraftableWithStatus o2) {
        boolean isFirstFood = o1.itemStack().has(DataComponents.FOOD);
        boolean isSecondFood = o2.itemStack().has(DataComponents.FOOD);
        if (!isFirstFood && !isSecondFood) {
            return fallback.compare(o1, o2);
        } else if (!isFirstFood) {
            return 1;
        } else if (!isSecondFood) {
            return -1;
        }

        final var foodStatsProvider = CookingForBlockheadsAPI.getFoodStatsProvider();
        int result = (int) (foodStatsProvider.getSaturationModifier(o2.itemStack(), player) * 100
                - foodStatsProvider.getSaturationModifier(o1.itemStack(), player) * 100);
        if (result == 0) {
            return fallback.compare(o1, o2);
        }

        return result;
    }

}
