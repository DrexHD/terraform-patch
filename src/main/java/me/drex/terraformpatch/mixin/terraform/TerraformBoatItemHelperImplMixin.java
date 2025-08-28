package me.drex.terraformpatch.mixin.terraform;

import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.terraform.boat.impl.item.TerraformBoatItemHelperImpl;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import me.drex.terraformpatch.entity.PolyBaseEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(TerraformBoatItemHelperImpl.class)
public abstract class TerraformBoatItemHelperImplMixin {
    @Inject(method = "registerBoat", at = @At("RETURN"))
    private static <T extends AbstractBoat> void polymerifyBoats(
        ResourceLocation id, ResourceKey<Item> itemKey, ResourceKey<EntityType<?>> entityTypeKey,
        Item.Properties settings, Function<Supplier<Item>, EntityType.EntityFactory<T>> factory,
        BiConsumer<ResourceLocation, EntityType<T>> registry, CallbackInfoReturnable<BoatItem> cir,
        @Local EntityType<?> entityType, @Local BoatItem item
    ) {
        ResourceLocation location = entityTypeKey.location();
        var vanillaType = location.getPath().contains("_chest")? EntityType.BIRCH_CHEST_BOAT : EntityType.BIRCH_BOAT;
        PolymerEntityUtils.registerOverlay(entityType, object -> new PolyBaseEntity(vanillaType));
    }
}
