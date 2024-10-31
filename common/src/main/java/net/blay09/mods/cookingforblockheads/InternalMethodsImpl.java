package net.blay09.mods.cookingforblockheads;

import net.blay09.mods.cookingforblockheads.api.*;
import net.blay09.mods.cookingforblockheads.crafting.KitchenImpl;
import net.blay09.mods.cookingforblockheads.registry.CookingForBlockheadsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

public class InternalMethodsImpl implements InternalMethods {

    private FoodStatsProvider foodStatsProvider;

    @Override
    public void addOvenFuel(ItemStack fuelItem, int fuelTime) {
        CookingForBlockheadsRegistry.addOvenFuel(fuelItem, fuelTime);
    }

    @Override
    public Kitchen createKitchen(Level level, BlockPos pos) {
        return new KitchenImpl(level, pos);
    }

    @Override
    public Kitchen createKitchen(ItemStack itemStack) {
        return new KitchenImpl(itemStack);
    }

    @Override
    public void addSortButton(ISortButton button) {
        CookingForBlockheadsRegistry.addSortButton(button);
    }

    @Override
    public void setFoodStatsProvider(FoodStatsProvider foodStatsProvider) {
        this.foodStatsProvider = foodStatsProvider;
    }

    @Override
    public FoodStatsProvider getFoodStatsProvider() {
        return foodStatsProvider;
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> void registerKitchenRecipeHandler(Class<T> recipeClass, KitchenRecipeHandler<C, T> kitchenRecipeHandler) {
        CookingForBlockheadsRegistry.registerKitchenRecipeHandler(recipeClass, kitchenRecipeHandler);
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> KitchenRecipeHandler<C, T> getKitchenRecipeHandler(T recipe) {
        return CookingForBlockheadsRegistry.getKitchenRecipeHandler(recipe);
    }
}
