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
import org.joml.Math;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.thruster.AbstractThrusterBlockEntity;

import java.util.List;

import static com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity.MAX_SPEED;

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
    public void updateThrust(BlockState currentBlockState) {
        float speed = getSpeed();
        if (speed == 0) {
            thrusterData.setThrust(0);
            isThrustDirty = false;
            return;
        }

        float obstructionEffect = calculateObstructionEffect(); // 0..1
        float powerPercentage = org.joml.Math.min(Math.abs(speed) / MAX_SPEED, 1f); // scale rotation to 0..1

        float thrustMultiplier = VstuffConfig.THRUSTER_THRUST_MULTIPLIER.get().floatValue(); // user-configurable

        float softPower = (float) java.lang.Math.pow((double) powerPercentage, 1.2);

        float thrust = BASE_MAX_THRUST * thrustMultiplier * softPower * obstructionEffect;

        thrusterData.setThrust(thrust);
        isThrustDirty = false;

        System.out.println("[Thruster] speed=" + speed + ", obstruction=" + obstructionEffect + ", thrust=" + thrust);
    }

}
