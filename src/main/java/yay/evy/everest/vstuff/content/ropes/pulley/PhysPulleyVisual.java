package yay.evy.everest.vstuff.content.ropes.pulley;

import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import yay.evy.everest.vstuff.index.VStuffPartials;

import java.util.function.Consumer;

import static yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlock.HORIZONTAL_FACING;

public class PhysPulleyVisual extends ShaftVisual<PhysPulleyBlockEntity> implements SimpleTickableVisual {

    final Direction direction;
    final RotatingInstance coil;

    public PhysPulleyVisual(VisualizationContext context, PhysPulleyBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        direction = blockState.getValue(HORIZONTAL_FACING).getOpposite();

        coil = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(VStuffPartials.PULLEY_COIL))
                .createInstance()
                .rotateToFace(Direction.UP, rotationAxis())
                .setup(blockEntity)
                .setPosition(getVisualPosition());

        coil.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(coil);
    }

    @Override
    public void updateLight(float pt) {
        super.updateLight(pt);
        BlockPos front = pos.relative(direction);
        relight(front, coil);
    }

    @Override
    public void update(float pt) {
        super.update(pt);
        coil.setup(blockEntity).setChanged();
    }

    @Override
    protected void _delete() {
        super._delete();
        coil.delete();
    }
}
