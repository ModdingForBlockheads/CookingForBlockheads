package net.blay09.mods.cookingforblockheads.kitchen;

import net.blay09.mods.cookingforblockheads.api.CacheHint;
import net.blay09.mods.cookingforblockheads.api.IngredientToken;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collection;
import java.util.List;

public record CombinedKitchenItemProvider(List<KitchenItemProvider> providers) implements KitchenItemProvider {

    private record IngredientTokenWrapper(int providerIndex, IngredientToken token) implements IngredientToken {
        @Override
        public ItemStack peek() {
            return token.peek();
        }

        @Override
        public ItemStack consume() {
            return token.consume();
        }

        @Override
        public ItemStack restore(ItemStack itemStack) {
            return token.restore(itemStack);
        }
    }

    private record CacheHintWrapper(int providerIndex, CacheHint cacheHint) implements CacheHint {
    }

    @Override
    public IngredientToken findIngredient(Ingredient ingredient, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
        if (cacheHint instanceof CacheHintWrapper wrapper) {
            final var provider = providers.get(wrapper.providerIndex);
            final var filteredIngredientTokens = getFilteredIngredientTokens(ingredientTokens, wrapper.providerIndex);
            final var token = provider.findIngredient(ingredient, filteredIngredientTokens, wrapper.cacheHint);
            if (token != null) {
                return new IngredientTokenWrapper(wrapper.providerIndex, token);
            }
            return null;
        }

        for (int i = 0; i < providers.size(); i++) {
            final var provider = providers.get(i);
            final var filteredIngredientTokens = getFilteredIngredientTokens(ingredientTokens, i);
            final var unwrappedCacheHint = cacheHint instanceof CacheHintWrapper wrapper ? wrapper.cacheHint : cacheHint;
            final var token = provider.findIngredient(ingredient, filteredIngredientTokens, unwrappedCacheHint);
            if (token != null) {
                return new IngredientTokenWrapper(i, token);
            }
        }
        return null;
    }

    @Override
    public IngredientToken findIngredient(ItemStack itemStack, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
        if (cacheHint instanceof CacheHintWrapper wrapper) {
            final var provider = providers.get(wrapper.providerIndex);
            final var token = provider.findIngredient(itemStack, ingredientTokens, wrapper.cacheHint);
            if (token != null) {
                return new IngredientTokenWrapper(wrapper.providerIndex, token);
            }
            return null;
        }

        for (int i = 0; i < providers.size(); i++) {
            final var provider = providers.get(i);
            final var filteredIngredientTokens = getFilteredIngredientTokens(ingredientTokens, i);
            final var unwrappedCacheHint = cacheHint instanceof CacheHintWrapper wrapper ? wrapper.cacheHint : cacheHint;
            final var token = provider.findIngredient(itemStack, filteredIngredientTokens, unwrappedCacheHint);
            if (token != null) {
                return new IngredientTokenWrapper(i, token);
            }
        }
        return null;
    }

    @Override
    public CacheHint getCacheHint(IngredientToken ingredientToken) {
        if (ingredientToken instanceof IngredientTokenWrapper wrapper) {
            final var provider = providers.get(wrapper.providerIndex);
            final var cacheHint = provider.getCacheHint(wrapper.token);
            return new CacheHintWrapper(wrapper.providerIndex, cacheHint);
        }

        return CacheHint.NONE;
    }

    private Collection<IngredientToken> getFilteredIngredientTokens(Collection<IngredientToken> ingredientTokens, int providerIndex) {
        return ingredientTokens.stream()
                .filter(ingredientToken -> (ingredientToken instanceof IngredientTokenWrapper wrapper && wrapper.providerIndex == providerIndex))
                .map(ingredientToken -> ((IngredientTokenWrapper) ingredientToken).token)
                .toList();
    }
}
