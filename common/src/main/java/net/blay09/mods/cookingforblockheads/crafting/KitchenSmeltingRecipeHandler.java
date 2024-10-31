package net.blay09.mods.cookingforblockheads.crafting;

import net.blay09.mods.cookingforblockheads.api.IngredientToken;
import net.blay09.mods.cookingforblockheads.api.KitchenRecipeHandler;
import net.blay09.mods.cookingforblockheads.mixin.SingleItemRecipeAccessor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.List;
import java.util.Optional;

public class KitchenSmeltingRecipeHandler implements KitchenRecipeHandler<SingleRecipeInput, SmeltingRecipe> {
    @Override
    public int mapToMatrixSlot(SmeltingRecipe recipe, int ingredientIndex) {
        return 4;
    }

    @Override
    public List<Optional<Ingredient>> getIngredients(SmeltingRecipe recipe) {
        return List.of(Optional.of(recipe.input()));
    }

    @Override
    public ItemStack assemble(CraftingContext context, SmeltingRecipe recipe, List<IngredientToken> ingredientTokens, RegistryAccess registryAccess) {
        for (final var itemProcessor : context.getItemProcessors()) {
            if (itemProcessor.canProcess(recipe.getType())) {
                itemProcessor.processRecipe(recipe, ingredientTokens);
                return ItemStack.EMPTY;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack predictResultItem(SmeltingRecipe recipe) {
        return ((SingleItemRecipeAccessor) recipe).getResult();
    }
}
