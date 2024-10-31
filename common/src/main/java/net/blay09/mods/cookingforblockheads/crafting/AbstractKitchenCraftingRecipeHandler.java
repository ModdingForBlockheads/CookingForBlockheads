package net.blay09.mods.cookingforblockheads.crafting;

import net.blay09.mods.cookingforblockheads.api.IngredientToken;
import net.blay09.mods.cookingforblockheads.api.KitchenRecipeHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public abstract class AbstractKitchenCraftingRecipeHandler<T extends Recipe<CraftingInput>> implements KitchenRecipeHandler<CraftingInput, T> {
    @Override
    public ItemStack assemble(CraftingContext context, T recipe, List<IngredientToken> ingredientTokens, RegistryAccess registryAccess) {
        final var craftingContainer = new TransientHeadlessCraftingContainer(3, 3);
        for (int i = 0; i < ingredientTokens.size(); i++) {
            final var ingredientToken = ingredientTokens.get(i);
            final var matrixSlot = mapToMatrixSlot(recipe, i);
            craftingContainer.setItem(matrixSlot, ingredientToken.consume());
        }
        return recipe.assemble(craftingContainer.asCraftInput(), registryAccess);
    }
}
