package me.drex.terraformpatch.block.type;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

public record SingleStatePolymerBlock(BlockState blockState,
                                      FactoryBlock fallback) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {

    public static SingleStatePolymerBlock of(ResourceLocation id, Block block, BlockModelType type) {
        return of(id, block, type, BaseFactoryBlock.BARRIER);
    }

    public static SingleStatePolymerBlock of(ResourceLocation id, Block block, BlockModelType type, FactoryBlock fallback) {
        try {
            BlockStateAsset blockStateAsset = ResourceHelper.decodeBlockState(id);
            Map<String, List<StateModelVariant>> variants = blockStateAsset.variants().orElseThrow();

            var model = Objects.requireNonNullElseGet(variants.get(""), () -> variants.values().iterator().next());

            return new SingleStatePolymerBlock(PolymerBlockResourceUtils.requestBlock(
                type,
                model.stream().map(x -> new PolymerBlockModel(x.model(), x.x(), x.y(), x.uvlock(), x.weigth())).toArray(PolymerBlockModel[]::new)), fallback);
        } catch (Throwable e) {
            TerraformerPatch.LOGGER.error("Failed to handle single state block {}: {}", id, e);
            return null;
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return this.blockState != null ? this.blockState : fallback.getPolymerBlockState(blockState, packetContext);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return this.blockState != null ? null : fallback.createElementHolder(world, pos, initialBlockState);
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
