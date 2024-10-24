package net.blay09.mods.cookingforblockheads.crafting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record CraftableWithStatus(ItemStack itemStack, boolean missingIngredients, boolean missingUtensils) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftableWithStatus> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            CraftableWithStatus::itemStack,
            ByteBufCodecs.BOOL,
            CraftableWithStatus::missingIngredients,
            ByteBufCodecs.BOOL,
            CraftableWithStatus::missingUtensils,
            CraftableWithStatus::new
    );

    public static CraftableWithStatus best(@Nullable CraftableWithStatus first, @Nullable CraftableWithStatus second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        }

        if (!first.missingIngredients && second.missingIngredients) {
            return first;
        } else if (!second.missingIngredients && first.missingIngredients) {
            return second;
        } else if (!first.missingUtensils && second.missingUtensils) {
            return first;
        } else if (!second.missingUtensils && first.missingUtensils) {
            return second;
        }

        return first;
    }
}
