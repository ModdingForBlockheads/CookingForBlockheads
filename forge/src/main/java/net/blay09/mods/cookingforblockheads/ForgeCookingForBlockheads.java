package net.blay09.mods.cookingforblockheads;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.forge.ForgeLoadContext;
import net.blay09.mods.balm.forge.provider.ForgeBalmProviders;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProcessor;
import net.blay09.mods.cookingforblockheads.api.event.OvenItemSmeltedEvent;
import net.blay09.mods.cookingforblockheads.client.CookingForBlockheadsClient;
import net.blay09.mods.cookingforblockheads.compat.Compat;
import net.blay09.mods.cookingforblockheads.compat.TheOneProbeAddon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(CookingForBlockheads.MOD_ID)
public class ForgeCookingForBlockheads {

    public static Capability<KitchenItemProvider> KITCHEN_ITEM_PROVIDER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Capability<KitchenItemProcessor> KITCHEN_ITEM_PROCESSOR_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public ForgeCookingForBlockheads(FMLJavaModLoadingContext context) {
        final var loadContext = new ForgeLoadContext(context.getModEventBus());
        Balm.getEvents().onEvent(OvenItemSmeltedEvent.class, orig -> {
            PlayerEvent.ItemSmeltedEvent event = new PlayerEvent.ItemSmeltedEvent(orig.getPlayer(), orig.getResultItem());
            MinecraftForge.EVENT_BUS.post(event);
        });

        Balm.initialize(CookingForBlockheads.MOD_ID, loadContext, CookingForBlockheads::initialize);
        if (FMLEnvironment.dist.isClient()) {
            BalmClient.initialize(CookingForBlockheads.MOD_ID, loadContext, CookingForBlockheadsClient::initialize);
        }

        context.getModEventBus().addListener(this::registerCapabilities);
        context.getModEventBus().addListener(this::enqueueIMC);

        ForgeBalmProviders providers = (ForgeBalmProviders) Balm.getProviders();
        providers.register(KitchenItemProvider.class, new CapabilityToken<>() {
        });
        providers.register(KitchenItemProcessor.class, new CapabilityToken<>() {
        });
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        if (Balm.isModLoaded(Compat.THEONEPROBE)) {
            TheOneProbeAddon.register();
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(KitchenItemProvider.class);
        event.register(KitchenItemProcessor.class);
    }
}
