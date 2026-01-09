package yay.evy.everest.vstuff.content.thrust;

import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public class MechanicalThrusterVisualizer
        implements BlockEntityVisualizer<MechanicalThrusterBlockEntity> {

    @Override
    public MechanicalThrusterVisual createVisual(
            VisualizationContext context,
            MechanicalThrusterBlockEntity blockEntity,
            float partialTick
    ) {
        return new MechanicalThrusterVisual(context, blockEntity, partialTick);
    }

    @Override
    public boolean skipVanillaRender(MechanicalThrusterBlockEntity rotationalThrusterBlockEntity) {
        return false;
    }

    public boolean shouldVisualize(
            MechanicalThrusterBlockEntity blockEntity
    ) {
        return true;
    }
}
