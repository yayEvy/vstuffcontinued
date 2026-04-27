package yay.evy.everest.vstuff.content.ships.thrust;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;
import yay.evy.everest.vstuff.internal.utility.AttachmentUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

import javax.annotation.Nullable;
import java.util.List;

import static com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity.MAX_SPEED;

public class MechanicalThrusterBlockEntity extends KineticBlockEntity implements IAirCurrentSource {

   @Nullable
    private AirCurrent airCurrent;

    public static final int BASE_MAX_THRUST = 100_000;

    protected static final int OBSTRUCTION_LENGTH = 10;
    protected static final int TICKS_PER_ENTITY_CHECK = 5;

    protected ThrusterData thrusterData;
    protected int emptyBlocks;


    private int currentTick = 0;

    public MechanicalThrusterBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        thrusterData = new ThrusterData();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level == null) return;
        if (!level.isClientSide) {
            calculateObstruction(level, worldPosition, getBlockState().getValue(MechanicalThrusterBlock.FACING));
        }
    }

    @Override
    public void tick() {
        if (this.isRemoved()) {
            return;
        }

        if (level == null) return;

        if (level.getBlockState(worldPosition).getBlock() != this.getBlockState().getBlock()) {
            this.setRemoved();
            return;
        }

        super.tick();
        updateAirCurrent();
        BlockState currentBlockState = getBlockState();

        currentTick++;

        int previousEmptyBlocks = emptyBlocks;
        calculateObstruction(level, worldPosition, currentBlockState.getValue(MechanicalThrusterBlock.FACING));
        if (previousEmptyBlocks != emptyBlocks) {
            setChanged();
            level.sendBlockUpdated(worldPosition, currentBlockState, currentBlockState, Block.UPDATE_CLIENTS);
        }

        updateThrust(currentBlockState);
    }

    public void updateThrust(BlockState currentBlockState) {
        float speed = getSpeed();
        if (speed == 0) {
            thrusterData.setThrust(0);
            return;
        }

        float obstructionEffect = calculateObstructionEffect(); // 0..1
        float powerPercentage = Math.min(Math.abs(speed) / MAX_SPEED, 1f); // scale rotation to 0..1

        float thrustMultiplier = VStuffConfigs.server().thrustMultiplier.getF();

        float softPower = (float) java.lang.Math.pow(powerPercentage, 1.2);

        float thrust = BASE_MAX_THRUST * thrustMultiplier * softPower * obstructionEffect;

        thrusterData.setThrust(thrust);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    protected String getGoggleStatus() {
        int speed = 0;
        float thrust = 0f;
        if (thrusterData != null) {
            speed = (int) java.lang.Math.abs(getSpeed());
            thrust = thrusterData.getThrust();
        }

        return "Speed: " + speed + " rpm, Thrust: " + thrust;
    }

    public ThrusterData getThrusterData() {
        return thrusterData;
    }

    protected void addSpecificGoggleInfo(List<Component> tooltip, boolean isPlayerSneaking) {}

    protected float calculateObstructionEffect() {
        return (float) emptyBlocks / (float) OBSTRUCTION_LENGTH;
    }


    @SuppressWarnings("deprecation")
    public void calculateObstruction(Level level, BlockPos pos, Direction forwardDirection){
        for (emptyBlocks = 0; emptyBlocks < OBSTRUCTION_LENGTH; emptyBlocks++){
            BlockPos checkPos = pos.relative(forwardDirection, emptyBlocks + 1);
            BlockState state = level.getBlockState(checkPos);
            if (!(state.isAir() || !state.isSolid())) break;
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        calculateObstruction(getLevel(), worldPosition, getBlockState().getValue(MechanicalThrusterBlock.FACING));

        tooltip.add(Component.literal(" "));

        tooltip.add(Component.translatable("create.gui.goggles.thruster.status")
                .append(Component.literal(": "))
                .append(Component.literal(getGoggleStatus())
                .withStyle(ChatFormatting.AQUA)));

        float efficiency = 100;
        ChatFormatting tooltipColor = ChatFormatting.GREEN;
        if (emptyBlocks < OBSTRUCTION_LENGTH) {
            efficiency = calculateObstructionEffect() * 100;
        }

        tooltip.add(Component.translatable("create.gui.goggles.thruster.efficiency")
                .append(Component.literal(" "))
                .append(Component.literal(String.format("%.1f%%", efficiency))
                .withStyle(tooltipColor)));


        addSpecificGoggleInfo(tooltip, isPlayerSneaking);

        return true;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("emptyBlocks", emptyBlocks);
        compound.putInt("currentTick", currentTick);
        compound.putFloat("thrust", thrusterData.getThrust());

    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        emptyBlocks = compound.getInt("emptyBlocks");
        currentTick = compound.getInt("currentTick");
        thrusterData.setThrust(compound.getFloat("thrust"));
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            server.execute(() -> {
                if (!server.isSameThread()) return;

                recalcThruster();

                AttachmentUtils.getAttachment(serverLevel, getBlockPos(), ThrusterForceAttachment.class, a -> {
                    ThrusterData data = getThrusterData();
                    data.setDirection(VectorConversionsMCKt.toJOMLD(getBlockState().getValue(MechanicalThrusterBlock.FACING).getOpposite().getNormal()));

                    ThrusterForceApplier applier = new ThrusterForceApplier(data);
                    a.addApplier(worldPosition, applier);
                });
            });
        }
    }

    private void recalcThruster() {
        calculateObstruction(level, worldPosition, getBlockState().getValue(MechanicalThrusterBlock.FACING));
        updateThrust(getBlockState());
        setChanged();
    }

    private void updateAirCurrent() {
        float speed = Math.abs(getSpeed());
        if (speed == 0) {
            airCurrent = null;
            return;
        }

        if (airCurrent == null) {
            airCurrent = new AirCurrent(this);
        }

        if (currentTick % 10 == 0) {
            airCurrent.rebuild();
        }

        if (currentTick % TICKS_PER_ENTITY_CHECK == 0) {
            airCurrent.findEntities();
        }

        airCurrent.tick();
    }

    @Override
    public float getMaxDistance() {
        float speed = Math.abs(getSpeed());
        return Math.min(VStuffConfigs.server().thrusterMaxPushDistance.getF(), speed / 16.0f);
    }

    @Override
    public @Nullable AirCurrent getAirCurrent() {
        return airCurrent;
    }

    @Override
    public Level getAirCurrentWorld() {
        return level;
    }

    @Override
    public @NotNull BlockPos getAirCurrentPos() {
        return worldPosition;
    }

    @Override
    public @NotNull Direction getAirflowOriginSide() {
        return getBlockState().getValue(MechanicalThrusterBlock.FACING);
    }

    @Override
    public boolean isSourceRemoved() {
        return isRemoved();
    }

    @Override
    public Direction getAirFlowDirection() {
        return getBlockState().getValue(MechanicalThrusterBlock.FACING);
    }



}