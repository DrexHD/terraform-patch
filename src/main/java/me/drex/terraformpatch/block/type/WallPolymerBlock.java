package me.drex.terraformpatch.block.type;

import com.brand.blockus.blocks.base.SmallHedgeBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.model.generic.ShiftyBlockStateModel;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Function;

public record WallPolymerBlock<T extends Comparable<T>>(
    Property<T> north, Property<T> east,
    Property<T> south, Property<T> west,
    Function<T, Boolean> hasWall
) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {

    public static final WallPolymerBlock<WallSide> BARRIER = new WallPolymerBlock<>(
        BlockStateProperties.NORTH_WALL, BlockStateProperties.EAST_WALL,
        BlockStateProperties.SOUTH_WALL, BlockStateProperties.WEST_WALL,
        value -> value != WallSide.NONE
    );

    public static final WallPolymerBlock<Boolean> HEDGE = new WallPolymerBlock<>(
        SmallHedgeBlock.NORTH, SmallHedgeBlock.EAST,
        SmallHedgeBlock.SOUTH, SmallHedgeBlock.WEST,
        value -> value
    );

    private static final BlockState eastWest = PolymerBlockResourceUtils.requestEmpty(BlockModelType.EAST_WEST_GATE);
    private static final BlockState northSouth = PolymerBlockResourceUtils.requestEmpty(BlockModelType.NORTH_SOUTH_GATE);

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        Boolean north = blockState.getOptionalValue(north()).map(hasWall).orElse(false);
        Boolean east = blockState.getOptionalValue(east()).map(hasWall).orElse(false);
        Boolean south = blockState.getOptionalValue(south()).map(hasWall).orElse(false);
        Boolean west = blockState.getOptionalValue(west()).map(hasWall).orElse(false);
        if (east && west) {
            return northSouth;
        } else if (north && south) {
            return eastWest;
        }
        if (east || west) {
            return northSouth;
        } else if (north || south) {
            return eastWest;
        }

        return northSouth;
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
