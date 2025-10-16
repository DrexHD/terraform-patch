package me.drex.terraformpatch.block.type;

import com.brand.blockus.blocks.base.PostBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.model.generic.ShiftyBlockStateModel;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

public record PostPolymerBlock() implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {
    public static final PostPolymerBlock INSTANCE = new PostPolymerBlock();

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        BlockState blockState = Blocks.LIGHTNING_ROD.defaultBlockState();
        Direction.Axis axis = state.getValue(PostBlock.AXIS);
        var direction = switch (axis) {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            default -> Direction.NORTH;
        };
        return blockState.setValue(LightningRodBlock.FACING, direction).setValue(LightningRodBlock.WATERLOGGED, state.getValue(SlabBlock.WATERLOGGED));
    }

    @Override
    public @NotNull ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return ShiftyBlockStateModel.midRange(initialBlockState, pos);
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
