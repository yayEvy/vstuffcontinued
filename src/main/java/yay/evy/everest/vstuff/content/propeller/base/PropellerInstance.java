package yay.evy.everest.vstuff.content.propeller.base;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.content.propeller.WoodenPropellerBlockEntity;

public class PropellerInstance<T extends AbstractPropellerBlockEntity>
        extends KineticBlockEntityVisual<T>
        implements DynamicVisual {

    protected RotatingInstance shaft;
    protected Instance propeller;
    protected boolean renderBlades;

    public PropellerInstance(VisualizationContext context, T blockEntity) {
        super(context, blockEntity, 0);

        shaft = instancerProvider()
                .instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
                .createInstance();

        updateShaft();
    }

    public PropellerInstance(VisualizationContext visualizationContext, WoodenPropellerBlockEntity woodenPropellerBlockEntity, float v) {
        super(visualizationContext, (T) woodenPropellerBlockEntity, v);
    }

    protected void updateShaft() {
        shaft.setRotationalSpeed(blockEntity.angle);
    }

    public void update() {
        BlockState state = blockEntity.getBlockState();

        if (state.hasProperty(AbstractPropellerBlock.HAS_BLADES)
                && state.getValue(AbstractPropellerBlock.HAS_BLADES)) {

            if (!renderBlades) {
                renderBlades = true;
                propeller = instancerProvider()
                        .instancer(
                                InstanceTypes.TRANSFORMED,
                                Models.partial(blockEntity.getPartialBladeModel())
                        )
                        .createInstance();
            }
        }

        updateShaft();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(shaft);
        if (propeller != null) {
            relight((BlockPos) propeller);
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(shaft);
        if (propeller != null) consumer.accept(propeller);
    }

    @Override
    protected void _delete() {
        shaft.delete();
        if (propeller != null) propeller.delete();
    }

    @Override
    public Plan<Context> planFrame() {
        return null;
    }

}
