package yay.evy.everest.vstuff.content.thrust;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.Math;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.particles.ParticleTypes;
import yay.evy.everest.vstuff.particles.PlumeParticleData;
import com.simibubi.create.content.kinetics.fan.AirFlowParticleData;
import com.simibubi.create.content.kinetics.fan.AirFlowParticle;

import java.util.List;

import static com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity.MAX_SPEED;
@SuppressWarnings({"deprecation", "unchecked"})
public class RotationalThrusterBlockEntity extends KineticBlockEntity
implements IAirCurrentSource {

    private AirCurrent airCurrent;


    public static final int BASE_MAX_THRUST = 100_000;
    // Constants
    protected static final int OBSTRUCTION_LENGTH = 10;
    protected static final int TICKS_PER_ENTITY_CHECK = 5;
    private static final float PARTICLE_VELOCITY = 4;
    private static final double NOZZLE_OFFSET_FROM_CENTER = 0.9;
    private static final double SHIP_VELOCITY_INHERITANCE = 0.5;

    // Common State
    protected ThrusterData thrusterData;
    protected int emptyBlocks;
    protected boolean isThrustDirty = false;
    private final ThrusterDamager damager;

    // Ticking
    private int currentTick = 0;
    private int clientTick = 0;
    private float particleSpawnAccumulator = 0.0f;

    // Particles
    protected ParticleType<PlumeParticleData> particleType;

    public AbstractComputerBehaviour computerBehaviour;
    public boolean overridePower = false;
    public int overridenPower;

    public RotationalThrusterBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        thrusterData = new ThrusterData();
        particleType = (ParticleType<PlumeParticleData>) ParticleTypes.getPlumeType();
        this.damager = new ThrusterDamager(this);
    }


    @Override
    public float calculateStressApplied() {
        return 64.0f;
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        super.initialize();
        if (!level.isClientSide) {
            calculateObstruction(level, worldPosition, getBlockState().getValue(RotationalThrusterBlock.FACING));
        }
    }

    @SuppressWarnings("null")
    @Override
    public void tick() {
        if (this.isRemoved()) {
            return;
        }
        //This part should ACTUALLY fix the issue with particle emission
        if (level.getBlockState(worldPosition).getBlock() != this.getBlockState().getBlock()) {
            this.setRemoved();
            return;
        }

        super.tick();
        BlockState currentBlockState = getBlockState();
        if (level.isClientSide) {
            spawnAirFlowParticles();
            return;


        }
        currentTick++;
        damager.tick(currentTick);
        int tick_rate = VstuffConfig.THRUSTER_TICKS_PER_UPDATE.get();

        if (currentTick % (tick_rate * 2) == 0) {
            int previousEmptyBlocks = emptyBlocks;
            calculateObstruction(level, worldPosition, currentBlockState.getValue(RotationalThrusterBlock.FACING));
            if (previousEmptyBlocks != emptyBlocks) {
                isThrustDirty = true;
                setChanged();
                level.sendBlockUpdated(worldPosition, currentBlockState, currentBlockState, Block.UPDATE_CLIENTS);
            }
        }

        if (isThrustDirty || currentTick % tick_rate == 0) {
            updateThrust(currentBlockState);
        }
    }

    public void updateThrust(BlockState currentBlockState) {
        float speed = getSpeed();
        if (speed == 0) {
            thrusterData.setThrust(0);
            isThrustDirty = false;
            return;
        }

        float obstructionEffect = calculateObstructionEffect(); // 0..1
        float powerPercentage = Math.min(Math.abs(speed) / MAX_SPEED, 1f); // scale rotation to 0..1

        float thrustMultiplier = VstuffConfig.THRUSTER_THRUST_MULTIPLIER.get().floatValue(); // user-configurable

        float softPower = (float) java.lang.Math.pow(powerPercentage, 1.2);

        float thrust = BASE_MAX_THRUST * thrustMultiplier * softPower * obstructionEffect;

        thrusterData.setThrust(thrust);
        isThrustDirty = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    protected boolean isWorking() {
        return getRpm() != 0;
    }

    public float getRpm() {
        if (level != null) {
            BlockEntity be = level.getBlockEntity(worldPosition);
            if (be instanceof KineticBlockEntity kineticBE) {
                return kineticBE.getSpeed();
            }
        }
        return 0;
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

    public int getEmptyBlocks() {
        return emptyBlocks;
    }

    public void dirtyThrust() {
        isThrustDirty = true;
    }

    protected boolean shouldEmitParticles() {
        return isWorking();
    }

    protected boolean shouldDamageEntities() {
        return VstuffConfig.THRUSTER_DAMAGE_ENTITIES.get() && isWorking();
    }

    protected void addSpecificGoggleInfo(List<Component> tooltip, boolean isPlayerSneaking) {}

    protected float calculateObstructionEffect() {
        return (float) emptyBlocks / (float) OBSTRUCTION_LENGTH;
    }

    protected float getSpeedScalar() {
        return Math.abs(getSpeed() / 256);
    }


    protected void spawnAirFlowParticles() {
        if (!(level instanceof ClientLevel clientLevel)) return;
        if (!isWorking() || emptyBlocks == 0) return;

        Direction exhaust = getBlockState().getValue(RotationalThrusterBlock.FACING).getOpposite();

        Vec3 localPos = VecHelper.getCenterOf(worldPosition)
                .add(Vec3.atLowerCornerOf(exhaust.getNormal()).scale(0.6));

        Vec3 finalPos;
        var ship = VSGameUtilsKt.getShipObjectManagingPos(level, worldPosition);
        if (ship != null) {
            finalPos = VectorConversionsMCKt.toMinecraft(
                    ship.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOML(localPos))
            );
        } else {
            finalPos = localPos;
        }

        clientLevel.addParticle(
                new AirFlowParticleData(
                        worldPosition.getX(),
                        worldPosition.getY(),
                        worldPosition.getZ()
                ),
                finalPos.x, finalPos.y, finalPos.z,
                0, 0, 0
        );
    }



    public void calculateObstruction(Level level, BlockPos pos, Direction forwardDirection){
        //Starting from the block behind and iterate OBSTRUCTION_LENGTH blocks in that direction
        //Can't really use level.clip as we explicitly want to check for obstruction only in ship space
        int oldEmptyBlocks = this.emptyBlocks;
        for (emptyBlocks = 0; emptyBlocks < OBSTRUCTION_LENGTH; emptyBlocks++){
            BlockPos checkPos = pos.relative(forwardDirection.getOpposite(), emptyBlocks + 1);
            BlockState state = level.getBlockState(checkPos);
            if (!(state.isAir() || !state.isSolid())) break;
        }
        if (oldEmptyBlocks != this.emptyBlocks) {
            isThrustDirty = true;
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        boolean wasThrustDirty = isThrustDirty;
        calculateObstruction(getLevel(), worldPosition, getBlockState().getValue(RotationalThrusterBlock.FACING));
        isThrustDirty = wasThrustDirty;

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
        isThrustDirty = true;

    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();

            server.execute(() -> {

                if (!server.isSameThread())
                    return;


                recalcThruster();

                ThrusterForceAttachment attachment =
                        ThrusterForceAttachment.get(serverLevel, worldPosition);

                if (attachment != null) {
                    ThrusterData data = getThrusterData();
                    data.setDirection(VectorConversionsMCKt.toJOMLD(
                            getBlockState().getValue(RotationalThrusterBlock.FACING).getNormal()
                    ));

                    ThrusterForceApplier applier = new ThrusterForceApplier(data);
                    attachment.addApplier(worldPosition, applier);
                }
            });
        }
    }

    private void recalcThruster() {
        calculateObstruction(level, worldPosition, getBlockState().getValue(RotationalThrusterBlock.FACING));
        isThrustDirty = true;
        updateThrust(getBlockState());
        setChanged();
    }

    @Override
    public boolean isSourceRemoved() {
        return isRemoved() || !isWorking();
    }

    @Override
    public BlockPos getAirCurrentPos() {
        return worldPosition;
    }

    @Override
    public Direction getAirflowOriginSide() {
        return null;
    }

    @Override
    public AirCurrent getAirCurrent() {
        if (airCurrent == null)
            airCurrent = new AirCurrent(this);

        Direction exhaust = getAirFlowDirection();

        airCurrent.direction = exhaust;
        airCurrent.pushing = true;

        float rpm = Math.abs(getSpeed());
        float strength = Mth.clamp(rpm / 24f, 0f, 6f);

        airCurrent.maxDistance =
                (float) (1.5 + strength * 0.9) * calculateObstructionEffect();

        Vec3 start = VecHelper.getCenterOf(worldPosition);
        Vec3 dir = Vec3.atLowerCornerOf(exhaust.getNormal());

        airCurrent.bounds = new AABB(
                start,
                start.add(dir.scale(airCurrent.maxDistance + 1))
        ).inflate(0.5);

        return airCurrent;
    }

    @Override
    public @Nullable Level getAirCurrentWorld() {
        return level;
    }


    private void invalidateAirCurrent() {
        if (airCurrent != null)
            airCurrent.rebuild();
    }
    @Override
    public Direction getAirFlowDirection() {
        return getBlockState()
                .getValue(RotationalThrusterBlock.FACING)
                .getOpposite();
    }

}