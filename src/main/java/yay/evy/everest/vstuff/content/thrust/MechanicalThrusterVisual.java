package yay.evy.everest.vstuff.content.thrust;

import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import yay.evy.everest.vstuff.index.VStuffPartials;

import java.util.function.Consumer;

import static yay.evy.everest.vstuff.content.thrust.MechanicalThrusterBlock.FACING;

public class MechanicalThrusterVisual extends ShaftVisual<MechanicalThrusterBlockEntity> {

    protected final RotatingInstance fan;
    final Direction facing;

    public MechanicalThrusterVisual(VisualizationContext context, MechanicalThrusterBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        facing = blockState.getValue(FACING);

        fan = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(VStuffPartials.THRUSTER_FAN))
                .createInstance()
                .rotateToFace(Direction.NORTH, facing)
                .setup(blockEntity, facing.getAxis())
                .setPosition(getVisualPosition());

        fan.setChanged();
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
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(fan);
    }

    @Override
    public void updateLight(float pt) {
        super.updateLight(pt);
        BlockPos front = pos.relative(facing);
        relight(front, fan);
    }

    @Override
    public void update(float pt) {
        super.update(pt);
        fan.setup(blockEntity, facing.getAxis(), getFanSpeed()).setChanged();
    }

    @Override
    protected void _delete() {
        super._delete();
        fan.delete();
    }
}
