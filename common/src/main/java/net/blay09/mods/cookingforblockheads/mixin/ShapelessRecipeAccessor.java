package net.blay09.mods.cookingforblockheads.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ShapelessRecipe.class)
public interface ShapelessRecipeAccessor {
    @Accessor
    List<Ingredient> getIngredients();

    @Accessor
    ItemStack getResult();
}
