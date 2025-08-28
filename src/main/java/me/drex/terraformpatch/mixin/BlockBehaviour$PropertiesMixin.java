package me.drex.terraformpatch.mixin;

import me.drex.terraformpatch.TerraformerPatch;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockBehaviour$PropertiesMixin {
    @Shadow
    public abstract BlockBehaviour.Properties noOcclusion();

    @Inject(
        method = "setId",
        at = @At("RETURN")
    )
    public void disableOcclusion(ResourceKey<Block> resourceKey, CallbackInfoReturnable<BlockBehaviour.Properties> cir) {
        ResourceLocation id = resourceKey.location();
        if (!TerraformerPatch.MOD_NAMESPACES.contains(id.getNamespace())) {
            return;
        }
        this.noOcclusion();
    }
}
