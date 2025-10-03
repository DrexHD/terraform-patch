package me.drex.terraformpatch.block.type;

import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.ShiftyBlockStateModel;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import me.drex.terraformpatch.TerraformerPatch;
import me.drex.terraformpatch.block.PolymerBlockHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.IOException;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

public record SlabPolymerBlock(BlockState bottomState, BlockState bottomStateWaterlogged) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {
    public static SlabPolymerBlock of(ResourceLocation id) {
        try {
            BlockState bottom = PolymerBlockHelper.requestPolymerBlockState(id, "type=bottom", BlockModelType.SCULK_SENSOR_BLOCK);
            BlockState bottomWaterlogged = PolymerBlockHelper.requestPolymerBlockState(id, "type=bottom", BlockModelType.SCULK_SENSOR_BLOCK_WATERLOGGED);
            return new SlabPolymerBlock(bottom, bottomWaterlogged);
        } catch (IOException e) {
            TerraformerPatch.LOGGER.error("Failed to handle slab block {}", id, e);
            return null;
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        SlabType slabType = state.getValue(SlabBlock.TYPE);
        if (slabType == SlabType.BOTTOM) {
            return state.getValue(SlabBlock.WATERLOGGED) ? bottomStateWaterlogged : bottomState;
        }

        return slabType != SlabType.DOUBLE ? Blocks.SANDSTONE_SLAB.withPropertiesOf(state) : Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        SlabType slabType = initialBlockState.getValue(SlabBlock.TYPE);
        if (slabType == SlabType.BOTTOM) {
            return null;
        }
        return ShiftyBlockStateModel.longRange(initialBlockState, pos);
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
