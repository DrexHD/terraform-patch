package me.drex.terraformpatch.block.type;

import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.BlockStateAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.StateModelVariant;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import me.drex.terraformpatch.TerraformerPatch;
import me.drex.terraformpatch.res.ResourceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.function.Predicate;

public record StatePolymerBlock(Map<BlockState, BlockState> map,
                                FactoryBlock fallback) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {

    public static StatePolymerBlock of(ResourceLocation id, Block block, BlockModelType type) {
        return of(id, block, type, BaseFactoryBlock.BARRIER);
    }

    public static StatePolymerBlock of(ResourceLocation id, Block block, BlockModelType type, FactoryBlock fallback) {
        return of(id, block, type, fallback, x -> true);
    }

    public static StatePolymerBlock of(ResourceLocation id, Block block, BlockModelType type, FactoryBlock fallback, Predicate<BlockState> canUseBlock) {
        try {
            BlockStateAsset blockStateAsset = ResourceHelper.decodeBlockState(id);

            var list = new ArrayList<Tuple<BlockStatePredicate, List<StateModelVariant>>>();
            var cache = new HashMap<List<StateModelVariant>, BlockState>();


            BlockStateModelManager.parseVariants(block, blockStateAsset.variants().orElseThrow(), (a, b) -> list.add(new Tuple<>(a, b)));
            var map = new IdentityHashMap<BlockState, BlockState>();

            for (var state : block.getStateDefinition().getPossibleStates()) {
                for (var pair : list) {
                    if (pair.getA().test(state) && canUseBlock.test(state)) {
                        map.put(state, cache.computeIfAbsent(pair.getB(), c -> PolymerBlockResourceUtils.requestBlock(
                            type,
                            c.stream().map(x -> new PolymerBlockModel(x.model(), x.x(), x.y(), x.uvlock(), x.weigth())).toArray(PolymerBlockModel[]::new))));
                        break;
                    }
                }
            }
            TerraformerPatch.LOGGER.debug("{} uses block states {} from {}", id, cache.size(), type);
            return new StatePolymerBlock(map, fallback);
        } catch (Throwable e) {
            TerraformerPatch.LOGGER.error("Failed to handle state block {}", id, e);
            return null;
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        var val = map.get(blockState);
        return val != null ? val : fallback.getPolymerBlockState(blockState, packetContext);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return map.get(initialBlockState) != null ? null : fallback.createElementHolder(world, pos, initialBlockState);
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
