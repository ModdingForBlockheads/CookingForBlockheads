package net.blay09.mods.cookingforblockheads.network.message;

import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class SyncedEffectMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncedEffectMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
            CookingForBlockheads.MOD_ID,
            "synced_effect"));

    public enum Type {
        COW_IN_A_JAR,
        OVEN_UPGRADE,
        FRIDGE_UPGRADE
    }

    private final BlockPos pos;
    private final Type type;

    public SyncedEffectMessage(BlockPos pos, Type type) {
        this.pos = pos;
        this.type = type;
    }

    public static void encode(FriendlyByteBuf buf, SyncedEffectMessage message) {
        buf.writeLong(message.pos.asLong());
        buf.writeByte(message.type.ordinal());
    }

    public static SyncedEffectMessage decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Type type = Type.values()[buf.readByte()];
        return new SyncedEffectMessage(pos, type);
    }

    public static void handle(Player player, SyncedEffectMessage message) {
        SimpleParticleType particleType = ParticleTypes.EXPLOSION;
        int particleCount = 1;
        float particleRandomOffset = 1f;
        Vec3i particleOffset = Vec3i.ZERO;
        SoundEvent soundEvent = null;
        float volume = 1f;
        switch (message.type) {
            case COW_IN_A_JAR -> {
                soundEvent = SoundEvents.CHICKEN_EGG;
                particleOffset = new Vec3i(0, 1, 0);
            }
            case OVEN_UPGRADE, FRIDGE_UPGRADE -> {
                soundEvent = SoundEvents.ANVIL_USE;
                particleType = ParticleTypes.LARGE_SMOKE;
                particleCount = 10;
                volume = 0.5f;
            }
        }

        if (soundEvent != null) {
            Minecraft.getInstance().level.playLocalSound(message.pos.getX(),
                    message.pos.getY(),
                    message.pos.getZ(),
                    soundEvent,
                    SoundSource.BLOCKS,
                    volume,
                    1f,
                    false);
        }

        for (int i = 0; i < particleCount; i++) {
            float offsetX = particleOffset.getX() + 0.5f + (float) (Math.random() - 0.5f) * particleRandomOffset;
            float offsetY = particleOffset.getX() + 0.5f + (float) (Math.random() - 0.5f) * particleRandomOffset;
            float offsetZ = particleOffset.getX() + 0.5f + (float) (Math.random() - 0.5f) * particleRandomOffset;
            Minecraft.getInstance().level.addParticle(particleType,
                    true,
                    false,
                    (double) message.pos.getX() + offsetX,
                    (double) message.pos.getY() + offsetY,
                    (double) message.pos.getZ() + offsetZ,
                    0.0,
                    0.0,
                    0.0);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
