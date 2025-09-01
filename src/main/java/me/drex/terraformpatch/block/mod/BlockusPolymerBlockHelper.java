package me.drex.terraformpatch.block.mod;

import com.brand.blockus.blocks.base.*;
import com.brand.blockus.blocks.base.asphalt.AsphaltBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import me.drex.terraformpatch.block.type.*;
import me.drex.terraformpatch.res.ResourcePackGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import org.jetbrains.annotations.Nullable;

public class BlockusPolymerBlockHelper implements ModPolymerBlockHelper {
    public static final BlockusPolymerBlockHelper INSTANCE = new BlockusPolymerBlockHelper();

    @Override
    public @Nullable PolymerBlock requestPolymerBlock(ResourceLocation id, Block block) {

        return switch (id.getPath()) {
            case "golden_bars" -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.BARS);
            default -> switch (block) {
                case IronBarsBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.PANE);
                case ChainBlock ignored -> ResourcePackGenerator.expandBlockModel(id, StateCopyFactoryBlock.CHAIN);
                case PostBlock ignored -> ResourcePackGenerator.expandBlockModel(id, PostPolymerBlock.INSTANCE);
                case WeatheringCopperFullBlock ignored -> BaseFactoryBlock.BARRIER;
                case StainedGlassBlock ignored -> BaseFactoryBlock.BARRIER;
                case ColoredTilesBlock ignored -> BaseFactoryBlock.BARRIER;
                case RotatedPillarBlock ignored -> BaseFactoryBlock.BARRIER;
                case SmallHedgeBlock ignored -> BaseFactoryBlock.BARRIER;
                case Barrier ignored -> BaseFactoryBlock.BARRIER;
                case OrientableBlockBase ignored -> BaseFactoryBlock.BARRIER;
                case RedstoneLampBlock ignored -> BaseFactoryBlock.BARRIER;
                case LargeFlowerPotBlock ignored -> BaseFactoryBlock.BARRIER;
                case PaperLampBlock ignored -> BaseFactoryBlock.BARRIER;
                case FullFacingBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.FULL_BLOCK);
                case AmethystBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.FULL_BLOCK);
                case AsphaltBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.FULL_BLOCK);
                default -> null;
            };
        };
    }
}
