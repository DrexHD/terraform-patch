package me.drex.terraformpatch.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import me.drex.terraformpatch.TerraformerPatch;
import me.drex.terraformpatch.block.PolymerBlockHelper;
import me.drex.terraformpatch.item.PolyBaseItem;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public interface RegistryMixin {
    @WrapWithCondition(
        method = "register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;)Ljava/lang/Object;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"
        )
    )
    private static <V, T extends V> boolean dontRegisterCreateModeTabs(WritableRegistry<T> instance, ResourceKey<T> resourceKey, T t, RegistrationInfo registrationInfo) {
        ResourceLocation id = resourceKey.location();
        if (!TerraformerPatch.MOD_NAMESPACES.contains(id.getNamespace())) {
            return true;
        }
        var registry = (Object) instance;
        if (registry == BuiltInRegistries.CREATIVE_MODE_TAB) {
            return false;
        }
        return true;
    }

    @Inject(
        method = "register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;)Ljava/lang/Object;",
        at = @At("TAIL")
    )
    private static <V, T extends V> void polymerifyEntries(Registry<V> registry, ResourceKey<V> resourceKey, T object, CallbackInfoReturnable<T> cir) {
        ResourceLocation id = resourceKey.location();
        if (!TerraformerPatch.MOD_NAMESPACES.contains(id.getNamespace())) {
            return;
        }
        if (registry == BuiltInRegistries.ITEM) {
            Item item = (Item) object;
            PolymerItem.registerOverlay(item, new PolyBaseItem(item));
        } else if (registry == BuiltInRegistries.BLOCK) {
            PolymerBlockHelper.registerPolymerBlock(id, (Block) object);
        } else if (registry == BuiltInRegistries.SOUND_EVENT) {
            PolymerSoundEvent.registerOverlay((SoundEvent) object);
        } else if (registry == BuiltInRegistries.CREATIVE_MODE_TAB) {
            PolymerItemGroupUtils.registerPolymerItemGroup(id, (CreativeModeTab) object);
        }
    }
}
