package yay.evy.everest.vstuff.content.reaction_wheel;

import org.joml.Vector3i;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ReactionWheelBlockEntity extends KineticBlockEntity {

    protected ReactionWheelData reactionWheelData;
    public float visualRPM = 0;
    public float visualAngle = 0;


    public ReactionWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        reactionWheelData = new ReactionWheelData();
        reactionWheelData.mode = ReactionWheelData.ReactionWheelMode.DIRECT;
    }


    @SuppressWarnings("null")
    @Override
    public void initialize() {
        super.initialize();
        if (!level.isClientSide) {
            Direction facingDirection = this.getBlockState().getValue(ReactionWheelBlock.FACING);
            reactionWheelData.facing = new Vector3i(facingDirection.getStepX(), facingDirection.getStepY(), facingDirection.getStepZ());

            ReactionWheelAttachment ship = ReactionWheelAttachment.get(level, worldPosition);
            if (ship != null) {
                ReactionWheelForceApplier applier = new ReactionWheelForceApplier(reactionWheelData);
                ship.addApplier(worldPosition, applier);
            }
        }
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        if (getSpeed() != prevSpeed) {
            reactionWheelData.rotationSpeed = getSpeed();
        }
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            float targetRPM = getSpeed();
            float smoothing = 0.1f;
            visualRPM += (targetRPM - visualRPM) * smoothing;

            float degreesPerTick = (visualRPM * 6f) / 20f;
            visualAngle = (visualAngle + degreesPerTick) % 360f;
        }
    }




}