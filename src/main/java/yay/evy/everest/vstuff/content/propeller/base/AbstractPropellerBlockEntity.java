package yay.evy.everest.vstuff.content.propeller.base;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import yay.evy.everest.vstuff.content.mechanical_thruster.AttachmentUtils;
import yay.evy.everest.vstuff.content.mechanical_thruster.ThrusterData;
import yay.evy.everest.vstuff.content.mechanical_thruster.ThrusterForceApplier;
import yay.evy.everest.vstuff.content.mechanical_thruster.ThrusterForceAttachment;
import yay.evy.everest.vstuff.index.VStuffPartialModels;

public abstract class AbstractPropellerBlockEntity extends KineticBlockEntity {

    public LerpedFloat visualSpeed = LerpedFloat.linear();
    public float angle;
    protected PartialModel bladeModel;
    protected double power;
    protected double waterPower;
    protected double stressImpact;
    protected double maxThrust;
    protected boolean isInWater = false;

    private float rotation = 0f;
    private float prevRotation = 0f;
    private float currentVisualRPM = 0f;
    private float lastAppliedThrust = 0f;

    private final ThrusterData thrusterData = new ThrusterData();
    private ThrusterForceApplier applier;

    private static final float MAX_THRUST = 100000f;

    public AbstractPropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);

        if (bladeModel != null) {
            if (bladeModel == VStuffPartialModels.WOODEN_PROPELLER_BLADE) {
                compound.putString("BladeModelId", "wooden_double_blade");
            }
        }
    }


    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        if (clientPacket) {
            visualSpeed.chase(getGeneratedSpeed(), 1 / 64f, LerpedFloat.Chaser.EXP);

            if (compound.contains("BladeModelId")) {
                String id = compound.getString("BladeModelId");

                if (id.equals("wooden_double_blade")) {
                    bladeModel = VStuffPartialModels.WOODEN_PROPELLER_BLADE;
                }
            }
        }
    }


    public void tick() {
        if (level == null) return;

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            float rpm = getSpeed() * 20f;
            updateThrustFromRPM(serverLevel, rpm);
        }

        if (level.isClientSide) {
            float targetSpeed = getSpeed();
            visualSpeed.updateChaseTarget(targetSpeed);
            visualSpeed.tickChaser();
            angle += visualSpeed.getValue() * 3f / 10f;
            angle %= 360;

            prevRotation = rotation;

            float rpm = getSpeed();
            float maxVisualRPM = 400f;
            float targetVisualRPM = Math.signum(rpm) * Math.min(Math.abs(rpm), maxVisualRPM);

            float accelRate = 0.05f;
            float decelRate = 0.08f;

            if (Math.abs(targetVisualRPM) > Math.abs(currentVisualRPM)) {
                currentVisualRPM += (targetVisualRPM - currentVisualRPM) * accelRate;
            } else {
                currentVisualRPM += (targetVisualRPM - currentVisualRPM) * decelRate;
            }

            if (Math.signum(targetVisualRPM) != Math.signum(currentVisualRPM) &&
                    Math.abs(currentVisualRPM) < 5f) {
                currentVisualRPM *= 0.9f;
            }

            rotation += (-currentVisualRPM * 6f / 20f);
        }
    }

    public float getInterpolatedRotation(float partialTicks) {
        return prevRotation + (rotation - prevRotation) * partialTicks;
    }

    private void updateThrustFromRPM(ServerLevel serverLevel, float rpm) {
        ServerShip ship = AttachmentUtils.getShipAt(serverLevel, worldPosition);
        ThrusterForceAttachment attachment = ship != null ? ThrusterForceAttachment.get(level, worldPosition) : null;

        if (Math.abs(rpm) < 0.01f || !getBlockState().getValue(AbstractPropellerBlock.HAS_BLADES)) {
            if (attachment != null && applier != null) {
                attachment.removeApplier(serverLevel, worldPosition);
                applier = null;
            }
            lastAppliedThrust = 0f;
            return;
        }

        Direction facing = getBlockState().getValue(AbstractPropellerBlock.FACING);
        Vector3d dir = new Vector3d(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        dir.normalize();

        float newThrust = getNewThrust(rpm);
        lastAppliedThrust = newThrust;

        thrusterData.setThrust(newThrust);
        thrusterData.setDirection(dir);

        if (attachment != null) {
            if (applier == null) {
                applier = new ThrusterForceApplier(thrusterData);
                attachment.addApplier(worldPosition, applier);
            } else {
                applier.updateData(thrusterData);
            }
        }
    }

    private float getNewThrust(float rpm) {
        float targetThrust = getTargetThrust(rpm);
        float thrustChangeSpeed = 0.1f;
        float reverseBlendLimit = 0.15f;

        if (Math.signum(targetThrust) != Math.signum(lastAppliedThrust)) {
            thrustChangeSpeed = reverseBlendLimit;
        }

        float newThrust = lastAppliedThrust + (targetThrust - lastAppliedThrust) * thrustChangeSpeed;
        return (float) (Math.min(maxThrust, newThrust * (isInWater ? waterPower : power)));
    }

    private static float getTargetThrust(float rpm) {
        float rpmAbs = Math.abs(rpm);
        float rpmIdle = 40f;
        float rpmFull = 200f;

        float rpmFactor;
        if (rpmAbs <= rpmIdle) {
            rpmFactor = (float) Math.pow(rpmAbs / rpmIdle, 2.5f) * 0.1f;
        } else {
            float normalized = (rpmAbs - rpmIdle) / (rpmFull - rpmIdle);
            normalized = Math.min(1f, normalized);
            rpmFactor = (float) Math.pow(normalized, 3.5f);
        }

        return MAX_THRUST * rpmFactor * Math.signum(rpm);
    }

    public abstract PartialModel getPartialBladeModel();
    public abstract void setPartialBladeModel(PartialModel model);
    public abstract double getPower();
    public abstract void setPower(double setTo);
    public abstract double getWaterPower();
    public abstract void setWaterPower(double setTo);
    public abstract double getStressImpact();
    public abstract void setStressImpact(double setTo);
    public abstract double getMaxThrust();
    public abstract void setMaxThrust(double setTo);
}
