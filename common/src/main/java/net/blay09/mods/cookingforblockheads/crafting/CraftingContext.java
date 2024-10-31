package net.blay09.mods.cookingforblockheads.crafting;

import net.blay09.mods.cookingforblockheads.api.CacheHint;
import net.blay09.mods.cookingforblockheads.api.Kitchen;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProcessor;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingContext {

    private final List<KitchenItemProvider> itemProviders;
    private final List<KitchenItemProcessor> itemProcessors;
    private final Map<List<Holder<Item>>, Integer> cachedProviderIndexByIngredient = new HashMap<>();
    private final Map<CraftingOperation.IngredientTokenKey, CacheHint> cacheHintsByIngredient = new HashMap<>();

    public CraftingContext(final Kitchen kitchen, final @Nullable Player player) {
        itemProviders = kitchen.getItemProviders(player);
        itemProcessors = kitchen.getItemProcessors();
    }

    public <C extends RecipeInput, T extends Recipe<C>> CraftingOperation<C, T> createOperation(RecipeHolder<T> recipe) {
        return new CraftingOperation<>(this, recipe );
    }

    public List<KitchenItemProvider> getItemProviders() {
        return itemProviders;
    }

    public List<KitchenItemProcessor> getItemProcessors() {
        return itemProcessors;
    }

    public int getCachedItemProviderIndexFor(Ingredient ingredient) {
        return cachedProviderIndexByIngredient.getOrDefault(ingredient.items(), -1);
    }

    public CacheHint getCacheHintFor(CraftingOperation.IngredientTokenKey ingredientTokenKey) {
        return cacheHintsByIngredient.getOrDefault(ingredientTokenKey, CacheHint.NONE);
    }

    public void cache(CraftingOperation.IngredientTokenKey ingredientTokenKey, int itemProviderIndex, CacheHint cacheHint) {
        cacheHintsByIngredient.put(ingredientTokenKey, cacheHint);
        cachedProviderIndexByIngredient.put(ingredientTokenKey.items(), itemProviderIndex);
    }
}
