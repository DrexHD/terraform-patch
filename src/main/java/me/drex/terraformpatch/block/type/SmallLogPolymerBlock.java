package me.drex.terraformpatch.block.type;

import com.terraformersmc.terraform.wood.api.block.SmallLogBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import me.drex.terraformpatch.TerraformerPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public record SmallLogPolymerBlock(
    Map<Direction.Axis, BlockState> clientBlock) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {

    private static final Map<ResourceLocation, ModelAsset> MODELS = new HashMap<>();
    private static final Set<ResourceLocation> USED_TEXTURES = new HashSet<>();

    static {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(builder -> {
            MODELS.forEach((id, asset) -> {
                String path = AssetPaths.model(id) + ".json";
                builder.addData(path, asset.toBytes());
            });
            USED_TEXTURES.forEach(id -> {
                String path = AssetPaths.texture(id) + ".png";
                byte[] bytes = builder.getData(path);
                if (bytes == null) {
                    TerraformerPatch.LOGGER.error("Failed to find texture {}", path);
                    return;
                }
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                    BufferedImage trimmed = img.getSubimage(3, 3, 10, 10);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(trimmed, "png", baos);
                    builder.addData(path, baos.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        });
    }

    public static SmallLogPolymerBlock of(ResourceLocation id) {
        Map<Direction.Axis, BlockState> clientBlock = new HashMap<>();
        Map<String, String> textures = new HashMap<>();
        if (id.getPath().contains("oak_log")) {
            BlockState state = (id.getPath().contains("stripped") ? Blocks.STRIPPED_OAK_LOG : Blocks.OAK_LOG).defaultBlockState();
            for (Direction.Axis value : Direction.Axis.values()) {
                clientBlock.put(value, state.setValue(RotatedPillarBlock.AXIS, value));
            }
            return new SmallLogPolymerBlock(clientBlock);
        } else {
            textures.put("end", id.withPrefix("block/").withSuffix("_top").toString());
            textures.put("side", id.withPrefix("block/").toString());
        }

        textures.forEach((key, value) -> USED_TEXTURES.add(ResourceLocation.parse(value)));
        {

            ModelAsset modelAsset = new ModelAsset(Optional.of(ResourceLocation.withDefaultNamespace("block/cube_column")), Optional.empty(), textures);
            ResourceLocation modelId = id.withPrefix("block/").withSuffix("_cube_column");
            MODELS.put(modelId, modelAsset);
            BlockState state = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, new PolymerBlockModel[]{new PolymerBlockModel(modelId, 0, 0, false, 1)});
            clientBlock.put(Direction.Axis.Y, state);
        }

        {
            for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
                ModelAsset modelAsset = new ModelAsset(Optional.of(ResourceLocation.withDefaultNamespace("block/cube_column_horizontal")), Optional.empty(), textures);
                ResourceLocation modelId = id.withPrefix("block/").withSuffix("_cube_column_horizontal");
                MODELS.put(modelId, modelAsset);
                BlockState state = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, new PolymerBlockModel[]{new PolymerBlockModel(modelId, 90, axis == Direction.Axis.X ? 90 : 0, false, 1)});
                clientBlock.put(axis, state);
            }
        }

        return new SmallLogPolymerBlock(clientBlock);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return clientBlock.getOrDefault(state.getValue(SmallLogBlock.AXIS), Blocks.BARRIER.defaultBlockState());
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return null;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
