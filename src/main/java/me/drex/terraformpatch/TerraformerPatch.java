package me.drex.terraformpatch;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.impl.DefaultModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.MapColorTintSource;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import me.drex.terraformpatch.res.ResourceCollector;
import me.drex.terraformpatch.res.ResourcePackGenerator;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static net.minecraft.commands.Commands.literal;

public class TerraformerPatch implements ModInitializer {
    public static final String MOD_ID = "terraformer-polymer-patch";

    public static final Set<String> MOD_NAMESPACES = Set.of("terrestria", "cinderscapes", "traverse", "woods_and_mires");
    public static final Set<String> MOD_ASSET_IDS = Set.of("terrestria", "cinderscapes", "traverse", "woods_and_mires", "terraform");

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(ResourceCollector::init);
        ResourcePackGenerator.setup();

        for (String modNamespace : MOD_NAMESPACES) {
            setupModAssets(modNamespace);
        }

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            CommandRegistrationCallback.EVENT.register((dispatcher, a, b) -> {
                dispatcher.register(literal("count_displays").executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    var map = new Reference2IntOpenHashMap<Block>();
                    for (var holder : ((HolderHolder) player.connection).polymer$getHolders()) {
                        if (holder.getAttachment() instanceof BlockAwareAttachment attachment) {
                            map.put(attachment.getBlockState().getBlock(), map.getInt(attachment.getBlockState().getBlock()) + 1);
                        }
                    }
                    var entries = new ArrayList<>(map.reference2IntEntrySet());
                    entries.sort(Comparator.comparing(Reference2IntMap.Entry::getIntValue));
                    for (var entry : entries.reversed()) {
                        player.sendSystemMessage(Component.literal(BuiltInRegistries.BLOCK.getKey(entry.getKey()) + " -> " + entry.getIntValue()));
                    }

                    return 0;
                }));
                dispatcher.register(literal("count_model_types").executes(ctx -> {
                    for (var entry : BlockModelType.values()) {
                        ctx.getSource().sendSystemMessage(Component.literal(entry.name() + " -> " + PolymerBlockResourceUtils.getBlocksLeft(entry) + " / " + DefaultModelData.USABLE_STATES.get(entry).size()));
                    }

                    return 0;
                }));
            });
        }
    }


    public static void setupModAssets(String modid) {
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
            ResourceLocation.fromNamespaceAndPath(modid, "block"),
            ResourceLocation.fromNamespaceAndPath(modid, "block_sign")
        );
        ResourcePackExtras.forDefault().addBridgedModelsFolder(ResourceLocation.fromNamespaceAndPath(modid, "entity"), (id, b) -> {
            return new ItemAsset(new BasicItemModel(id, List.of(new MapColorTintSource(0xFFFFFF))), new ItemAsset.Properties(true, true));
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}
