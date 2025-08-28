package me.drex.terraformpatch.block.mod;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface ModPolymerBlockHelper {
    @Nullable
    PolymerBlock requestPolymerBlock(ResourceLocation id, Block block);
}
