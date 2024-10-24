package net.blay09.mods.cookingforblockheads.menu.comparator;

import net.blay09.mods.cookingforblockheads.crafting.CraftableWithStatus;

import java.util.Comparator;

public class ComparatorName implements Comparator<CraftableWithStatus> {

    @Override
    public int compare(CraftableWithStatus o1, CraftableWithStatus o2) {
        String s1 = o1.itemStack().getDisplayName().getString();
        String s2 = o2.itemStack().getDisplayName().getString();
        return s1.compareToIgnoreCase(s2);
    }

}
