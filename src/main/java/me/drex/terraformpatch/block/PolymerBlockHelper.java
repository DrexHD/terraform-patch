package me.drex.terraformpatch.block;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import eu.pb4.factorytools.api.block.model.SignModel;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.BlockStateAsset;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import me.drex.terraformpatch.TerraformerPatch;
import me.drex.terraformpatch.block.mod.*;
import me.drex.terraformpatch.block.type.*;
import me.drex.terraformpatch.res.ResourceCollector;
import me.drex.terraformpatch.res.ResourcePackGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Supplier;

public class PolymerBlockHelper {
    private static final Map<String, Supplier<ModPolymerBlockHelper>> MOD_HELPERS = Map.of(
        "traverse", () -> TraversePolymerBlockHelper.INSTANCE,
        "terrestria", () -> TerrestriaPolymerBlockHelper.INSTANCE,
        "cinderscapes", () -> CinderscapesPolymerBlockHelper.INSTANCE,
        "woods_and_mires", () -> WoodsAndMiresPolymerBlockHelper.INSTANCE
    );

    public static void registerPolymerBlock(ResourceLocation id, Block block) {
        BlockStateModelManager.addBlock(id, block);
        PolymerBlock polymerBlock = requestPolymerBlock(id, block);
        PolymerBlock.registerOverlay(block, polymerBlock);
        if (polymerBlock instanceof BlockWithElementHolder blockWithElementHolder) {
            BlockWithElementHolder.registerOverlay(block, blockWithElementHolder);
        }

        if (block instanceof SignBlock) {
            SignModel.setModel(block, id.withPrefix("block_sign/"));
        }
        if (block.getClass().equals(StandingSignBlock.class)) {
            ResourcePackGenerator.SIGNS.add(id.withPath(id.getPath().replace("_sign", "")));
        }
    }

    public static PolymerBlock requestPolymerBlock(ResourceLocation id, Block block) {
        Supplier<ModPolymerBlockHelper> modPolymerBlockHelper = MOD_HELPERS.get(id.getNamespace());
        if (modPolymerBlockHelper != null) {
            PolymerBlock polymerBlock = modPolymerBlockHelper.get().requestPolymerBlock(id, block);
            if (polymerBlock != null) {
                return polymerBlock;
            }
        }

        var polymerBlock = switch (block) {
            case WallBlock ignored -> StateCopyFactoryBlock.WALL;
            case StairBlock ignored -> StateCopyFactoryBlock.STAIR;
            case FenceBlock ignored -> StateCopyFactoryBlock.FENCE;
            case FenceGateBlock ignored -> StateCopyFactoryBlock.FENCE_GATE;
            case ButtonBlock ignored -> StateCopyFactoryBlock.BUTTON;
            case PressurePlateBlock ignored -> StateCopyFactoryBlock.PRESSURE_PLATE;
            case WallSignBlock ignored -> StateCopyFactoryBlock.WALL_SIGN;
            case WallHangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_WALL_SIGN;
            case CeilingHangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_SIGN;
            case StandingSignBlock ignored -> StateCopyFactoryBlock.SIGN;
            case DoorBlock ignored -> DoorPolymerBlock.INSTANCE;
            case TrapDoorBlock ignored -> TrapdoorPolymerBlock.INSTANCE;
            case SlabBlock ignored -> SlabPolymerBlock.INSTANCE;
            case FlowerPotBlock ignored -> BaseFactoryBlock.POT;
            case LeavesBlock ignored -> StatePolymerBlock.of(block, BlockModelType.BIOME_TRANSPARENT_BLOCK);
            case SnowyDirtBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case RotatedPillarBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case ColoredFallingBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case FarmBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FARMLAND_BLOCK);
            case VegetationBlock ignored -> BaseFactoryBlock.PLANT;
            default -> {
                if (block.getClass().equals(Block.class)) {
                    yield StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
                } else {
                    TerraformerPatch.LOGGER.warn("Missing overlay for block: '{}' {}", id, block.getClass().getName());
                    yield null;
                }
            }
        };
        if (polymerBlock == null) {
            if (block.defaultBlockState().getCollisionShape(PolymerCommonUtils.getFakeWorld(), BlockPos.ZERO).isEmpty()) {
                polymerBlock = BaseFactoryBlock.PLANT;
            } else {
                polymerBlock = BaseFactoryBlock.BARRIER;
            }
        }
        return polymerBlock;
    }

    public static BlockState requestPolymerBlockState(ResourceLocation id, String variant, BlockModelType blockModelType) throws IOException {
        IoSupplier<InputStream> supplier = ResourceCollector.GLOBAL_ASSETS.getAsset(id.getNamespace(), "blockstates/" + id.getPath() + ".json");
        BlockStateAsset decoded = BlockStateAsset.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(new JsonReader(new InputStreamReader(supplier.get())))).getOrThrow().getFirst();

        var model = decoded.variants().get().get(variant);

        return PolymerBlockResourceUtils.requestBlock(
            blockModelType,
            model.stream().map(x -> new PolymerBlockModel(x.model(), x.x(), x.y(), x.uvlock(), x.weigth())).toArray(PolymerBlockModel[]::new));
    }
}
