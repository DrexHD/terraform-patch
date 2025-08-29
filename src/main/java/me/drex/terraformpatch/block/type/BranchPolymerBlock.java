package me.drex.terraformpatch.block.type;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModel;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import juuxel.woodsandmires.block.BranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public record BranchPolymerBlock() implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {

    public static final BranchPolymerBlock INSTANCE = new BranchPolymerBlock();

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        BlockState clientState = Blocks.LIGHTNING_ROD.defaultBlockState();
        Direction.Axis axis = blockState.getValue(BranchBlock.AXIS);
        if (axis == Direction.Axis.X) {
            clientState = clientState.setValue(LightningRodBlock.FACING, Direction.EAST);
        } else if (axis == Direction.Axis.Z) {
            clientState = clientState.setValue(LightningRodBlock.FACING, Direction.SOUTH);
        }
        return clientState.setValue(LightningRodBlock.WATERLOGGED, blockState.getValue(BranchBlock.WATERLOGGED));
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return BlockStateModel.midRange(initialBlockState, pos);
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
