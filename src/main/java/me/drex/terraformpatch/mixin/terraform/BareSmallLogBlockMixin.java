package me.drex.terraformpatch.mixin.terraform;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.terraform.wood.api.block.BareSmallLogBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BareSmallLogBlock.class)
public abstract class BareSmallLogBlockMixin {
    @Shadow
    @Final
    public static EnumProperty<Direction.Axis> AXIS;

    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    public BlockState setAxis(BlockState original, @Local(argsOnly = true) BlockPlaceContext context) {
        return original.setValue(AXIS, context.getClickedFace().getAxis());
    }
}
