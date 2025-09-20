package yay.evy.everest.vstuff.blocks;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import yay.evy.everest.vstuff.thruster.AbstractThrusterBlockEntity;

import java.util.List;

public class RotationalThrusterBlockEntity extends AbstractThrusterBlockEntity {

    public static final int BASE_MAX_THRUST = 400_000;

    public RotationalThrusterBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    @Override
    protected boolean isWorking() {
        // Only generate thrust if there is rotational input
        return getRpm() != 0;
    }


    public float getRpm() {
        if (level != null) {
            BlockEntity be = level.getBlockEntity(worldPosition);
            if (be instanceof KineticBlockEntity kineticBE) {
                // Use the kinetic network's speed
                return kineticBE.getSpeed();
            }
        }
        return 0;
    }

    @Override
    protected Direction getFluidCapSide() {
        return null; // No fluid capability
    }

    @Override
    protected LangBuilder getGoggleStatus() {
        int speed = 0;
        float thrust = 0f;
        if (thrusterData != null) {
            speed = (int)getSpeed();
            thrust = thrusterData.getThrust();
        }
        return Lang.text("Speed: " + speed + " rpm, Thrust: " + thrust);
    }


    @Override
    protected void addSpecificGoggleInfo(List<Component> tooltip, boolean isPlayerSneaking) {
    }
}
