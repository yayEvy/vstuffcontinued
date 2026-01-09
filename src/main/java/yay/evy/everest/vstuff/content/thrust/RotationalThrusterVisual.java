package yay.evy.everest.vstuff.content.thrust;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class RotationalThrusterVisual
        extends KineticBlockEntityVisual<RotationalThrusterBlockEntity> {

    private final RotatingInstance fan;
    private final Direction facing;

    public RotationalThrusterVisual(
            VisualizationContext context,
            RotationalThrusterBlockEntity be,
            float partialTick
    ) {
        super(context, be, partialTick);

        facing = blockState.getValue(RotationalThrusterBlock.FACING);

        fan = instancerProvider()
                .instancer(AllInstanceTypes.ROTATING,
                        Models.partial(AllPartialModels.ENCASED_FAN_INNER))
                .createInstance();

        Vec3 offset = Vec3.atLowerCornerOf(facing.getNormal()).scale(0.25);
        Vec3 pos = Vec3.atLowerCornerOf(getVisualPosition()).add(offset);

        fan.setup(blockEntity, getFanSpeed())
                .setPosition(new Vector3f((float) pos.x, (float) pos.y, (float) pos.z))
                .rotateToFace(Direction.SOUTH, facing.getOpposite())
                .rotateTo(
                        0f, 0f, 0f,
                        0f, (float) Math.PI / 2, 0f
                )
                .setChanged();



    }

    private float getFanSpeed() {
        float speed = blockEntity.getSpeed() * 5;
        if (speed > 0)
            speed = Mth.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = Mth.clamp(speed, -64 * 20, -80);
        return speed;
    }

    @Override
    public void update(float partialTick) {
        fan.setup(blockEntity, getFanSpeed()).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(pos.relative(facing), fan);
    }

    @Override
    protected void _delete() {
        fan.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(fan);
    }
}
