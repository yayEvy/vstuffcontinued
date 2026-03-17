package yay.evy.everest.vstuff.content.ropes.pulley;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.mod.api.BlockEntityPhysicsListener;
import yay.evy.everest.vstuff.content.ropes.IRopeActor;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;

import java.util.List;
import java.util.Objects;

public class PhysPulleyBlockEntity extends KineticBlockEntity implements BlockEntityPhysicsListener, IRopeActor {

    private Integer ropeId = null;
    private ReworkedRope rope = null;

    public PhysPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void connectRope(Integer ropeId, BlockState state, Level level, BlockPos pos) {
        if (ropeId == null || RopeManager.getRope(ropeId) == null) return;

        this.ropeId = ropeId;
        this.rope = RopeManager.getRope(ropeId);

        blockConnect(state, level, pos);

        setChanged();
    }

    @Override
    public void removeRope(Integer ropeId, BlockState state, Level level, BlockPos pos) {
        this.ropeId = null;
        this.rope = null;

        blockRemove(state, level, pos);

        setChanged();
    }

    @Override
    public BlockState getActorBlockState() {
        return getBlockState();
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

        if (IRopeActor.canActorAttach(this.getBlockState())) return;
        if (ropeId == null) {
            if (rope != null) removeRope(null, getBlockState(), serverLevel, getBlockPos());
            return;
        }


        if (!RopeManager.hasRope(ropeId)) {
            removeRope(ropeId, getBlockState(), serverLevel, getBlockPos());
            return;
        }

        float ropeDelta = getSpeed() * 0.001f; // todo replace 0.001f with config value
        float oldLength = rope.jointValues.maxLength();

        if (oldLength <= 1.0f && ropeDelta < 0f) return; // less than min and retracting

        float newLength = oldLength + ropeDelta;
        newLength = Math.max(1.0f, Math.min(newLength, 256f)); // todo replace 256f with config value

        if (Math.abs(newLength - oldLength) < 0.0001f) return;

        rope.setJointLength(serverLevel, newLength);
    }

    @Override public void setDimension(@NotNull String s) {}

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        boolean hasRope = ropeId != null;
        if (hasRope && rope == null) {
            rope = RopeManager.getRope(ropeId);
            if (rope == null) return false;
        }

        tooltip.add(Component.literal(" "));

        if (hasRope) {
            tooltip.add(Component.literal("Length: " + String.format("%.1f", rope.jointValues.maxLength()) + " blocks")
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

        tag.putInt("rope_constraint_id", Objects.requireNonNullElse(this.ropeId, -1));
    }
    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        if (tag.contains("rope_constraint_id")) {
            this.ropeId = tag.getInt("rope_constraint_id");
            if (this.ropeId == -1) this.ropeId = null;
        }

        connectRope(ropeId, getBlockState(), level, getBlockPos());
    }
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (ropeId != null) tag.putInt("rope_constraint_id", ropeId);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains("id")) ropeId = tag.getInt("id");
    }


    public void onRedstoneUpdate(boolean powered) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!powered) return;

        if (ropeId == null || rope == null) return;

        rope.removeJoint(serverLevel);

        removeRope(ropeId, getBlockState(), serverLevel, getBlockPos());
    }




}