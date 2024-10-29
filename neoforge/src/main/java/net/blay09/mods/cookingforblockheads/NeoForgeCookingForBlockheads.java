package net.blay09.mods.cookingforblockheads;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.neoforge.NeoForgeLoadContext;
import net.blay09.mods.balm.neoforge.energy.NeoForgeEnergyStorage;
import net.blay09.mods.balm.neoforge.fluid.NeoForgeFluidTank;
import net.blay09.mods.balm.neoforge.provider.NeoForgeBalmProviders;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProcessor;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.event.OvenItemSmeltedEvent;
import net.blay09.mods.cookingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.cookingforblockheads.compat.Compat;
import net.blay09.mods.cookingforblockheads.compat.TheOneProbeAddon;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(CookingForBlockheads.MOD_ID)
public class NeoForgeCookingForBlockheads {

    private static final BlockCapability<KitchenItemProvider, Void> KITCHEN_ITEM_PROVIDER = BlockCapability.createVoid(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID,
            "kitchen_item_provider"), KitchenItemProvider.class);
    private static final BlockCapability<KitchenItemProcessor, Void> KITCHEN_ITEM_PROCESSOR = BlockCapability.createVoid(ResourceLocation.fromNamespaceAndPath(
            CookingForBlockheads.MOD_ID,
            "kitchen_item_processor"), KitchenItemProcessor.class);

    public NeoForgeCookingForBlockheads(IEventBus eventBus) {
        Balm.getEvents().onEvent(OvenItemSmeltedEvent.class, orig -> {
            PlayerEvent.ItemSmeltedEvent event = new PlayerEvent.ItemSmeltedEvent(orig.getPlayer(), orig.getResultItem());
            NeoForge.EVENT_BUS.post(event);
        });

        final var context = new NeoForgeLoadContext(eventBus);
        Balm.initialize(CookingForBlockheads.MOD_ID, context, CookingForBlockheads::initialize);

        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(this::enqueueIMC);

        final var providers = ((NeoForgeBalmProviders) Balm.getProviders());
        providers.registerBlockProvider(KitchenItemProvider.class, KITCHEN_ITEM_PROVIDER);
        providers.registerBlockProvider(KitchenItemProcessor.class, KITCHEN_ITEM_PROCESSOR);
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        if (Balm.isModLoaded(Compat.THEONEPROBE)) {
            TheOneProbeAddon.register();
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(KITCHEN_ITEM_PROVIDER, ModBlockEntities.milkJar.get(), (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));
        event.registerBlockEntity(KITCHEN_ITEM_PROVIDER, ModBlockEntities.cowJar.get(), (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));
        event.registerBlockEntity(KITCHEN_ITEM_PROVIDER, ModBlockEntities.fridge.get(), (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));
        event.registerBlockEntity(KITCHEN_ITEM_PROVIDER, ModBlockEntities.sink.get(), (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.sink.get(), (blockEntity, context) -> new NeoForgeFluidTank(blockEntity.getFluidTank()));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.milkJar.get(), (blockEntity, context) -> new NeoForgeFluidTank(blockEntity.getFluidTank()));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.cowJar.get(), (blockEntity, context) -> new NeoForgeFluidTank(blockEntity.getFluidTank()));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.oven.get(), (blockEntity, context) -> new NeoForgeEnergyStorage(blockEntity.getEnergyStorage()));
        event.registerBlockEntity(KITCHEN_ITEM_PROVIDER, ModBlockEntities.cuttingBoard.get(), (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));

        event.registerBlockEntity(KITCHEN_ITEM_PROCESSOR, ModBlockEntities.oven.get(), (blockEntity, context) -> blockEntity.getProvider(KitchenItemProcessor.class));
    }
}
