package me.drex.terraformpatch.res;


import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.factorytools.api.resourcepack.ModelModifiers;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.BlockStateAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.StateModelVariant;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.StateMultiPartDefinition;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelElement;
import me.drex.terraformpatch.TerraformerPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

import static me.drex.terraformpatch.TerraformerPatch.id;

public class ResourcePackGenerator {
    private static final Vec3 EXPANSION = new Vec3(0.08, 0.08, 0.08);
    public static final Set<ResourceLocation> SIGNS = new HashSet<>();
    public static final Set<ResourceLocation> EXPANDABLE_MODELS = new HashSet<>();
    private static final FileSystem vanillaFilesystem;

    static {
        try {
            vanillaFilesystem = FileSystems.newFileSystem(PolymerCommonUtils.getClientJar());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setup() {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(ResourcePackGenerator::build);
        PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.register(ResourcePackGenerator::close);
    }

    private static void close() {
        try {
            vanillaFilesystem.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PolymerBlock expandBlockModel(ResourceLocation id, PolymerBlock polymerBlock) {
        try {
            IoSupplier<InputStream> supplier = getAsset(id.getNamespace(), "blockstates/" + id.getPath() + ".json");
            BlockStateAsset decoded = BlockStateAsset.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(new JsonReader(new InputStreamReader(supplier.get())))).getOrThrow().getFirst();

            Map<String, List<StateModelVariant>> variants = decoded.variants().orElse(Collections.emptyMap());
            variants.values().forEach(list -> list.forEach(variant -> expandModel(variant.model())));

            List<StateMultiPartDefinition> multiParts = decoded.multipart().orElse(Collections.emptyList());
            multiParts.forEach(stateMultiPartDefinition -> stateMultiPartDefinition.apply().forEach(stateModelVariant -> expandModel(stateModelVariant.model())));
        } catch (Throwable e) {
            TerraformerPatch.LOGGER.error("Failed to read blockstate {}: {}", id, e);
        }
        return polymerBlock;
    }

    private static void expandModel(ResourceLocation id) {
        try {
            EXPANDABLE_MODELS.add(id.withSuffix(".json"));
            IoSupplier<InputStream> supplier = getAsset(id.getNamespace(), "models/" + id.getPath() + ".json");
            ModelAsset decoded = ModelAsset.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(new JsonReader(new InputStreamReader(supplier.get())))).getOrThrow().getFirst();
            decoded.parent().ifPresent(ResourcePackGenerator::expandModel);
        } catch (Throwable e) {
            TerraformerPatch.LOGGER.error("Failed to read model {}: {}", id, e);
        }
    }

    private static IoSupplier<InputStream> getAsset(String namespace, String path) {
        IoSupplier<InputStream> supplier = ResourceCollector.GLOBAL_ASSETS.getAsset(namespace, path);
        if (supplier != null) {
            return supplier;
        }
        var vanillaPath = vanillaFilesystem.getPath("/assets/" + namespace + "/" + path);
        if (Files.exists(vanillaPath)) {
            return IoSupplier.create(vanillaPath);
        } else {
            return null;
        }
    }

    private static void build(ResourcePackBuilder builder) {
        var atlas = AtlasAsset.builder();
        builder.forEachFile(((string, bytes) -> {
            String[] parts = string.split("/", 4);
            if (parts.length < 4) return;
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(parts[1], parts[3]);
            if (!parts[0].equals("assets") || !parts[2].equals("models")) return;
            if (!EXPANDABLE_MODELS.contains(id)) return;
            var asset = ModelAsset.fromJson(new String(bytes, StandardCharsets.UTF_8));
            if (asset.parent().isPresent()) {
                var parentId = asset.parent().get();
                var parentAsset = ModelAsset.fromJson(new String(Objects.requireNonNull(builder.getDataOrSource(AssetPaths.model(parentId) + ".json")), StandardCharsets.UTF_8));

                builder.addData(AssetPaths.model(TerraformerPatch.MOD_ID, parentId.getPath()) + ".json", new ModelAsset(parentAsset.parent(), parentAsset.elements().map(x -> x.stream()
                    .map(element -> new ModelElement(element.from().subtract(EXPANSION), element.to().add(EXPANSION),
                        element.faces(), element.rotation(), element.shade(), element.lightEmission())
                    ).toList()), parentAsset.textures(), parentAsset.display(), parentAsset.guiLight(), parentAsset.ambientOcclusion()).toBytes());
                if (asset.elements().isPresent()) {
                    builder.addData(string, new ModelAsset(asset.parent(), asset.elements().map(x -> x.stream()
                        .map(element -> new ModelElement(element.from().subtract(EXPANSION), element.to().add(EXPANSION),
                            element.faces(), element.rotation(), element.shade(), element.lightEmission())
                        ).toList()), asset.textures(), asset.display(), asset.guiLight(), asset.ambientOcclusion()).toBytes());
                }
            }
        }));

        TerraformerPatch.MOD_ASSET_IDS.forEach(modid -> {
            for (var entry : BlockStateModelManager.UV_LOCKED_MODELS.getOrDefault(modid, Collections.emptyMap()).entrySet()) {
                String path = entry.getKey();
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, path).withSuffix(".json");

                var expand = EXPANDABLE_MODELS.contains(id) ? EXPANSION : Vec3.ZERO;

                for (var v : entry.getValue()) {
                    var suffix = "_uvlock_" + v.x() + "_" + v.y();
                    var modelId = v.model().withSuffix(suffix);
                    var asset = ModelAsset.fromJson(new String(Objects.requireNonNull(builder.getData(AssetPaths.model(v.model()) + ".json")), StandardCharsets.UTF_8));

                    if (asset.parent().isPresent()) {
                        var parentId = asset.parent().get();
                        var parentAsset = ModelAsset.fromJson(new String(Objects.requireNonNull(builder.getDataOrSource(AssetPaths.model(parentId) + ".json")), StandardCharsets.UTF_8));
                        builder.addData(AssetPaths.model(TerraformerPatch.MOD_ID, parentId.getPath() + suffix) + ".json",
                            ModelModifiers.expandModelAndRotateUVLocked(parentAsset, expand, v.x(), v.y()));
                        builder.addData(AssetPaths.model(modelId) + ".json",
                            new ModelAsset(Optional.of(ResourceLocation.fromNamespaceAndPath(TerraformerPatch.MOD_ID, parentId.getPath() + suffix)), asset.elements(),
                                asset.textures(), asset.display(), asset.guiLight(), asset.ambientOcclusion()).toBytes());
                    }
                }
            }
        });

        builder.addWriteConverter(((string, bytes) -> {
            if (!string.contains("_uvlock_")) {
                String[] parts = string.split("/", 4);
                if (parts.length < 4) return bytes;
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(parts[1], parts[3]);
                if (!parts[0].equals("assets") || !parts[2].equals("models")) return bytes;
                if (!EXPANDABLE_MODELS.contains(id)) return bytes;

                var asset = ModelAsset.fromJson(new String(bytes, StandardCharsets.UTF_8));
                return new ModelAsset(asset.parent().map(x -> id(x.getPath())), asset.elements(), asset.textures(), asset.display(), asset.guiLight(), asset.ambientOcclusion()).toBytes();
            }
            return bytes;
        }));

        SIGNS.forEach(id -> ModelModifiers.createSignModel(builder, id.getNamespace(), id.getPath(), atlas));

        builder.addData("assets/minecraft/atlases/blocks.json", atlas.build());
    }
}