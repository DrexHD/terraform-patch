package me.drex.terraformpatch.res;


import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.factorytools.api.resourcepack.ModelModifiers;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelElement;
import me.drex.terraformpatch.TerraformerPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static me.drex.terraformpatch.TerraformerPatch.id;

public class ResourcePackGenerator {
    private static final Set<String> EXPANDABLE = Set.of("wall", "fence", "slab", "stairs", "pressure_plate", "button");
    private static final Vec3 EXPANSION = new Vec3(0.08, 0.08, 0.08);
    public static final Set<ResourceLocation> SIGNS = new HashSet<>();

    public static void setup() {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(ResourcePackGenerator::build);
    }

    private static void build(ResourcePackBuilder builder) {
        var atlas = AtlasAsset.builder();
        builder.forEachFile(((string, bytes) -> {
            for (var expandable : EXPANDABLE) {
                String[] parts = string.split("/", 5);
                if (parts.length < 5) continue;
                if (parts[0].equals("assets") && TerraformerPatch.MOD_ASSET_IDS.contains(parts[1]) && parts[2].equals("models") && parts[3].equals("block") && parts[4].contains(expandable)) {
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
                }
            }
        }));

        TerraformerPatch.MOD_ASSET_IDS.forEach(modid -> {
            for (var entry : BlockStateModelManager.UV_LOCKED_MODELS.getOrDefault(modid, Collections.emptyMap()).entrySet()) {
                var expand = EXPANDABLE.stream().anyMatch(expandable -> entry.getKey().contains(expandable) && entry.getKey().startsWith("block/")) ? EXPANSION : Vec3.ZERO;
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
                String[] parts = string.split("/", 5);
                if (parts.length < 5) return bytes;
                for (var expandable : EXPANDABLE) {
                    if (parts[0].equals("assets") && TerraformerPatch.MOD_ASSET_IDS.contains(parts[1]) && parts[2].equals("models") && parts[3].equals("block") && parts[4].contains(expandable)) {
                        var asset = ModelAsset.fromJson(new String(bytes, StandardCharsets.UTF_8));
                        return new ModelAsset(asset.parent().map(x -> id(x.getPath())), asset.elements(), asset.textures(), asset.display(), asset.guiLight(), asset.ambientOcclusion()).toBytes();
                    }
                }
            }
            return bytes;
        }));

        SIGNS.forEach(id -> ModelModifiers.createSignModel(builder, id.getNamespace(), id.getPath(), atlas));

        builder.addData("assets/minecraft/atlases/blocks.json", atlas.build());
    }
}