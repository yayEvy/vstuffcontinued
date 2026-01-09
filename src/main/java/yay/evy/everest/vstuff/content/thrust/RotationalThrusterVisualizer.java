package yay.evy.everest.vstuff.content.thrust;

import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlockEntity;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterVisual;

public class RotationalThrusterVisualizer
        implements BlockEntityVisualizer<RotationalThrusterBlockEntity> {

    @Override
    public RotationalThrusterVisual createVisual(
            VisualizationContext context,
            RotationalThrusterBlockEntity blockEntity,
            float partialTick
    ) {
        return new RotationalThrusterVisual(context, blockEntity, partialTick);
    }

    @Override
    public boolean skipVanillaRender(RotationalThrusterBlockEntity rotationalThrusterBlockEntity) {
        return false;
    }

    public boolean shouldVisualize(
            RotationalThrusterBlockEntity blockEntity
    ) {
        return true;
    }
}
