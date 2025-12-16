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

    private Rope attachedRope = null;
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
    }

    public void setWaiting() {
        state = PulleyState.WAITING;
    }

    public void clearWaiting() {
        state = state == PulleyState.WAITING ? PulleyState.OPEN : state;
    }

    public void resetSelf() {
        state = PulleyState.OPEN;
        attachedRope = null;
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
        System.out.println("new " + newLength);
        float minRopeLength = 0.25f;
        newLength = Math.max(newLength, minRopeLength);

        attachedRope.setJointLength(serverLevel, newLength);
    }


    @Override
    public void setDimension(@NotNull String s) {

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