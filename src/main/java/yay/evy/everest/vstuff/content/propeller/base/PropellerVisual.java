package yay.evy.everest.vstuff.content.propeller.base;

import java.util.function.Consumer;

import dev.engine_room.flywheel.api.task.Plan;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;

public class PropellerVisual<T extends AbstractPropellerBlockEntity>
        extends KineticBlockEntityVisual<T>
        implements DynamicVisual, TickableVisual {

    protected TransformedInstance propeller;
    protected float lastAngle = Float.NaN;
    protected boolean renderBlades;
    protected final Matrix4f baseTransform = new Matrix4f();

    public PropellerVisual(VisualizationContext context, T blockEntity, float partialTicks) {
        super(context, blockEntity, partialTicks);
        setupBlades();
    }

    private void setupBlades() {
        PartialModel model = blockEntity.getPartialBladeModel();

        if (blockEntity.getBlockState().getValue(AbstractPropellerBlock.HAS_BLADES) && model != null) {
            renderBlades = true;
            propeller = instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(model))
                    .createInstance();

            Direction.Axis axis = rotationAxis();
            Direction align = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);

            propeller.translate(getVisualPosition())
                    .center()
                    .rotate(new Quaternionf().rotateTo(0, 1, 0, align.getStepX(), align.getStepY(), align.getStepZ()));

            baseTransform.set(propeller.pose);
            animate(blockEntity.angle);
        } else {
            renderBlades = false;
        }
    }

     public void beginFrame(DynamicVisual.Context ctx) {
        if (!renderBlades || propeller == null) return;

        float partialTicks = ctx.partialTick();
        float speed = blockEntity.visualSpeed.getValue(partialTicks) * 3f / 10f;
        float angle = blockEntity.angle + speed * partialTicks;

        if (Math.abs(angle - lastAngle) < 0.001f) return;

        animate(angle);
        lastAngle = angle;
    }

    private void animate(float angle) {
        propeller.setTransform(baseTransform)
                .rotateY(AngleHelper.rad(angle))
                .uncenter()
                .setChanged();
    }

    public void tick(TickableVisual.Context ctx) {
        boolean shouldHaveBlades = blockEntity.getBlockState().getValue(AbstractPropellerBlock.HAS_BLADES);
        if (shouldHaveBlades != renderBlades) {
            if (renderBlades && propeller != null) propeller.delete();
            setupBlades();
        }
    }

    @Override
    public void updateLight(float partialTicks) {
        if (propeller != null) relight(propeller);
    }

    @Override
    protected void _delete() {
        if (propeller != null) propeller.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        if (propeller != null) consumer.accept(propeller);
    }

    @Override
    public Plan<DynamicVisual.Context> planFrame() {
        return null;
    }

    @Override
    public Plan<TickableVisual.Context> planTick() {
        return null;
    }
}