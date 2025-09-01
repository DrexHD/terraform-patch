package me.drex.terraformpatch.block.mod;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import me.drex.terraformpatch.block.type.StatePolymerBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class TraversePolymerBlockHelper implements ModPolymerBlockHelper {
    public static final TraversePolymerBlockHelper INSTANCE = new TraversePolymerBlockHelper();

    @Override
    public @Nullable PolymerBlock requestPolymerBlock(ResourceLocation id, Block block) {
        PolymerBlock polymerBlock = TerraformPolymerBlockHelper.INSTANCE.requestPolymerBlock(id, block);
        if (polymerBlock != null) return polymerBlock;

        if (id.getPath().contains("autumnal_leaves")) {
            return StatePolymerBlock.of(id, block, BlockModelType.TRANSPARENT_BLOCK);
        }
        return null;
    }
}
