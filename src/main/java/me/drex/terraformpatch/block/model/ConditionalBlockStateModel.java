package me.drex.terraformpatch.block.model;

import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

public class ConditionalBlockStateModel extends BlockModel {
    private final List<ItemDisplayElement> modelElements = new ArrayList<>();
    private final float viewRange;
    private final Predicate<BlockState> predicate;

    public ConditionalBlockStateModel(BlockState state, BlockPos pos, float viewRange, Predicate<BlockState> predicate) {
        this.predicate = predicate;
        this.viewRange = viewRange;
        updateBlockState(state);
    }

    public static ConditionalBlockStateModel longRange(BlockState state, BlockPos pos, Predicate<BlockState> predicate) {
        return new ConditionalBlockStateModel(state, pos, 100.0F, predicate);
    }

    public static ConditionalBlockStateModel midRange(BlockState state, BlockPos pos, Predicate<BlockState> predicate) {
        return new ConditionalBlockStateModel(state, pos, 3.0F, predicate);
    }

    public static ConditionalBlockStateModel shortRange(BlockState state, BlockPos pos, Predicate<BlockState> predicate) {
        return new ConditionalBlockStateModel(state, pos, 1.1F, predicate);
    }

    public void notifyUpdate(HolderAttachment.UpdateType updateType) {
        super.notifyUpdate(updateType);
        if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
            updateBlockState(this.blockState());
        }
    }

    private void updateBlockState(BlockState blockState) {
        List<BlockStateModelManager.ModelGetter> models = Collections.emptyList();
        if (predicate.test(blockState)) {
            models = BlockStateModelManager.get(this.blockState());
        }
        this.applyModel(models, this.blockPos());
    }

    private void applyModel(List<BlockStateModelManager.ModelGetter> models, BlockPos pos) {
        RandomSource random = RandomSource.create(this.blockState().getSeed(pos));
        int i = 0;

        while(models.size() < this.modelElements.size()) {
            this.removeElement(this.modelElements.removeLast());
        }

        for(; i < models.size(); ++i) {
            boolean newModel = false;
            ItemDisplayElement element;
            if (this.modelElements.size() <= i) {
                element = ItemDisplayElementUtil.createSimple();
                element.setViewRange(this.viewRange);
                element.setTeleportDuration(0);
                element.setItemDisplayContext(ItemDisplayContext.NONE);
                element.setYaw(180.0F);
                newModel = true;
                this.modelElements.add(element);
            } else {
                element = this.modelElements.get(i);
            }

            BlockStateModelManager.ModelData model = models.get(i).getModel(random);
            element.setItem(model.stack());
            element.setLeftRotation(model.quaternionfc());
            if (newModel) {
                this.addElement(element);
            } else {
                element.tick();
            }
        }

    }
}
