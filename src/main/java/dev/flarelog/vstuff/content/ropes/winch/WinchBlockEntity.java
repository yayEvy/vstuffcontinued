package dev.flarelog.vstuff.content.ropes.winch;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.flarelog.vstuff.content.ropes.Rope;
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
import dev.flarelog.vstuff.content.ropes.util.IRopeActor;
import dev.flarelog.vstuff.content.ropes.RopeManager;
import dev.flarelog.vstuff.content.ropes.RopeFactory;
import dev.flarelog.vstuff.index.VStuffBlockEntities;

import java.util.List;
import java.util.Objects;

public class WinchBlockEntity extends KineticBlockEntity implements BlockEntityPhysicsListener, IRopeActor {

    private volatile Integer ropeId = null;

    public WinchBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void connectRope(Integer ropeId) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (ropeId == null || !RopeManager.get(serverLevel).hasRope(ropeId)) return;

        this.ropeId = ropeId;

        blockConnect(this.getActorBlockState(), level, this.getPos());

        setChanged();
    }

    @Override
    public void removeRope(Integer ropeId) {
        this.ropeId = null;

        assert level != null;
        blockRemove(this.getActorBlockState(), level, this.getPos());

        setChanged();
    }

    @Override
    public BlockState getActorBlockState() {
        return getBlockState();
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    public static WinchBlockEntity create(BlockPos pos, BlockState state) {
        return new WinchBlockEntity(VStuffBlockEntities.WINCH_BE.get(), pos, state);
    }

    @Override
    public @NotNull String getDimension() {
        return "";
    }

    @Override
    public void physTick(@Nullable PhysShip physShip, @NotNull PhysLevel physLevel) {
        assert level != null;
        this.tickPulley();
//        VSGameUtilsKt.executeOrSchedule(level, this::tickPulley);
    }

    private void tickPulley() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (this.canAttach()) return;
        if (ropeId == null) {
            return;
        }

        Rope rope = RopeManager.get(serverLevel).getRope(this.ropeId);

        if (!RopeManager.get(serverLevel).hasRope(ropeId)) {
            removeRope(ropeId);
            return;
        }

        float ropeDelta = getSpeed() * 0.001f; // todo replace 0.001f with config value
        float oldLength = rope.getLength();

        if (oldLength <= 1.0f && ropeDelta < 0f) return; // less than min and retracting

        float newLength = oldLength + ropeDelta;
        newLength = Math.max(1.0f, Math.min(newLength, 256f)); // todo replace 256f with config value

        if (Math.abs(newLength - oldLength) < 0.0001f) return;

        rope.setLength(newLength);
        //VStuffPackets.channel().send(PacketDistributor.ALL.noArg(), new UpdateRopeLengthPacket(ropeId, rope.jointValues.maxLength()));
    }

    @Override public void setDimension(@NotNull String s) {}

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        boolean hasRope = ropeId != null;

        tooltip.add(Component.literal(" "));

        if (hasRope) {

            tooltip.add(Component.literal("Length: ")
                    .withStyle(ChatFormatting.AQUA)
            );


            float speed = getSpeed();
            if (Math.abs(speed) > 0) {
                String direction = speed > 0 ? "Extending" : "Retracting";

                tooltip.add(Component.literal("Status: ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(direction + " (" + String.format("%.1f", Math.abs(speed)) + " RPM )")
                                .withStyle(ChatFormatting.GRAY)
                        )
                );

            } else {
                tooltip.add(Component.literal("Status: ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("Idle")
                                .withStyle(ChatFormatting.GRAY)
                        )
                );
            }
        } else {
            tooltip.add(Component.literal("No rope attached").withStyle(ChatFormatting.RED));
        }
        return true;
    }


    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);

        tag.putInt("ropeId", Objects.requireNonNullElse(this.ropeId, -1));

        if (ropeId != null && level instanceof ServerLevel serverLevel) {
            Rope rope = RopeManager.get(serverLevel).getRope(ropeId);
            if (rope != null) {
                tag.putFloat("ropeLength", rope.getLength());
            }
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        if (tag.contains("ropeId")) {
            this.ropeId = tag.getInt("ropeId");
            if (this.ropeId == -1) this.ropeId = null;
        }

        if (this.ropeId != null && tag.contains("ropeLength")) {
            float savedLength = tag.getFloat("ropeLength");
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.getServer().execute(() -> {
                    Rope rope = RopeManager.get(serverLevel).getRope(this.ropeId);
                    if (rope != null) {
                        rope.setLength(savedLength);

                    }
                });
            }
        }

        connectRope(ropeId);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (ropeId != null) tag.putInt("ropeId", ropeId);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains("ropeId")) ropeId = tag.getInt("ropeId");
    }


    public void onRedstoneUpdate(boolean powered) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!powered) return;

        if (ropeId == null) return;

        RopeFactory.removeAndCleanupRope(ropeId, serverLevel);
    }




}
