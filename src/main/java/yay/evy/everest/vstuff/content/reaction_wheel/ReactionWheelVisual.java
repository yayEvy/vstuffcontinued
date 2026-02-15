package yay.evy.everest.vstuff.content.reaction_wheel;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import yay.evy.everest.vstuff.index.VStuffPartials;

import java.util.function.Consumer;

import static yay.evy.everest.vstuff.content.reaction_wheel.ReactionWheelBlock.FACING;

public class ReactionWheelVisual extends KineticBlockEntityVisual<ReactionWheelBlockEntity> {

    final RotatingInstance shaftHalf;
    final RotatingInstance core;
    final Direction direction;

    public ReactionWheelVisual(VisualizationContext context, ReactionWheelBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        direction = blockState.getValue(FACING);

        shaftHalf = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
                .createInstance()
                .rotateToFace(Direction.NORTH, direction)
                .setup(blockEntity)
                .setPosition(getVisualPosition());

        core = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(VStuffPartials.REACTION_WHEEL_CORE))
                .createInstance()
                .rotateToFace(Direction.UP, direction)
                .setup(blockEntity)
                .setPosition(getVisualPosition());

        shaftHalf.setChanged();
        core.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(shaftHalf);
        consumer.accept(core);
    }


    @Override
    public void updateLight(float partialTick) {
        BlockPos bottom = pos.relative(direction);
        relight(bottom, shaftHalf);

        relight(core);
    }

    @Override
    public void update(float partialTick) {
        shaftHalf.setup(blockEntity).setChanged();
        core.setup(blockEntity).setChanged();
    }

    @Override
    protected void _delete() {
        shaftHalf.delete();
        core.delete();
    }
}
