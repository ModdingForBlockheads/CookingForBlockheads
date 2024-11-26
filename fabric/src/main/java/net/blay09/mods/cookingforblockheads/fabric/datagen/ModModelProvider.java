package net.blay09.mods.cookingforblockheads.fabric.datagen;

import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.blay09.mods.cookingforblockheads.block.*;
import net.blay09.mods.cookingforblockheads.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.minecraft.client.data.models.BlockModelGenerators.createBooleanModelDispatch;
import static net.minecraft.client.data.models.BlockModelGenerators.createHorizontalFacingDispatch;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.cowJar, ModelLocationUtils.getModelLocation(ModBlocks.cowJar.asItem()));


        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.cookingTable);
        for (final var cookingTable : ModBlocks.dyedCookingTables) {
            final var cookingTableParent = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/cooking_table")),
                    Optional.empty(), TextureSlot.ALL, TextureSlot.PARTICLE);
            cookingTableParent.create(cookingTable, TextureMapping.cube(getTerracottaByColor(cookingTable.getColor())), blockStateModelGenerator.modelOutput);
            blockStateModelGenerator.createNonTemplateHorizontalBlock(cookingTable);
        }
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.counter);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.counter, ModelLocationUtils.getModelLocation(ModBlocks.counter.asItem()));
        for (final var counter : ModBlocks.dyedCounters) {
            final var counterParent = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/counter")),
                    Optional.empty(), TextureSlot.ALL, TextureSlot.PARTICLE);
            TextureMapping textureMapping = TextureMapping.cube(getTerracottaByColor(counter.getColor()));
            counterParent.create(counter, textureMapping, blockStateModelGenerator.modelOutput);
            blockStateModelGenerator.createNonTemplateHorizontalBlock(counter);
            blockStateModelGenerator.registerSimpleItemModel(counter, ModelLocationUtils.getModelLocation(counter.asItem()));

            final var counterDoorTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/counter_door")),
                    Optional.empty(), TextureSlot.ALL);
            counterDoorTemplate.createWithSuffix(counter, "_door", textureMapping, blockStateModelGenerator.modelOutput);
            final var counterDoorFlippedTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/counter_door_flipped")),
                    Optional.empty(), TextureSlot.ALL);
            counterDoorFlippedTemplate.createWithSuffix(counter, "_door_flipped", textureMapping, blockStateModelGenerator.modelOutput);
        }
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.cabinet);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.cabinet, ModelLocationUtils.getModelLocation(ModBlocks.cabinet.asItem()));
        for (final var cabinet : ModBlocks.dyedCabinets) {
            final var cabinetParent = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/cabinet")),
                    Optional.empty(), TextureSlot.ALL, TextureSlot.PARTICLE);
            TextureMapping textureMapping = TextureMapping.cube(getTerracottaByColor(cabinet.getColor()));
            cabinetParent.create(cabinet, textureMapping, blockStateModelGenerator.modelOutput);
            blockStateModelGenerator.createNonTemplateHorizontalBlock(cabinet);
            blockStateModelGenerator.registerSimpleItemModel(cabinet, ModelLocationUtils.getModelLocation(cabinet.asItem()));

            final var cabinetDoorTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/cabinet_door")),
                    Optional.empty(), TextureSlot.ALL);
            cabinetDoorTemplate.createWithSuffix(cabinet, "_door", textureMapping, blockStateModelGenerator.modelOutput);
            final var cabinetDoorFlippedTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/cabinet_door_flipped")),
                    Optional.empty(), TextureSlot.ALL);
            cabinetDoorFlippedTemplate.createWithSuffix(cabinet, "_door_flipped", textureMapping, blockStateModelGenerator.modelOutput);
        }

        final var sinkModel = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/sink");
        final var sinkModelFlipped = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/sink_flipped");
        blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.sink)
                .with(createBooleanModelDispatch(SinkBlock.FLIPPED, sinkModelFlipped, sinkModel))
                .with(createHorizontalFacingDispatch()));
        for (final var sink : ModBlocks.dyedSinks) {
            final var sinkParent = new ModelTemplate(Optional.of(sinkModel), Optional.empty(), TextureSlot.ALL, TextureSlot.PARTICLE);
            final var sinkFlippedParent = new ModelTemplate(Optional.of(sinkModelFlipped), Optional.empty(), TextureSlot.ALL, TextureSlot.PARTICLE);
            final var textureMapping = TextureMapping.cube(getTerracottaByColor(sink.getColor()));
            final var dyedSinkModel = sinkParent.create(sink, textureMapping, blockStateModelGenerator.modelOutput);
            final var dyedSinkModelFlipped = sinkFlippedParent.createWithSuffix(sink, "_flipped", textureMapping, blockStateModelGenerator.modelOutput);
            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(sink)
                    .with(createBooleanModelDispatch(SinkBlock.FLIPPED, dyedSinkModelFlipped, dyedSinkModel))
                    .with(createHorizontalFacingDispatch()));
        }
        for (final var oven : ModBlocks.ovens) {
            createOvenBlock(blockStateModelGenerator, oven);
        }
        final var toasterModel = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/toaster");
        final var toasterModelActive = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/toaster_active");
        blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.toaster)
                .with(createBooleanModelDispatch(ToasterBlock.ACTIVE, toasterModelActive, toasterModel))
                .with(createHorizontalFacingDispatch()));

        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.milkJar);
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.cowJar);
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.fruitBasket);
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.cuttingBoard);
        createConnector(blockStateModelGenerator, ModBlocks.connector);
        for (final var connector : ModBlocks.dyedConnectors) {
            createConnector(blockStateModelGenerator, connector);
        }

        final var kitchenFloorParent = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/kitchen_floor")),
                Optional.empty(), TextureSlot.ALL, TextureSlot.PARTICLE);
        for (final var kitchenFloor : ModBlocks.kitchenFloors) {
            kitchenFloorParent.create(kitchenFloor, TextureMapping.cube(kitchenFloor), blockStateModelGenerator.modelOutput);
            blockStateModelGenerator.createNonTemplateModelBlock(kitchenFloor);
        }

        final var fridgeParentSmall = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/fridge")),
                Optional.empty(), TextureSlot.PARTICLE);
        final var fridgeParentLargeLower = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/fridge_large_lower")),
                Optional.empty(), TextureSlot.PARTICLE);
        final var fridgeParentLargeUpper = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "block/fridge_large_upper")),
                Optional.empty(), TextureSlot.PARTICLE);
        for(final var fridge : ModBlocks.fridges) {
            final var textureMapping = getFridgeTextures(fridge);
            final var fridgeModelSmall = fridgeParentSmall.create(fridge, textureMapping, blockStateModelGenerator.modelOutput);
            final var fridgeModelLargeLower = fridgeParentLargeLower.createWithSuffix(fridge, "_large_lower", textureMapping, blockStateModelGenerator.modelOutput);
            final var fridgeModelLargeUpper = fridgeParentLargeUpper.createWithSuffix(fridge, "_large_upper", textureMapping, blockStateModelGenerator.modelOutput);
            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(fridge).with(PropertyDispatch.property(FridgeBlock.MODEL_TYPE)
                    .select(FridgeBlock.FridgeModelType.SMALL, Variant.variant().with(VariantProperties.MODEL, fridgeModelSmall))
                    .select(FridgeBlock.FridgeModelType.LARGE_LOWER, Variant.variant().with(VariantProperties.MODEL, fridgeModelLargeLower))
                    .select(FridgeBlock.FridgeModelType.LARGE_UPPER, Variant.variant().with(VariantProperties.MODEL, fridgeModelLargeUpper))
            ).with(createHorizontalFacingDispatch()));
            blockStateModelGenerator.registerSimpleItemModel(fridge, ModelLocationUtils.getModelLocation(fridge.asItem()));

            final var fridgeDoorTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/fridge_door")), Optional.empty());
            fridgeDoorTemplate.createWithSuffix(fridge, "_door", textureMapping, blockStateModelGenerator.modelOutput);
            final var fridgeDoorFlippedTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/fridge_door_flipped")),
                    Optional.empty());
            fridgeDoorFlippedTemplate.createWithSuffix(fridge, "_door_flipped", textureMapping, blockStateModelGenerator.modelOutput);
            final var fridgeLargeDoorLowerTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/fridge_large_door_lower")),
                    Optional.empty());
            fridgeLargeDoorLowerTemplate.createWithSuffix(fridge, "_large_door_lower", textureMapping, blockStateModelGenerator.modelOutput);
            final var fridgeLargeDoorLowerFlippedTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/fridge_large_door_lower_flipped")),
                    Optional.empty());
            fridgeLargeDoorLowerFlippedTemplate.createWithSuffix(fridge, "_large_door_lower_flipped", textureMapping, blockStateModelGenerator.modelOutput);
            final var fridgeLargeDoorUpperTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/fridge_large_door_upper")),
                    Optional.empty());
            fridgeLargeDoorUpperTemplate.createWithSuffix(fridge, "_large_door_upper", textureMapping, blockStateModelGenerator.modelOutput);
            final var fridgeLargeDoorUpperFlippedTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/fridge_large_door_upper_flipped")),
                    Optional.empty());
            fridgeLargeDoorUpperFlippedTemplate.createWithSuffix(fridge, "_large_door_upper_flipped", textureMapping, blockStateModelGenerator.modelOutput);
        }

        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.toolRack);
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.spiceRack);

        final var ovenTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "item/oven")), Optional.empty());
        for (final var oven : ModBlocks.ovens) {
            final var modelLocation = ModelLocationUtils.getModelLocation(oven.asItem());
            final var textureMapping = getOvenTextures(oven, false);
            ovenTemplate.create(modelLocation, textureMapping, blockStateModelGenerator.modelOutput);
        }

        final var fridgeTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "item/fridge")), Optional.empty());
        for (final var fridge : ModBlocks.fridges) {
            final var modelLocation = ModelLocationUtils.getModelLocation(fridge.asItem());
            fridgeTemplate.create(modelLocation, getFridgeTextures(fridge), blockStateModelGenerator.modelOutput);
        }

        final var counterTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "item/counter")),
                Optional.empty(),
                TextureSlot.ALL);
        for (final var counter : ModBlocks.dyedCounters) {
            final var color = counter.getColor();
            if (color == null) {
                continue;
            }

            final var modelLocation = ModelLocationUtils.getModelLocation(counter.asItem());
            final var textureMapping = TextureMapping.cube(getTerracottaByColor(color));
            counterTemplate.create(modelLocation, textureMapping, blockStateModelGenerator.modelOutput);
        }

        final var cabinetTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "item/cabinet")),
                Optional.empty(),
                TextureSlot.ALL);
        for (final var cabinet : ModBlocks.dyedCabinets) {
            final var color = cabinet.getColor();
            if (color == null) {
                continue;
            }

            final var modelLocation = ModelLocationUtils.getModelLocation(cabinet.asItem());
            final var textureMapping = TextureMapping.cube(getTerracottaByColor(color));
            cabinetTemplate.create(modelLocation, textureMapping, blockStateModelGenerator.modelOutput);
        }
    }

    private Block getTerracottaByColor(@Nullable DyeColor color) {
        if (color == null) {
            return Blocks.TERRACOTTA;
        }
        return switch (color) {
            case WHITE -> Blocks.WHITE_TERRACOTTA;
            case ORANGE -> Blocks.ORANGE_TERRACOTTA;
            case MAGENTA -> Blocks.MAGENTA_TERRACOTTA;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_TERRACOTTA;
            case YELLOW -> Blocks.YELLOW_TERRACOTTA;
            case LIME -> Blocks.LIME_TERRACOTTA;
            case PINK -> Blocks.PINK_TERRACOTTA;
            case GRAY -> Blocks.GRAY_TERRACOTTA;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_TERRACOTTA;
            case CYAN -> Blocks.CYAN_TERRACOTTA;
            case PURPLE -> Blocks.PURPLE_TERRACOTTA;
            case BLUE -> Blocks.BLUE_TERRACOTTA;
            case BROWN -> Blocks.BROWN_TERRACOTTA;
            case GREEN -> Blocks.GREEN_TERRACOTTA;
            case RED -> Blocks.RED_TERRACOTTA;
            case BLACK -> Blocks.BLACK_TERRACOTTA;
        };
    }

    private void createConnector(BlockModelGenerators blockStateModelGenerator, Block block) {
        final var innerModelBottomTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads",
                "block/connector_inner_bottom_template")),
                Optional.of("_inner_bottom"));
        final var straightModelBottomTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads",
                "block/connector_straight_bottom_template")),
                Optional.of("_straight_bottom"));
        final var outerModelBottomTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads",
                "block/connector_outer_bottom_template")),
                Optional.of("_outer_bottom"));
        final var innerModelTopTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/connector_inner_top_template")),
                Optional.of("_inner_top"));
        final var straightModelTopTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads",
                "block/connector_straight_top_template")),
                Optional.of("_straight_top"));
        final var outerModelTopTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/connector_outer_top_template")),
                Optional.of("_outer_top"));

        final var textures = new TextureMapping();
        if (block instanceof DyedConnectorBlock dyedConnectorBlock) {
            final var color = dyedConnectorBlock.getColor();
            textures.putForced(TextureSlot.PARTICLE, ResourceLocation.withDefaultNamespace( "block/" + color.getSerializedName() + "_terracotta"));
            textures.putForced(TextureSlot.TEXTURE, ResourceLocation.withDefaultNamespace( "block/" + color.getSerializedName() + "_terracotta"));
        }

        final var straightModelBottom = straightModelBottomTemplate.create(block, textures, blockStateModelGenerator.modelOutput);
        blockStateModelGenerator.blockStateOutput.accept(createStairLike(block,
                innerModelBottomTemplate.create(block, textures, blockStateModelGenerator.modelOutput),
                straightModelBottom,
                outerModelBottomTemplate.create(block, textures, blockStateModelGenerator.modelOutput),
                innerModelTopTemplate.create(block, textures, blockStateModelGenerator.modelOutput),
                straightModelTopTemplate.create(block, textures, blockStateModelGenerator.modelOutput),
                outerModelTopTemplate.create(block, textures, blockStateModelGenerator.modelOutput)));
        blockStateModelGenerator.registerSimpleItemModel(block, straightModelBottom);
    }

    public static BlockStateGenerator createStairLike(Block block, ResourceLocation innerModelBottom, ResourceLocation straightModelBottom, ResourceLocation outerModelBottom, ResourceLocation innerModelTop, ResourceLocation straightModelTop, ResourceLocation outerModelTop) {
        return MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                        .select(Direction.EAST,
                                Half.BOTTOM,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.BOTTOM,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.BOTTOM,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.BOTTOM,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, Variant.variant().with(VariantProperties.MODEL, outerModelBottom))
                        .select(Direction.WEST,
                                Half.BOTTOM,
                                StairsShape.OUTER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.BOTTOM,
                                StairsShape.OUTER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.BOTTOM,
                                StairsShape.OUTER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST,
                                Half.BOTTOM,
                                StairsShape.OUTER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.BOTTOM,
                                StairsShape.OUTER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, Variant.variant().with(VariantProperties.MODEL, outerModelBottom))
                        .select(Direction.NORTH,
                                Half.BOTTOM,
                                StairsShape.OUTER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, Variant.variant().with(VariantProperties.MODEL, innerModelBottom))
                        .select(Direction.WEST,
                                Half.BOTTOM,
                                StairsShape.INNER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.BOTTOM,
                                StairsShape.INNER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.BOTTOM,
                                StairsShape.INNER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST,
                                Half.BOTTOM,
                                StairsShape.INNER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.BOTTOM,
                                StairsShape.INNER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, Variant.variant().with(VariantProperties.MODEL, innerModelBottom))
                        .select(Direction.NORTH,
                                Half.BOTTOM,
                                StairsShape.INNER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelBottom)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST,
                                Half.TOP,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.TOP,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.TOP,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.TOP,
                                StairsShape.STRAIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, straightModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST,
                                Half.TOP,
                                StairsShape.OUTER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.TOP,
                                StairsShape.OUTER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.TOP,
                                StairsShape.OUTER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.TOP,
                                StairsShape.OUTER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST,
                                Half.TOP,
                                StairsShape.OUTER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.TOP,
                                StairsShape.OUTER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.TOP,
                                StairsShape.OUTER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.TOP,
                                StairsShape.OUTER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, outerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST,
                                Half.TOP,
                                StairsShape.INNER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.TOP,
                                StairsShape.INNER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.TOP,
                                StairsShape.INNER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.TOP,
                                StairsShape.INNER_RIGHT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.EAST,
                                Half.TOP,
                                StairsShape.INNER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.WEST,
                                Half.TOP,
                                StairsShape.INNER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.SOUTH,
                                Half.TOP,
                                StairsShape.INNER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.UV_LOCK, true))
                        .select(Direction.NORTH,
                                Half.TOP,
                                StairsShape.INNER_LEFT,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, innerModelTop)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        .with(VariantProperties.UV_LOCK, true)));
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(ModItems.recipeBook, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.craftingBook, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.noFilterBook, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.heatingUnit, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.iceUnit, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.preservationChamber, ModelTemplates.FLAT_ITEM);
    }

    private void createOvenBlock(BlockModelGenerators blockStateModelGenerator, OvenBlock block) {
        final var ovenTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/oven")), Optional.empty());
        final var textureMapping = getOvenTextures(block, false);
        final var ovenModel = ovenTemplate.create(block, textureMapping, blockStateModelGenerator.modelOutput);
        final var activeTextureMapping = getOvenTextures(block, true);
        final var activeOvenModel = ovenTemplate.createWithSuffix(block, "_active", activeTextureMapping, blockStateModelGenerator.modelOutput);
        blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(createBooleanModelDispatch(OvenBlock.ACTIVE, ovenModel, activeOvenModel))
                .with(createHorizontalFacingDispatch()));
        blockStateModelGenerator.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block.asItem()));

        final var ovenDoorTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/oven_door")), Optional.empty());
        ovenDoorTemplate.createWithSuffix(block, "_door", textureMapping, blockStateModelGenerator.modelOutput);
        final var ovenDoorActiveTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/oven_door_active")),
                Optional.empty());
        ovenDoorActiveTemplate.createWithSuffix(block, "_door_active", activeTextureMapping, blockStateModelGenerator.modelOutput);
        final var ovenDoorHandleTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/oven_door_handle")),
                Optional.empty());
        ovenDoorHandleTemplate.createWithSuffix(block, "_door_handle", activeTextureMapping, blockStateModelGenerator.modelOutput);
    }

    private static TextureMapping getOvenTextures(OvenBlock oven, boolean active) {
        final var textureMapping = new TextureMapping();
        final var colorName = oven.getColor().getName();
        textureMapping.putForced(TextureSlot.PARTICLE, ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_oven_side"));
        textureMapping.putForced(TextureSlot.TEXTURE, ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_oven_side"));
        if (active) {
            textureMapping.putForced(TextureSlot.create("ovenfront"),
                    ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + oven.getColor().getName() + "_oven_front_active"));
        } else {
            textureMapping.putForced(TextureSlot.create("ovenfront"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_oven_front"));
        }
        textureMapping.putForced(TextureSlot.create("ovenfront_active"),
                ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_oven_front_active"));
        textureMapping.putForced(TextureSlot.create("oventop"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_oven_top"));
        textureMapping.putForced(TextureSlot.create("ovenbottom"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_oven_bottom"));
        textureMapping.putForced(TextureSlot.create("backsplash"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_oven_side"));
        return textureMapping;
    }

    private static TextureMapping getFridgeTextures(FridgeBlock fridge) {
        final var textureMapping = new TextureMapping();
        final var colorName = fridge.getColor().getName();
        textureMapping.put(TextureSlot.PARTICLE, ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_fridge_side"));
        textureMapping.putForced(TextureSlot.create("fridge_back"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_fridge_back"));
        textureMapping.putForced(TextureSlot.create("fridge_top"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_fridge_top"));
        textureMapping.putForced(TextureSlot.create("fridge_side"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_fridge_side"));
        textureMapping.putForced(TextureSlot.create("fridge_inside"), ResourceLocation.fromNamespaceAndPath("cookingforblockheads", "block/" + colorName + "_fridge_inside"));
        return textureMapping;
    }

}
