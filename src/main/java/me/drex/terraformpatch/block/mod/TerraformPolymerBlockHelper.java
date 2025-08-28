package me.drex.terraformpatch.block.mod;

import com.terraformersmc.terraform.dirt.api.block.TerraformDirtPathBlock;
import com.terraformersmc.terraform.leaves.api.block.LeafPileBlock;
import com.terraformersmc.terraform.wood.api.block.QuarterLogBlock;
import com.terraformersmc.terraform.wood.api.block.SmallLogBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import me.drex.terraformpatch.block.type.BaseFactoryBlock;
import me.drex.terraformpatch.block.type.StatePolymerBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class TerraformPolymerBlockHelper implements ModPolymerBlockHelper {
    public static final TerraformPolymerBlockHelper INSTANCE = new TerraformPolymerBlockHelper();

    @Override
    public @Nullable PolymerBlock requestPolymerBlock(ResourceLocation id, Block block) {
        return switch (block) {
            case SmallLogBlock ignored -> BaseFactoryBlock.CACTUS;
            case QuarterLogBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case TerraformDirtPathBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FARMLAND_BLOCK);
            case LeafPileBlock ignored -> StatePolymerBlock.of(block, BlockModelType.TRIPWIRE_BLOCK_FLAT);
            default -> null;
        };
    }
}
