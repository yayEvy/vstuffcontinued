package yay.evy.everest.vstuff.content.ropes.pulley;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.mod.api.BlockEntityPhysicsListener;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;

import java.util.List;
import java.util.Map;

public class PhysPulleyBlockEntity extends KineticBlockEntity implements BlockEntityPhysicsListener {

    public enum PulleyState {
        OPEN,
        WAITING,
        EXTENDED
    }

    private Integer constraintId = null;
    private ReworkedRope attachedRope = null;
    private double currentRopeLength = 0.0;
    public PulleyState state = PulleyState.OPEN;

    public PhysPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public boolean canAttach() {
        if (state == PulleyState.EXTENDED || state == PulleyState.WAITING) {
            if (constraintId == null || !RopeManager.getActiveRopes().containsKey(constraintId)) {
                resetSelf();
                return true;
            }
        }
        return state == PulleyState.OPEN;
    }
    public void attachRope(ReworkedRope rope) {
        if (rope == null) return;

        this.state = PulleyState.EXTENDED;
        this.attachedRope = rope;
        this.currentRopeLength = rope.jointValues.maxLength();
        this.constraintId = rope.ropeId;


        this.setChanged();
        if (level instanceof ServerLevel sl) {
            sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public void setWaiting() {
        state = PulleyState.WAITING;
        setChanged();
    }

    public void open() {
        state = PulleyState.OPEN;
        // System.out.println("opening");
        setChanged();
    }

    public void resetSelf() {
        // System.out.println("self reset");
        state = PulleyState.OPEN;
        attachedRope = null;
        constraintId = null;
        currentRopeLength = 0.0;
        setChanged();
    }

    public static PhysPulleyBlockEntity create(BlockPos pos, BlockState state) {
        return new PhysPulleyBlockEntity(VStuffBlockEntities.PHYS_PULLEY_BE.get(), pos, state);
    }

    @Override
    public @NotNull String getDimension() {
        return "";
    }

    @Override
    public void physTick(@Nullable PhysShip physShip, @NotNull PhysLevel physLevel) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (state != PulleyState.EXTENDED) return;


        if (constraintId != null && !RopeManager.getActiveRopes().containsKey(constraintId)) {
            resetSelf();
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        if (attachedRope == null && constraintId != null) {
            attachedRope = RopeManager.getActiveRopes().get(constraintId);
        }

        if (attachedRope == null) {
            resetSelf();
            return;
        }

        float speed = getSpeed();
        float ropeDelta = speed * 0.001f;
        float oldLength = attachedRope.jointValues.maxLength();

        if (oldLength <= 1.0f && ropeDelta < 0f) {
            currentRopeLength = 1.0f;
            return;
        }

        float newLength = oldLength + ropeDelta;
        newLength = Math.max(1.0f, Math.min(newLength, 256f));

        if (Math.abs(newLength - oldLength) < 0.0001f) return;

        attachedRope.setJointLength(serverLevel, newLength);
        this.currentRopeLength = newLength;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);

    }

    @Override
    public void setDimension(@NotNull String s) {
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);


        boolean hasRope = (state == PulleyState.EXTENDED || state == PulleyState.WAITING) && constraintId != null;

        tooltip.add(Component.literal(" "));

        if (hasRope) {
            tooltip.add(Component.literal("Length: " + String.format("%.1f", currentRopeLength) + " blocks")
                    .withStyle(ChatFormatting.BLUE));

            float speed = getSpeed();
            if (Math.abs(speed) > 4) {
                String direction = speed > 0 ? "Extending" : "Retracting";
                tooltip.add(Component.literal("Status: " + direction + " (" + String.format("%.1f", Math.abs(speed)) + " RPM )")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("Status: Idle")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.literal("No rope attached").withStyle(ChatFormatting.AQUA));
        }
        return true;
    }


    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);

        if (this.constraintId != null) {
            tag.putInt("rope_constraint_id", this.constraintId);
        } else {
            tag.putInt("rope_constraint_id", -1);
        }

        tag.putDouble("length", currentRopeLength);
        tag.putString("state", state.name());
    }
    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        if (tag.contains("rope_constraint_id")) {
            this.constraintId = tag.getInt("rope_constraint_id");
            if (this.constraintId == -1) this.constraintId = null;
        }
        if (tag.contains("state")) {
            this.state = PulleyState.valueOf(tag.getString("state"));
        }

        this.currentRopeLength = tag.getDouble("length");

        Map<Integer, ReworkedRope> active = RopeManager.getActiveRopes();
        if (active != null && constraintId != null) {
            this.attachedRope = active.get(constraintId);
        }
    }
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putDouble("length", currentRopeLength);
        tag.putString("state", state.name());
        if (constraintId != null) tag.putInt("rope_constraint_id", constraintId);
        return tag;
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("length")) {
            this.currentRopeLength = tag.getDouble("length");
        }
        if (tag.contains("state")) {
            this.state = PulleyState.valueOf(tag.getString("state"));

        }
        if (tag.contains("id")) constraintId = tag.getInt("id");
        if (tag.contains("length")) currentRopeLength = tag.getDouble("length");
    }

    @Override
    public float calculateStressApplied() {
        return 32f;
    }

    public void onRedstoneUpdate(boolean powered) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!powered) return;

        if (constraintId == null || attachedRope == null) return;

        attachedRope.removeJoint(serverLevel);

        attachedRope = null;
        constraintId = null;
        currentRopeLength = 0.0;
        state = PulleyState.OPEN;
        setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }




}