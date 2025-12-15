package yay.evy.everest.vstuff.content.thrust;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;
import org.joml.Math;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.particles.ParticleTypes;
import yay.evy.everest.vstuff.particles.PlumeParticleData;

import java.util.List;

import static com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity.MAX_SPEED;
@SuppressWarnings({"deprecation", "unchecked"})
public class RotationalThrusterBlockEntity extends KineticBlockEntity {

    public static final int BASE_MAX_THRUST = 400_000;
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
            if (shouldEmitParticles()) {
                emitParticles(level, worldPosition, currentBlockState);
            }
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

    public void emitParticles(Level level, BlockPos pos, BlockState state) {
        if (emptyBlocks == 0) return;

        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        double particleCountMultiplier = org.joml.Math.clamp(
                0.0, 2.0,
                VstuffConfig.THRUSTER_PARTICLE_COUNT_MULTIPLIER.get() * getSpeedScalar()
        );
        if (particleCountMultiplier <= 0) return;

        clientTick++;
        if (clientTick % 2 == 0) {
            clientTick = 0;
            return;
        }

        this.particleSpawnAccumulator += particleCountMultiplier;
        int particlesToSpawn = (int) this.particleSpawnAccumulator;
        if (particlesToSpawn == 0) return;
        this.particleSpawnAccumulator -= particlesToSpawn;

        Direction direction = state.getValue(RotationalThrusterBlock.FACING);
        Direction oppositeDirection = direction.getOpposite();

        double currentNozzleOffset = NOZZLE_OFFSET_FROM_CENTER;
        Vector3d additionalVel = new Vector3d();

        ClientShip ship = VSGameUtilsKt.getShipObjectManagingPos(clientLevel, pos);
        if (ship != null) {
            Vector3dc shipWorldVelocityJOML = ship.getVelocity();
            Matrix4dc transform = ship.getRenderTransform().getShipToWorld();
            Matrix4dc invTransform = ship.getRenderTransform().getWorldToShip();

            Vector3d shipVelocity = invTransform.transformDirection(new Vector3d(shipWorldVelocityJOML));
            Vector3d particleEjectionUnitVecJOML = transform.transformDirection(VectorConversionsMCKt.toJOMLD(oppositeDirection.getNormal()));

            double shipVelComponentAlongRotatedEjection = shipWorldVelocityJOML.dot(particleEjectionUnitVecJOML);
            if (shipVelComponentAlongRotatedEjection > 0.0) {
                Vector3d normalizedVelocity = new Vector3d();
                shipWorldVelocityJOML.normalize(normalizedVelocity);
                double shipVelComponentAlongRotatedEjectionNormalized = normalizedVelocity.dot(particleEjectionUnitVecJOML);

                double effect = org.joml.Math.clamp(0.0, 1.0, shipVelComponentAlongRotatedEjectionNormalized);
                double additionalOffset = shipVelComponentAlongRotatedEjection * VstuffConfig.THRUSTER_PARTICLE_OFFSET_INCOMING_VEL_MODIFIER.get();
                currentNozzleOffset += additionalOffset * effect;
                additionalVel = new Vector3d(shipVelocity).mul(SHIP_VELOCITY_INHERITANCE * effect);
            }
        }

        double particleX = pos.getX() + 0.5 + oppositeDirection.getStepX() * currentNozzleOffset;
        double particleY = pos.getY() + 0.5 + oppositeDirection.getStepY() * currentNozzleOffset;
        double particleZ = pos.getZ() + 0.5 + oppositeDirection.getStepZ() * currentNozzleOffset;

        Vector3d particleVelocity = new Vector3d(oppositeDirection.getStepX(), oppositeDirection.getStepY(), oppositeDirection.getStepZ())
                .mul(PARTICLE_VELOCITY * getSpeedScalar())
                .add(additionalVel);

        for (int i = 0; i < particlesToSpawn; i++) {
            clientLevel.addParticle(new PlumeParticleData(particleType), true,
                    particleX, particleY, particleZ,
                    particleVelocity.x, particleVelocity.y, particleVelocity.z);
        }
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


}