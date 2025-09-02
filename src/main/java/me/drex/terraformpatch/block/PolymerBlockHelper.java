package me.drex.terraformpatch.block;

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
import me.drex.terraformpatch.res.ResourceHelper;
import me.drex.terraformpatch.res.ResourcePackGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class PolymerBlockHelper {
    private static final Map<String, Supplier<ModPolymerBlockHelper>> MOD_HELPERS = Map.of(
        "blockus", () -> BlockusPolymerBlockHelper.INSTANCE,
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
            case WallBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.WALL);
            case StairBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.STAIR);
            case FenceBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.FENCE);
            case FenceGateBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.FENCE_GATE);
            case ButtonBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.BUTTON);
            case BasePressurePlateBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.PRESSURE_PLATE);
            case CarpetBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.CARPET);
            case WallSignBlock ignored -> StateCopyFactoryBlock.WALL_SIGN;
            case WallHangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_WALL_SIGN;
            case CeilingHangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_SIGN;
            case StandingSignBlock ignored -> StateCopyFactoryBlock.SIGN;
            case DoorBlock ignored -> DoorPolymerBlock.INSTANCE;
            case TrapDoorBlock ignored -> TrapdoorPolymerBlock.INSTANCE;
            case SlabBlock ignored -> ResourcePackGenerator.expandBlockModel(id, SlabPolymerBlock.INSTANCE);
            case FlowerPotBlock ignored -> ResourcePackGenerator.expandBlockModel(id, BaseFactoryBlock.POT);
            case WaterloggedTransparentBlock ignored -> BaseFactoryBlock.BARRIER;
            case LeavesBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.BIOME_TRANSPARENT_BLOCK);
            case SnowyDirtBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.FULL_BLOCK);
            case ColoredFallingBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.FULL_BLOCK);
            case FarmBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.FARMLAND_BLOCK);
            case VegetationBlock ignored -> BaseFactoryBlock.PLANT;
            default -> {
                if (block.getClass().equals(Block.class)) {
                    // TODO log debug
                    yield StatePolymerBlock.of(id, block, BlockModelType.FULL_BLOCK);
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
        BlockStateAsset blockStateAsset = ResourceHelper.decodeBlockState(id);

        var model = blockStateAsset.variants().get().get(variant);

        return PolymerBlockResourceUtils.requestBlock(
            blockModelType,
            model.stream().map(x -> new PolymerBlockModel(x.model(), x.x(), x.y(), x.uvlock(), x.weigth())).toArray(PolymerBlockModel[]::new));
    }
}
