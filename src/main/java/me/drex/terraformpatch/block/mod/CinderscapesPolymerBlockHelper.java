package me.drex.terraformpatch.block.mod;

import com.terraformersmc.cinderscapes.block.*;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import me.drex.terraformpatch.block.type.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VegetationBlock;
import org.jetbrains.annotations.Nullable;

public class CinderscapesPolymerBlockHelper implements ModPolymerBlockHelper {
    public static final CinderscapesPolymerBlockHelper INSTANCE = new CinderscapesPolymerBlockHelper();

    @Override
    public @Nullable PolymerBlock requestPolymerBlock(ResourceLocation id, Block block) {
        PolymerBlock polymerBlock = TerraformPolymerBlockHelper.INSTANCE.requestPolymerBlock(id, block);
        if (polymerBlock != null) return polymerBlock;

        return switch (block) {
            // This is not great, but everything else seems worse
            // Could be improved by using textured blockstates for layers 1 and 8, but models for 2-7
            // but the holders should only be created when necessary
            case AshLayerBlock ignored -> LayerPolymerBlock.of(block);
            case VegetationBlock ignored -> StatePolymerBlock.of(block, BlockModelType.TRIPWIRE_BLOCK, BaseFactoryBlock.PLANT);
            case GhastlyEctoplasmBlock ignored -> BaseFactoryBlock.PLANT;
            case PolypiteQuartzBlock ignored -> PolypiteQuartzBlockPolymerBlock.INSTANCE;
            case CinderscapesTransparentBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case CinderscapesNyliumBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case CinderscapesOreBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case LeavesBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            default -> null;
        };
    }
}
