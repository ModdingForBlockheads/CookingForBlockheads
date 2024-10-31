package net.blay09.mods.cookingforblockheads.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

import java.lang.reflect.InvocationTargetException;

public class CookingForBlockheadsAPI {

    private static final InternalMethods internalMethods = loadInternalMethods();

    private static InternalMethods loadInternalMethods() {
        try {
            return (InternalMethods) Class.forName("net.blay09.mods.cookingforblockheads.InternalMethodsImpl").getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Failed to load Crafting for Blockheads API", e);
        }
    }

    public static void setFoodStatsProvider(FoodStatsProvider foodStatsProvider) {
        internalMethods.setFoodStatsProvider(foodStatsProvider);
    }

    public static FoodStatsProvider getFoodStatsProvider() {
        return internalMethods.getFoodStatsProvider();
    }

    public static void addOvenFuel(ItemStack fuelItem, int fuelTime) {
        internalMethods.addOvenFuel(fuelItem, fuelTime);
    }

    public static Kitchen createKitchen(Level level, BlockPos pos) {
        return internalMethods.createKitchen(level, pos);
    }

    public static Kitchen createKitchen(ItemStack itemStack) {
        return internalMethods.createKitchen(itemStack);
    }

    public static <C extends RecipeInput, T extends Recipe<C>> void registerKitchenRecipeHandler(Class<T> recipeClass, KitchenRecipeHandler<C, T> kitchenRecipeHandler) {
        internalMethods.registerKitchenRecipeHandler(recipeClass, kitchenRecipeHandler);
    }

    public static <C extends RecipeInput, T extends Recipe<C>> KitchenRecipeHandler<C, T> getKitchenRecipeHandler(T recipe) {
        return internalMethods.getKitchenRecipeHandler(recipe);
    }

    public static void addSortButton(ISortButton button) {
        internalMethods.addSortButton(button);
    }
}
