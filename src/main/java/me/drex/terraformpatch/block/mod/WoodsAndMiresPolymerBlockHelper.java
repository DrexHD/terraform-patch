package me.drex.terraformpatch.block.mod;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import juuxel.woodsandmires.block.BranchBlock;
import juuxel.woodsandmires.block.ShrubLogBlock;
import me.drex.terraformpatch.block.type.BaseFactoryBlock;
import me.drex.terraformpatch.block.type.BranchPolymerBlock;
import me.drex.terraformpatch.block.type.StatePolymerBlock;
import me.drex.terraformpatch.res.ResourcePackGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import org.jetbrains.annotations.Nullable;

public class WoodsAndMiresPolymerBlockHelper implements ModPolymerBlockHelper {
    public static final WoodsAndMiresPolymerBlockHelper INSTANCE = new WoodsAndMiresPolymerBlockHelper();

    @Override
    public @Nullable PolymerBlock requestPolymerBlock(ResourceLocation id, Block block) {
        return switch (id.getPath()) {
            case "fireweed", "tansy" -> StatePolymerBlock.of(id, block, BlockModelType.BIOME_PLANT_BLOCK, BaseFactoryBlock.PLANT);
            default -> switch (block) {
                case ShrubLogBlock ignored -> BaseFactoryBlock.BARRIER;
                case RotatedPillarBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.FULL_BLOCK);
                case BranchBlock ignored -> ResourcePackGenerator.expandBlockModel(id, BranchPolymerBlock.INSTANCE);
                default -> null;
            };
        };
    }
}
