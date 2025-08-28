package me.drex.terraformpatch.res;

import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import me.drex.terraformpatch.TerraformerPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import nl.theepicblock.resourcelocatorapi.ResourceLocatorApi;
import nl.theepicblock.resourcelocatorapi.api.AssetContainer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ResourceCollector {
    public static final AssetContainer GLOBAL_ASSETS = ResourceLocatorApi.createGlobalAssetContainer();

    public static void init(ResourcePackBuilder packBuilder) {
        GLOBAL_ASSETS.locateFiles("").forEach(tuple -> {
            ResourceLocation id = tuple.getA();
            if (!TerraformerPatch.MOD_ASSET_IDS.contains(id.getNamespace())) return;
            IoSupplier<InputStream> ioSupplier = tuple.getB();

            try {
                byte[] data = IOUtils.toByteArray(ioSupplier.get());
                packBuilder.addData("assets/" + id.getNamespace() + "/" + id.getPath(), data);
            } catch (IOException e) {
                TerraformerPatch.LOGGER.error("Failed to read resource {}: {}", id, e);
            }
        });
    }
}
