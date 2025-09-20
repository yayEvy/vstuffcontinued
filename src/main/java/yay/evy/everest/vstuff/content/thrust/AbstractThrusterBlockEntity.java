package yay.evy.everest.vstuff.content.thrust;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

import javax.annotation.Nullable;

import static com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity.MAX_SPEED;
import static yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlockEntity.BASE_MAX_THRUST;

@SuppressWarnings({"deprecation", "unchecked"})
public abstract class AbstractThrusterBlockEntity extends KineticBlockEntity {
    // Constants
    protected static final int OBSTRUCTION_LENGTH = 10;
    protected static final int TICKS_PER_ENTITY_CHECK = 5;
    protected static final int LOWEST_POWER_THRSHOLD = 5;
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

    public AbstractThrusterBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        thrusterData = new ThrusterData();
        particleType = (ParticleType<PlumeParticleData>) ParticleTypes.getPlumeType();
        this.damager = new ThrusterDamager(this);
    }


    @SuppressWarnings("null")
    @Override
    public void initialize() {
        super.initialize();
        if (!level.isClientSide) {
            calculateObstruction(level, worldPosition, getBlockState().getValue(AbstractThrusterBlock.FACING));
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
            calculateObstruction(level, worldPosition, currentBlockState.getValue(AbstractThrusterBlock.FACING));
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

        float softPower = (float) java.lang.Math.pow((double) powerPercentage, 1.2);

        float thrust = BASE_MAX_THRUST * thrustMultiplier * softPower * obstructionEffect;

        thrusterData.setThrust(thrust);
        isThrustDirty = false;

        System.out.println("[Thruster] speed=" + speed + ", obstruction=" + obstructionEffect + ", thrust=" + thrust);
    }





    protected boolean isWorking() {
        return getSpeed() != 0;
    }

    protected LangBuilder getGoggleStatus() {
        return Lang.text("Speed: " + getSpeed() + " rpm, Thrust: " + thrusterData.getThrust());
    }

    @Nullable
    protected abstract Direction getFluidCapSide();

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

    protected boolean isPowered() {
        return getOverriddenPowerOrState(getBlockState()) > 0;
    }

    protected float calculateObstructionEffect() {
        return (float) emptyBlocks / (float) OBSTRUCTION_LENGTH;
    }

    protected int getOverriddenPowerOrState(BlockState currentBlockState) {

        return currentBlockState.getValue(AbstractThrusterBlock.POWER);
    }

    public void emitParticles(Level level, BlockPos pos, BlockState state) {
        if (emptyBlocks == 0) return;
        int power = getOverriddenPowerOrState(state);

        double particleCountMultiplier = org.joml.Math.clamp(0.0, 2.0, VstuffConfig.THRUSTER_PARTICLE_COUNT_MULTIPLIER.get());
        if (particleCountMultiplier <= 0) return;

        clientTick++;
        if (power < LOWEST_POWER_THRSHOLD && clientTick % 2 == 0) {
            clientTick = 0;
            return;
        }

        this.particleSpawnAccumulator += particleCountMultiplier;

        int particlesToSpawn = (int) this.particleSpawnAccumulator;
        if (particlesToSpawn == 0) return;

        this.particleSpawnAccumulator -= particlesToSpawn;
        float powerPercentage = Math.max(power, LOWEST_POWER_THRSHOLD) / 15.0f;
        Direction direction = state.getValue(AbstractThrusterBlock.FACING);
        Direction oppositeDirection = direction.getOpposite();

        double currentNozzleOffset = NOZZLE_OFFSET_FROM_CENTER;
        Vector3d additionalVel = new Vector3d();
        ClientShip ship = VSGameUtilsKt.getShipObjectManagingPos((ClientLevel) level, pos);
        if (ship != null) {
            Vector3dc shipWorldVelocityJOML = ship.getVelocity();
            Matrix4dc transform = ship.getRenderTransform().getShipToWorld();
            Matrix4dc invTransform = ship.getRenderTransform().getWorldToShip();

            Vector3d shipVelocity = invTransform
                    // Rotate velocity with ship transform
                    .transformDirection(new Vector3d(shipWorldVelocityJOML));

            Vector3d particleEjectionUnitVecJOML = transform
                    // Rotate velocity with ship transform
                    .transformDirection(VectorConversionsMCKt.toJOMLD(oppositeDirection.getNormal()));

            double shipVelComponentAlongRotatedEjection = shipWorldVelocityJOML.dot(particleEjectionUnitVecJOML);
            if (shipVelComponentAlongRotatedEjection > 0.0) {
                Vector3d normalizedVelocity = new Vector3d();
                shipWorldVelocityJOML.normalize(normalizedVelocity);
                double shipVelComponentAlongRotatedEjectionNormalized = normalizedVelocity.dot(particleEjectionUnitVecJOML);
                //Effect is used to smooth transition from no additional offset/vel to full in range [0, 1]
                double effect = org.joml.Math.clamp(0.0, 1, shipVelComponentAlongRotatedEjectionNormalized);
                double additionalOffset = (shipVelComponentAlongRotatedEjection) * VstuffConfig.THRUSTER_PARTICLE_OFFSET_INCOMING_VEL_MODIFIER.get();
                currentNozzleOffset += additionalOffset * effect;
                additionalVel = new Vector3d(shipVelocity).mul(SHIP_VELOCITY_INHERITANCE * effect);
            }
        }

        double particleX = pos.getX() + 0.5 + oppositeDirection.getStepX() * currentNozzleOffset;
        double particleY = pos.getY() + 0.5 + oppositeDirection.getStepY() * currentNozzleOffset;
        double particleZ = pos.getZ() + 0.5 + oppositeDirection.getStepZ() * currentNozzleOffset;

        Vector3d particleVelocity = new Vector3d(oppositeDirection.getStepX(), oppositeDirection.getStepY(), oppositeDirection.getStepZ())
                .mul(PARTICLE_VELOCITY * powerPercentage).add(additionalVel);

        // Spawn the calculated number of particles.
        for (int i = 0; i < particlesToSpawn; i++) {
            level.addParticle(new PlumeParticleData(particleType), true,
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
        boolean wasThrustDirty = isThrustDirty;
        calculateObstruction(getLevel(), worldPosition, getBlockState().getValue(AbstractThrusterBlock.FACING));
        isThrustDirty = wasThrustDirty;

        Lang.translate("gui.goggles.thruster.status", new Object[0]).text(":").space().add(getGoggleStatus()).forGoggles(tooltip);

        float efficiency = 100;
        ChatFormatting tooltipColor = ChatFormatting.GREEN;
        if (emptyBlocks < OBSTRUCTION_LENGTH) {
            efficiency = calculateObstructionEffect() * 100;
        }

        Lang.builder().add(Lang.translate("gui.goggles.thruster.efficiency", new Object[0])).space().add(Lang.number(efficiency)).add(Lang.text("%")).style(tooltipColor).forGoggles(tooltip);

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
        if (!level.isClientSide) {
            level.getServer().execute(() -> {
                // Recalculate
                recalcThruster();

                // Re-register applier with the attachment
                ThrusterForceAttachment attachment = ThrusterForceAttachment.get(level, worldPosition);
                if (attachment != null) {
                    ThrusterData data = getThrusterData();
                    data.setDirection(VectorConversionsMCKt.toJOMLD(getBlockState().getValue(AbstractThrusterBlock.FACING).getNormal()));

                    ThrusterForceApplier applier = new ThrusterForceApplier(data);
                    attachment.addApplier(worldPosition, applier);

                    System.out.println("[Thruster] onLoad: re-registered applier at " + worldPosition);
                }
            });
        }
    }

    private void recalcThruster() {
        calculateObstruction(level, worldPosition, getBlockState().getValue(AbstractThrusterBlock.FACING));
        isThrustDirty = true;
        updateThrust(getBlockState());
        setChanged();
    }

}