package me.drex.terraformpatch.block.mod;

import com.terraformersmc.terrestria.block.CattailBlock;
import com.terraformersmc.terrestria.block.SaguaroCactusBlock;
import com.terraformersmc.terrestria.block.TallCattailBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import me.drex.terraformpatch.block.type.BaseFactoryBlock;
import me.drex.terraformpatch.block.type.StatePolymerBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

// TODO andisol_grass_block
public class TerrestriaPolymerBlockHelper implements ModPolymerBlockHelper {
    public static final TerrestriaPolymerBlockHelper INSTANCE = new TerrestriaPolymerBlockHelper();

    @Override
    public @Nullable PolymerBlock requestPolymerBlock(ResourceLocation id, Block block) {
        PolymerBlock polymerBlock = TerraformPolymerBlockHelper.INSTANCE.requestPolymerBlock(id, block);
        if (polymerBlock != null) return polymerBlock;

        return switch (block) {
            case SaguaroCactusBlock ignored -> BaseFactoryBlock.CACTUS;
            case CattailBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.KELP_BLOCK);
            case TallCattailBlock ignored -> StatePolymerBlock.of(id, block, BlockModelType.KELP_BLOCK);
            default -> null;
        };
    }
}
