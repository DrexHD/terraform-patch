package me.drex.terraformpatch.block.type;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModel;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.BiFunction;
import java.util.function.Function;

public record BaseFactoryBlock(Function<BlockState, BlockState> blockStateFunction, boolean tick,
                               BiFunction<BlockState, BlockPos, BlockModel> modelFunction) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {
    public BaseFactoryBlock(BlockState clientState, boolean tick,
                            BiFunction<BlockState, BlockPos, BlockModel> modelFunction) {
        this(state -> clientState, tick, modelFunction);
    }

    public static final BaseFactoryBlock BARRIER = new BaseFactoryBlock(blockState -> Blocks.BARRIER.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, blockState.getValueOrElse(BlockStateProperties.WATERLOGGED, false)), false, BlockStateModel::longRange);
    public static final BaseFactoryBlock POT = new BaseFactoryBlock(Blocks.FLOWER_POT.defaultBlockState(), false, BlockStateModel::midRange);
    public static final BaseFactoryBlock PLANT = new BaseFactoryBlock(PolymerBlockResourceUtils.requestEmpty(BlockModelType.PLANT_BLOCK), false, BlockStateModel::midRange);
    public static final BaseFactoryBlock CACTUS = new BaseFactoryBlock(PolymerBlockResourceUtils.requestEmpty(BlockModelType.CACTUS_BLOCK), false, BlockStateModel::midRange);

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return blockStateFunction.apply(state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return this.modelFunction.apply(initialBlockState, pos);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return this.tick;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
