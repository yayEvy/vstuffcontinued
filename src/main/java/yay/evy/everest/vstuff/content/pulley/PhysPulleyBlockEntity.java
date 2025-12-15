package yay.evy.everest.vstuff.content.pulley;

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

import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.content.constraint.Rope;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;

import java.util.List;

public class PhysPulleyBlockEntity extends KineticBlockEntity implements BlockEntityPhysicsListener {

    public enum PulleyState {
        OPEN,
        WAITING,
        EXTENDED
    }

    private Integer constraintId = null;
    private Rope attachedRope = null;
    private double currentRopeLength = 0.0;
    public PulleyState state = PulleyState.OPEN;

    public PhysPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public boolean canAttach() {
        return state == PulleyState.OPEN;
    }

    public void attachRope(Rope rope) {
        state = PulleyState.EXTENDED;
        attachedRope = rope;
        currentRopeLength = rope.maxLength;
        constraintId = rope.ID;

        clearWaiting();
    }

    public void setWaiting() {
        state = PulleyState.WAITING;
    }

    public void clearWaiting() {
        state = PulleyState.OPEN;
    }

    public void resetSelf() {
        state = PulleyState.OPEN;
        attachedRope = null;
        constraintId = null;
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
        if (!(state == PulleyState.EXTENDED) || attachedRope == null) return;

        float speed = getSpeed();
        if (speed == 0) return;

        float ropeDelta = speed * 0.0005f;

        float newLength = (float) attachedRope.maxLength + ropeDelta;
        float minRopeLength = 0.25f;
        newLength = Math.max(newLength, minRopeLength);

        attachedRope.setJointLength(serverLevel, newLength);
        currentRopeLength = newLength;
    }


    @Override
    public void setDimension(@NotNull String s) {

    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        tooltip.add(Component.literal(" "));

        if (constraintId != null) {
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
        if (constraintId != null) {
            tag.putInt("id", constraintId);
        }

        tag.putString("state", state.name());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        if (tag.contains("id")) {
            constraintId = tag.contains("id") ? tag.getInt("id") : null;

            attachedRope = ConstraintTracker.getActiveRopes().get(constraintId);
            currentRopeLength = attachedRope.maxLength;
        }

        state = PulleyState.valueOf(tag.getString("state"));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("state", state.name());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        state = PulleyState.valueOf(tag.getString("state"));
    }

    @Override
    public float calculateStressApplied() {
        return 32f;
    }
}