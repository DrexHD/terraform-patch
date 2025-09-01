package me.drex.terraformpatch.block.type;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import me.drex.terraformpatch.TerraformerPatch;
import me.drex.terraformpatch.block.PolymerBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

// layer 1 and 8 are very common in world generation and shouldn't use display entities
public record LayerPolymerBlock(BlockState layer1, BlockState layer8) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {

    public static LayerPolymerBlock of(ResourceLocation id, Block block) {
        try {
            BlockState layer1 = PolymerBlockHelper.requestPolymerBlockState(id, "layers=1", BlockModelType.TRIPWIRE_BLOCK_FLAT);
            BlockState layer8 = PolymerBlockHelper.requestPolymerBlockState(id, "layers=8", BlockModelType.FULL_BLOCK);

            return new LayerPolymerBlock(layer1, layer8);
        } catch (Throwable e) {
            TerraformerPatch.LOGGER.error("Failed to handle layered block {}", id, e);
            return null;
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
//        int layers = blockState.getValue(BlockStateProperties.LAYERS);
//        if (layers == 1) return layer1;
//        if (layers >= 8) return layer8;
//        return Blocks.SNOW.withPropertiesOf(blockState);
        int layers = blockState.getValue(BlockStateProperties.LAYERS);
        if (layers == 1) return layer1;
        else return layer8;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
//        return ConditionalBlockStateModel.midRange(initialBlockState, pos, blockState -> blockState.getValue(BlockStateProperties.LAYERS) > 1 && blockState.getValue(BlockStateProperties.LAYERS) < 8);
        return null;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
