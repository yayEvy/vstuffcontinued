package yay.evy.everest.vstuff.content.pulley;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ChunkClaim;
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffBlocks;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.index.VStuffItems;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import yay.evy.everest.vstuff.utils.RopeStyles;


import java.lang.reflect.Field;
import java.util.*;

public class PhysPulleyBlockEntity extends KineticBlockEntity {

    private ItemStackHandler ropeInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendData();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() == Items.STRING || stack.getItem() == Items.LEAD;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    };

    private LazyOptional<IItemHandler> ropeInventoryOptional = LazyOptional.of(() -> ropeInventory);

    public BlockPos targetPos = null;
    public boolean hasTarget = false;
    private Integer constraintId = null;
    private double currentRopeLength = 5.0; // Default rope length
    private double minRopeLength = 0.1;

    // Constraint data
    private Long shipA = null;
    private Long shipB = null;
    private Vector3d localPosA = null;
    private Vector3d localPosB = null;

    private int tickCounter = 0;
    private static final double MIN_SPEED_THRESHOLD = 1.0;
    private double getLengthChangeRate() {
        return VstuffConfig.PULLEY_SPEED.get();
    }
    private double consumedRopeLength = 0.0;
    private double baseRopeLength = 1.0;
    private boolean ropeStateInitialized = false;
    boolean isRopeRendering = false;
    private BlockPos previewTargetPos = null;



    // new stuff
    public static boolean MANUAL = true;
    public ScrollValueBehaviour MODE;
    public static final String PULLEY_MODE = "MANUAL";
    private static final PulleyMode[] MODES = PulleyMode.values();
    private PulleyMode lastMode = PulleyMode.MANUAL;

    private double targetRopeLength;
    private boolean isExtending = false;
    private long extensionStartTime;
    private static final double EXTENSION_SPEED = 1.5;

    private boolean isPlayerRetracting = false;
    private boolean isPlayerExtending = false;
    private static final double MANUAL_SPEED = 1.0;

    private boolean hasCustomModeLoaded = false;
    private Integer loadedPulleyMode = null;
    private boolean constraintRestored = false;
    private boolean restoring = false;

    boolean isLowering = false;
    private BlockPos pendingTargetPos = null;
    private double pendingTargetDistance = 0;
    private Vector3d pulleyWorldPos;
    private Vector3d targetWorldPos;
    private Vector3d previewAttachVec = null;
    private boolean wasCut = false;

    private boolean manualMode = false;


    private Vector3d ropeEndPos = null;

    public PhysPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public Vector3d getRopeEndPos() { return ropeEndPos; }

    public boolean waitingForTarget = false;
    public Vector3d getPulleyWorldPos() {
        return this.pulleyWorldPos;
    }

    public Vector3d getTargetWorldPos() {
        return this.targetWorldPos;
    }


    public static PhysPulleyBlockEntity create(BlockPos pos, BlockState state) {
        return new PhysPulleyBlockEntity(VStuffBlockEntities.PHYS_PULLEY_BE.get(), pos, state);
    }
    public void setManualMode(boolean manualMode) { this.manualMode = manualMode; }
    public boolean isManualMode() { return manualMode; }

// a

    public void setConstraintId(Integer constraintId) {
        this.constraintId = constraintId;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

    }

    public static class SelectedRegion {
        public boolean hasBlocks;
        public Set<BlockPos> blockPositions;
        public Vector3d geometricCenter;

        public static SelectedRegion empty() {
            SelectedRegion r = new SelectedRegion();
            r.hasBlocks = false;
            r.blockPositions = Set.of();
            r.geometricCenter = new Vector3d();
            return r;
        }
    }


    public PulleyMode getPulleyMode() {
        if (MODES.length > 0) {
            return MODES[0];
        } else {
            return PulleyMode.MANUAL;
        }
    }


    private void onModeChanged(PulleyMode oldMode, PulleyMode newMode) {
        System.out.println("Pulley mode changed from " + oldMode + " to " + newMode);

        if (oldMode == PulleyMode.MANUAL && newMode == PulleyMode.AUTO) {
            if (waitingForTarget) {
                waitingForTarget = false;
                // System.out.println("Cancelled manual target setting - switching to auto mode");
            }

            if (constraintId == null) {
                hasTarget = false;
                targetPos = null;
                //    System.out.println("Cleared manual target - auto mode will find targets");
            }

        } else if (oldMode == PulleyMode.AUTO && newMode == PulleyMode.MANUAL) {
            // System.out.println("Switched to manual mode - player control enabled");


        }

        setChanged();
        sendData();
    }

    private void updatePulleyMode() {
        PulleyMode currentMode = getPulleyMode();


        if (currentMode == PulleyMode.MANUAL) {
            System.out.println("MANUAL MODE: Player controls rope extension/retraction and target selection");
        } else if (currentMode == PulleyMode.AUTO) {
            System.out.println("AUTO MODE: Automatic rope management and target detection");

            if (waitingForTarget) {
                waitingForTarget = false;
            }
        }

        setChanged();
        sendData();
    }

    private boolean createAutoConstraint() {
        if (level.isClientSide) return false;
        if (!hasTarget || !hasRope() || constraintId != null) return false;


        this.setManualMode(false);

        try {
            ServerLevel serverLevel = (ServerLevel) level;

            Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, getBlockPos());
            Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, targetPos);

            shipA = pulleyShip != null ? pulleyShip.getId() : getGroundBodyId(serverLevel);
            shipB = targetShip != null ? targetShip.getId() : getGroundBodyId(serverLevel);

            localPosA = getLocalPosition(serverLevel, getBlockPos(), pulleyShip, shipA);
            pulleyWorldPos = getWorldPosition(serverLevel, getBlockPos(), pulleyShip);

            if (previewAttachVec != null) {
                targetWorldPos = previewAttachVec;

                if (targetShip != null) {
                    Vector3d local = new Vector3d();
                    targetShip.getTransform().getWorldToShip().transformPosition(previewAttachVec, local);
                    localPosB = local;
                } else {
                    localPosB = previewAttachVec;
                }
            } else if (targetPos != null) {
                targetWorldPos = getWorldPosition(serverLevel, targetPos, targetShip);
                localPosB = getLocalPosition(serverLevel, targetPos, targetShip, shipB);
            } else {
                System.err.println("Pulley: No valid target for constraint creation");
                return false;
            }

            if (targetWorldPos == null || pulleyWorldPos == null) {
                System.err.println("Pulley: Cannot create constraint, world positions invalid");
                return false;
            }

            double distance = pulleyWorldPos.distance(targetWorldPos);

            if (distance < 0.01) {
                System.err.println("Pulley: Target too close to pulley, skipping constraint creation");
                return false;
            }

            targetRopeLength = Math.min(distance, getRawMaxRopeLength());
            targetRopeLength = Math.max(targetRopeLength, minRopeLength);
            currentRopeLength = targetRopeLength;

            double compliance = 5e-9;
            double maxForce = 2.0e7;

            VSRopeConstraint constraint = new VSRopeConstraint(
                    shipA, shipB,
                    compliance,
                    localPosA, localPosB,
                    maxForce,
                    currentRopeLength
            );

            Integer newConstraintId = VSGameUtilsKt.getShipObjectWorld(serverLevel).createNewConstraint(constraint);
            if (newConstraintId != null) {
                constraintId = newConstraintId;
                consumedRopeLength = Math.max(0, currentRopeLength - baseRopeLength);
                ropeStateInitialized = true;
                isRopeRendering = true;

                ConstraintTracker.addConstraintWithPersistence(
                        serverLevel, constraintId, shipA, shipB,
                        localPosA, localPosB, currentRopeLength,
                        compliance, maxForce,
                        ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY, getBlockPos(), new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.BASIC, "vstuff.ropes.normal")
                );

                setChanged();
                sendData();

                previewAttachVec = null;

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }



    public Vec3 getRopeAttachmentPoint() {
        return Vec3.atBottomCenterOf(getBlockPos());
    }


    public boolean shouldRenderRope() {

        return isRopeRendering || waitingForTarget;
    }

    private boolean canExtendRope(double newLength) {
        if (targetPos == null) return true;

        double currentDistance = calculateWorldDistance(getBlockPos(), targetPos);
        return newLength >= currentDistance * 0.9; // Allow some slack
    }
    private BlockPos findPulleyAnchorInShip(ServerLevel serverLevel, Ship ship) {
        var shipAABB = ship.getShipAABB();
        if (shipAABB == null) return null;

        int minX = (int) Math.floor(shipAABB.minX());
        int maxX = (int) Math.ceil(shipAABB.maxX());
        int minY = (int) Math.floor(shipAABB.minY());
        int maxY = (int) Math.ceil(shipAABB.maxY());
        int minZ = (int) Math.floor(shipAABB.minZ());
        int maxZ = (int) Math.ceil(shipAABB.maxZ());

        BlockPos closestAnchorPos = null;
        double closestDistance = Double.MAX_VALUE;

        Vector3d shipCenter = new Vector3d(
                (shipAABB.minX() + shipAABB.maxX()) / 2.0,
                (shipAABB.minY() + shipAABB.maxY()) / 2.0,
                (shipAABB.minZ() + shipAABB.maxZ()) / 2.0
        );

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = serverLevel.getBlockState(pos);

                    if (state.getBlock() == VStuffBlocks.PHYS_PULLEY.get()) {
                        double dist = shipCenter.distance(new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
                        if (dist < closestDistance) {
                            closestDistance = dist;
                            closestAnchorPos = pos;
                        }
                    }
                }
            }
        }

        return closestAnchorPos;
    }


    public static Vector3d calculateGeometricCenter(Set<BlockPos> positions) {
        if (positions.isEmpty()) return new Vector3d(0, 0, 0);

        double sumX = 0, sumY = 0, sumZ = 0;
        int count = positions.size();

        for (BlockPos pos : positions) {
            sumX += pos.getX();
            sumY += pos.getY();
            sumZ += pos.getZ();
        }

        return new Vector3d(sumX / count, sumY / count, sumZ / count);
    }



    public PhysifyResult physifyBlocksIntoShip(ServerLevel level, Set<BlockPos> blockPositions, @Nullable BlockPos pulleyAnchorWorldPos) {
        System.out.println("Physify called with " + blockPositions.size() + " blocks");
        if (blockPositions.isEmpty())
            return null;

        Vector3d center = calculateGeometricCenter(blockPositions);

        BlockPos anchorPos = new BlockPos(
                (int) Math.floor(center.x),
                (int) Math.floor(center.y),
                (int) Math.floor(center.z)
        );

        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        Ship ship = shipWorld.createNewShipAtBlock(VectorConversionsMCKt.toJOML(anchorPos), false, 1, VSGameUtilsKt.getDimensionId(level));

        if (!(ship instanceof ServerShip serverShip)) {
            System.err.println("Created ship is not a ServerShip!");
            return null;
        }

        Vector3i internalCenter = calculateInternalCenterPos(serverShip, level);

        BlockPos pulleyAnchorLocalPos = null;

        for (BlockPos worldPos : blockPositions) {
            BlockPos relativePos = worldPos.subtract(anchorPos);
            BlockPos shipLocalPos = new BlockPos(
                    internalCenter.x() + relativePos.getX(),
                    internalCenter.y() + relativePos.getY(),
                    internalCenter.z() + relativePos.getZ()
            );

            System.out.println("Copying block at " + worldPos + " to ship local pos " + shipLocalPos);
            copyBlock(level, worldPos, shipLocalPos);
            removeBlock(level, worldPos);

            if (pulleyAnchorWorldPos != null && worldPos.equals(pulleyAnchorWorldPos)) {
                pulleyAnchorLocalPos = shipLocalPos;
            }
        }

        System.out.println("Physified ship with " + blockPositions.size() + " blocks at anchor " + anchorPos);

        BlockPos pulleyAnchorShipWorldPos = null;
        if (pulleyAnchorLocalPos != null) {
            Vector3d worldVec = getWorldPosition(level, pulleyAnchorLocalPos, ship);
            pulleyAnchorShipWorldPos = new BlockPos(
                    (int) Math.floor(worldVec.x),
                    (int) Math.floor(worldVec.y),
                    (int) Math.floor(worldVec.z)
            );
            System.out.println("Pulley anchor local pos " + pulleyAnchorLocalPos + " converted back to world pos " + pulleyAnchorShipWorldPos);
        }

        return new PhysifyResult(ship, pulleyAnchorShipWorldPos);
    }




    public SelectedRegion getGeometricCenterOfBlocks(ServerLevel level, Set<BlockPos> blocks) {
        if (blocks.isEmpty()) return SelectedRegion.empty();

        double sumX = 0, sumY = 0, sumZ = 0;
        for (BlockPos pos : blocks) {
            sumX += pos.getX() + 0.5;
            sumY += pos.getY() + 0.5;
            sumZ += pos.getZ() + 0.5;
        }

        double count = blocks.size();
        Vector3d center = new Vector3d(sumX / count, sumY / count, sumZ / count);

        SelectedRegion region = new SelectedRegion();
        region.geometricCenter = center;
        region.blockPositions = blocks;
        region.hasBlocks = true;


        return region;
    }
    public class PhysifyResult {
        public final Ship ship;
        public final BlockPos pulleyAnchorWorldPos;

        public PhysifyResult(Ship ship, BlockPos pulleyAnchorWorldPos) {
            this.ship = ship;
            this.pulleyAnchorWorldPos = pulleyAnchorWorldPos;
        }
    }


    public Vector3i calculateInternalCenterPos(ServerShip ship, ServerLevel level) {
        ChunkClaim claim = ship.getChunkClaim();
        return claim.getCenterBlockCoordinates(VSGameUtilsKt.getYRange(level), new Vector3i());
    }
    public void copyBlock(ServerLevel world, BlockPos sourcePos, BlockPos destPos) {
        BlockState state = world.getBlockState(sourcePos);
        BlockEntity tileEntity = world.getBlockEntity(sourcePos);

        world.setBlock(destPos, state, 3);

        if (tileEntity != null) {
            BlockEntity newTE = world.getBlockEntity(destPos);
            if (newTE != null) {
                CompoundTag nbt = tileEntity.saveWithoutMetadata();
                newTE.load(nbt);
            }
        }
    }

    public void removeBlock(ServerLevel world, BlockPos pos) {
        world.removeBlock(pos, false);
    }

    private BlockPos findAutoTarget() {
        if (level == null) return null;

        BlockPos pulleyPos = getBlockPos();
        ServerLevel serverLevel = (ServerLevel) level;
        double maxDistance = getMaxRopeLength();

        AnchorTarget anchorTarget = findPulleyAnchorTarget(serverLevel, pulleyPos, maxDistance);
        if (anchorTarget != null) {
            System.out.println("AUTO MODE: Found pulley anchor target at " + anchorTarget.pos);

            this.previewAttachVec = anchorTarget.attachPoint;

            return anchorTarget.pos;
        }


        BlockPos shipTarget = findShipTarget(serverLevel, pulleyPos, maxDistance);
        if (shipTarget != null) {
            System.out.println("AUTO MODE: Found ship target at " + shipTarget);
            this.previewTargetPos = shipTarget;
            Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, shipTarget);
            if (targetShip instanceof ServerShip) {
                BlockPos anchorInShip = findPulleyAnchorInShip(serverLevel, targetShip);
                if (anchorInShip != null) {
                    System.out.println("AUTO MODE: Found pulley anchor inside existing ship at " + anchorInShip);
                    return anchorInShip;
                }
            }
            return shipTarget;
        }


        for (int y = 1; y <= (int) maxDistance; y++) {
            BlockPos checkPos = pulleyPos.below(y);

            if (checkPos.getY() < level.getMinBuildHeight()) {
                break;
            }

            BlockState state = level.getBlockState(checkPos);

            if (!state.isAir() && state.isSolidRender(level, checkPos)) {
                if (isBlockSuperglued(serverLevel, checkPos)) {
                    System.out.println("AUTO MODE: Found superglued structure at " + checkPos);
                    this.previewTargetPos = checkPos;

                    Set<BlockPos> gluedCluster = findSupergluedCluster(serverLevel, checkPos);

                    if (!gluedCluster.isEmpty()) {
                        System.out.println("AUTO MODE: Physifying glued cluster of size " + gluedCluster.size());

                        PhysifyResult result = physifyBlocksIntoShip(serverLevel, gluedCluster,
                                anchorTarget != null ? anchorTarget.pos : null);

                        if (result != null && result.ship != null) {
                            if (result.pulleyAnchorWorldPos != null) {
                                System.out.println("AUTO MODE: Attaching pulley to pulley anchor inside physified ship at "
                                        + result.pulleyAnchorWorldPos);
                                return result.pulleyAnchorWorldPos;
                            } else {
                                Vector3d pulleyWorldPos = getWorldPosition(serverLevel, pulleyPos, null);
                                BlockPos attachPos = findBlockOnShip(serverLevel, result.ship, pulleyWorldPos, maxDistance);

                                if (attachPos != null) {
                                    Vector3d attachWorldPos = getWorldPosition(serverLevel, attachPos, result.ship);
                                    BlockPos attachWorldBlockPos = BlockPos.containing(toVec3(attachWorldPos));

                                    System.out.println("AUTO MODE: Attaching pulley to physified ship block at "
                                            + attachWorldBlockPos + " (converted from ship coords)");

                                    return attachWorldBlockPos;
                                }
                            }
                        }

                        System.out.println("AUTO MODE: Physify failed, falling back to block at " + checkPos);
                        return checkPos;
                    }
                } else {
                    System.out.println("AUTO MODE: Found solid block target at " + checkPos);
                    return checkPos;
                }
            }
        }


        System.out.println("AUTO MODE: No suitable target found within " + maxDistance + " blocks");
        return null;
    }
    private static Vec3 toVec3(Vector3d vec) {
        return new Vec3(vec.x, vec.y, vec.z);
    }

    public BlockPos getPreviewTargetPos() {
        return this.previewTargetPos;
    }

    public static class AnchorTarget {
        public final BlockPos pos;
        public final Vector3d attachPoint;

        public AnchorTarget(BlockPos pos, Vector3d attachPoint) {
            this.pos = pos;
            this.attachPoint = attachPoint;
        }
    }

    private Vector3d getRopeAnchorPos(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        double offset = 0.4;

        switch (facing) {
            case DOWN:  y = pos.getY();       break; // exactly bottom
            case UP:    y = pos.getY() + 1.0; break; // exactly top
            case NORTH: z = pos.getZ();       break; // north face
            case SOUTH: z = pos.getZ() + 1.0; break; // south face
            case WEST:  x = pos.getX();       break; // west face
            case EAST:  x = pos.getX() + 1.0; break; // east face
        }


        return new Vector3d(x, y, z);
    }



    private AnchorTarget findPulleyAnchorTarget(ServerLevel serverLevel, BlockPos pulleyPos, double maxDistance) {
        int radius = (int) Math.ceil(maxDistance);
        AnchorTarget closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int x = pulleyPos.getX() - radius; x <= pulleyPos.getX() + radius; x++) {
            for (int y = pulleyPos.getY() - radius; y <= pulleyPos.getY() + radius; y++) {
                for (int z = pulleyPos.getZ() - radius; z <= pulleyPos.getZ() + radius; z++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    BlockState state = serverLevel.getBlockState(checkPos);

                    if (state.getBlock() == VStuffBlocks.PHYS_PULLEY.get()) {
                        double dist = pulleyPos.distSqr(checkPos);
                        if (dist < closestDistance) {
                            closestDistance = dist;
                            Vector3d attach = getRopeAnchorPos(serverLevel, checkPos, state);
                            closest = new AnchorTarget(checkPos, attach);
                        }
                    }
                }
            }
        }

        return closest;
    }


    private boolean isBlockSuperglued(ServerLevel level, BlockPos pos) {
        AABB blockBox = new AABB(pos);
        List<SuperGlueEntity> glueEntities = level.getEntitiesOfClass(SuperGlueEntity.class,
                blockBox, e -> e != null);
        return !glueEntities.isEmpty();
    }

    private Set<BlockPos> findSupergluedCluster(ServerLevel level, BlockPos startPos) {
        Set<BlockPos> cluster = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(startPos);

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            if (cluster.contains(current)) continue;
            cluster.add(current);
            System.out.println("Added block to cluster: " + current);

            List<BlockPos> gluedNeighbors = getSupergluedNeighbors(level, current);
            System.out.println("Neighbors for " + current + ": " + gluedNeighbors);

            for (BlockPos neighbor : gluedNeighbors) {
                if (!cluster.contains(neighbor)) {
                    toCheck.add(neighbor);
                    System.out.println("Queued neighbor: " + neighbor);
                }
            }
        }
        return cluster;
    }


    private List<BlockPos> getSupergluedNeighbors(ServerLevel level, BlockPos pos) {
        List<BlockPos> gluedNeighbors = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (isBlockSuperglued(level, neighbor)) {
                gluedNeighbors.add(neighbor);
            }
        }

        return gluedNeighbors;
    }


    private BlockPos findShipTarget(ServerLevel serverLevel, BlockPos pulleyPos, double maxDistance) {
        try {
            var allShips = VSGameUtilsKt.getShipObjectWorld(serverLevel).getAllShips();

            Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pulleyPos);

            Vector3d pulleyWorldPos = getWorldPosition(serverLevel, pulleyPos, pulleyShip);

            BlockPos closestShipBlock = null;
            double closestDistance = Double.MAX_VALUE;

            for (Ship ship : allShips) {
                if (pulleyShip != null && ship.getId() == pulleyShip.getId()) {
                    continue;
                }

                Vector3d shipCenter = (Vector3d) ship.getTransform().getPositionInWorld();
                double distanceToShip = pulleyWorldPos.distance(shipCenter);

                if (distanceToShip <= maxDistance) {
                    BlockPos shipBlock = findBlockOnShip(serverLevel, ship, pulleyWorldPos, maxDistance);
                    if (shipBlock != null) {
                        Vector3d blockWorldPos  = getWorldPosition(serverLevel, shipBlock, ship);

                        double blockDistance = pulleyWorldPos.distance(blockWorldPos);
                        if (blockDistance < closestDistance && blockDistance <= maxDistance) {
                            closestDistance = blockDistance;
                            closestShipBlock = shipBlock;
                        }

                    }

                }
            }

            return closestShipBlock;
        } catch (Exception e) {
            System.err.println("Error finding ship target: " + e.getMessage());
            return null;
        }
    }
    private BlockPos findBlockOnShip(ServerLevel serverLevel, Ship ship, Vector3d pulleyWorldPos, double maxDistance) {
        try {
            var shipAABB = ship.getShipAABB();
            if (shipAABB == null) return null;

            double centerX = (shipAABB.minX() + shipAABB.maxX()) / 2.0;
            double centerZ = (shipAABB.minZ() + shipAABB.maxZ()) / 2.0;

            int minY = (int) Math.floor(shipAABB.minY());
            int maxY = (int) Math.ceil(shipAABB.maxY());

            BlockPos bestPos = null;
            double closestDistance = Double.MAX_VALUE;

            int radius = 2;

            for (int y = maxY; y >= minY; y--) {
                for (int x = (int) Math.floor(centerX) - radius; x <= (int) Math.floor(centerX) + radius; x++) {
                    for (int z = (int) Math.floor(centerZ) - radius; z <= (int) Math.floor(centerZ) + radius; z++) {
                        BlockPos candidatePos = new BlockPos(x, y, z);
                        BlockState state = serverLevel.getBlockState(candidatePos);
                        if (!state.isAir() && state.isSolidRender(serverLevel, candidatePos)) {
                            Vector3d blockWorldPos = getWorldPosition(serverLevel, candidatePos, ship);
                            double distance = pulleyWorldPos.distance(blockWorldPos);

                            // Check distance to pulley and prefer closest block if multiple at same height
                            if (distance <= maxDistance) {
                                if (bestPos == null || y > bestPos.getY() || (y == bestPos.getY() && distance < closestDistance)) {
                                    bestPos = candidatePos;
                                    closestDistance = distance;
                                }
                            }
                        }
                    }
                }
                if (bestPos != null) {
                    return bestPos;
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error finding block on ship: " + e.getMessage());
            return null;
        }
    }

    public double getMinRopeLength() {
        return minRopeLength;
    }

    public Integer getConstraintId() {
        return constraintId;
    }


    public enum PulleyMode {
        MANUAL("pulley_mode.manual"),
        AUTO("pulley_mode.auto");

        private final String translationKey;

        PulleyMode(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }
    }



    @Override
    public void onLoad() {
        super.onLoad();
        initializeRopeInventory();
        ropeStateInitialized = false;
        restoring = true;

        if (!wasCut && hasTarget && constraintId != null && level instanceof ServerLevel serverLevel) {
            Integer newId = createConstraintWithLength(currentRopeLength);
            if (newId != null) {
                constraintId = newId;
                System.out.println("Recreated constraint at length " + currentRopeLength + " after reload");
            }
        }

        ropeStateInitialized = true;
        restoring = false;
    }



    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        tickCounter++;
        ServerLevel serverLevel = (ServerLevel) level;

        if (!constraintRestored) {
            recreateConstraintOnLoad(serverLevel);
            constraintRestored = true;
        }

        PulleyMode currentMode = getPulleyMode();
        if (currentMode != lastMode) {
            onModeChanged(lastMode, currentMode);
            lastMode = currentMode;
        }

        if (constraintId != null && lastMode != PulleyMode.MANUAL && !ropeStateInitialized) {
            if (tickCounter == 20 || tickCounter == 60 || tickCounter == 120) {
                if (!isConstraintValid(serverLevel)) restoreConstraintAfterLoad(serverLevel);
                else ropeStateInitialized = true;
            }
        }

        double speed = getSpeed();
        BlockPos pulleyBlockPos = getBlockPos();
        if (pulleyBlockPos == null) return;


        if (currentMode == PulleyMode.AUTO && speed < 0 && !hasTarget && !isLowering) {
            BlockPos autoTarget = findAutoTarget();
            if (autoTarget != null) {
                pendingTargetPos = autoTarget;

                Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pulleyBlockPos);
                Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, autoTarget);

                if (pulleyShip == null || targetShip == null) return;

                Vector3d pulleyWorldPos = getWorldPosition(serverLevel, pulleyBlockPos, pulleyShip);
                Vector3d targetWorldPos = getWorldPosition(serverLevel, autoTarget, targetShip);

                double rawDistance = pulleyWorldPos.distance(targetWorldPos);
                double maxDistance = getRawMaxRopeLength();

                pendingTargetDistance = Math.min(rawDistance, maxDistance);

                isLowering = true;
                sendData();
                System.out.println("AUTO MODE: Starting lowering to " + pendingTargetPos + " over distance " + pendingTargetDistance);
            }
        }


        if (isLowering && pendingTargetPos != null) {
            Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pulleyBlockPos);
            Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pendingTargetPos);
            if (pulleyShip == null || targetShip == null) return;

            Vector3d pulleyWorldPos = getWorldPosition(serverLevel, pulleyBlockPos, pulleyShip);
            Vector3d targetWorldPos = getWorldPosition(serverLevel, pendingTargetPos, targetShip);

            double step = getLengthChangeRate() / 8.0;
            double distance = pendingTargetDistance;
            double newLen = Math.min(currentRopeLength + step, distance);
            double progress = distance > 0 ? newLen / distance : 1.0;

            ropeEndPos = new Vector3d(
                    pulleyWorldPos.x + (targetWorldPos.x - pulleyWorldPos.x) * progress,
                    pulleyWorldPos.y + (targetWorldPos.y - pulleyWorldPos.y) * progress,
                    pulleyWorldPos.z + (targetWorldPos.z - pulleyWorldPos.z) * progress
            );

            currentRopeLength = newLen;
            isRopeRendering = true;
            sendData();

            BlockPos checkPos = BlockPos.containing(ropeEndPos.x, ropeEndPos.y, ropeEndPos.z);

            Ship foundShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, checkPos);
            if (foundShip != null) {
                finishAutoAttach(serverLevel, checkPos);
                return;
            }

            if (isBlockSuperglued(serverLevel, checkPos)) {
                Set<BlockPos> cluster = findSupergluedCluster(serverLevel, checkPos);
                PhysifyResult result = physifyBlocksIntoShip(serverLevel, cluster, checkPos);
                if (result != null && result.ship != null) {
                    finishAutoAttach(serverLevel, checkPos);
                    return;
                }
            }

            if (progress >= 0.999) {
                finishAutoAttach(serverLevel, pendingTargetPos);
                isLowering = false;

                targetPos = pendingTargetPos;
                pendingTargetPos = null;
                ropeEndPos = targetWorldPos;
                hasTarget = true;

                if (constraintId != null) removeExistingConstraint();
                createAutoConstraint();

                System.out.println("AUTO MODE: Rope attached to " + targetPos);
            }
        }


        else if (currentMode == PulleyMode.MANUAL && hasTarget) {
            double step = getLengthChangeRate() / 4.0;
            if (speed > 0) extendRope(step);
            else if (speed < 0) retractRope(step);

            if (targetPos != null) {
                Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pulleyBlockPos);
                Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, targetPos);

                if (pulleyShip != null && targetShip != null) {
                    Vector3d pulleyWorldPos = getWorldPosition(serverLevel, pulleyBlockPos, pulleyShip);
                    Vector3d targetWorldPos = getWorldPosition(serverLevel, targetPos, targetShip);
                    Vector3d dir = targetWorldPos.sub(pulleyWorldPos).normalize();

                    ropeEndPos = pulleyWorldPos.add(dir.mul(currentRopeLength));
                    isRopeRendering = true;
                    sendData();
                }
            }
        }


        else if (currentMode == PulleyMode.AUTO && hasTarget) {
            double step = getLengthChangeRate() / 4.0;
            if (speed < 0) retractRope(step);
            else if (speed > 0) extendRope(step);
        }
    }


    private void finishAutoAttach(ServerLevel serverLevel, BlockPos attachPos) {
        isLowering = false;
        pendingTargetPos = null;
        targetPos = attachPos;
        hasTarget = true;

        if (constraintId != null) removeExistingConstraint();

        if (createAutoConstraint()) {
            System.out.println("AUTO MODE: Rope attached to " + attachPos);
        } else {
            System.err.println("AUTO MODE: Failed to create constraint at " + attachPos);
        }
    }

    public void removeExistingConstraint() {
        removeExistingConstraint(false);
    }



    private boolean updateLocalPositions() {
        if (level.isClientSide) return false;
        ServerLevel serverLevel = (ServerLevel) level;

        BlockPos pulleyPos = getBlockPos();
        if (pulleyPos == null) {
            System.err.println("Pulley block position is null!");
            return false;
        }
        if (targetPos == null) {
            System.err.println("Target position is null!");
            return false;
        }

        Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pulleyPos);
        Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, targetPos);

        shipA = pulleyShip != null ? Long.valueOf(pulleyShip.getId()) : getGroundBodyId(serverLevel);
        shipB = targetShip != null ? Long.valueOf(targetShip.getId()) : getGroundBodyId(serverLevel);

        localPosA = getLocalPosition(serverLevel, pulleyPos, pulleyShip, shipA);
        localPosB = getLocalPosition(serverLevel, targetPos, targetShip, shipB);

        return localPosA != null && localPosB != null;
    }


    public void sendData() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private void extendRope(double amount) {
        if (level.isClientSide) return;

        if (!hasTarget) {
            return;
        }

        double maxRopeLength = getRawMaxRopeLength();

        double newLength = currentRopeLength + amount;

        if (lastMode == PulleyMode.MANUAL) {
            if (newLength > maxRopeLength) {
                newLength = maxRopeLength;
                amount = newLength - currentRopeLength;
            }
            currentRopeLength = newLength;
            actuallyConsumeRope(amount);

            if (constraintId != null && level instanceof ServerLevel serverLevel) {
                removeExistingConstraint(true);

                Integer newId = createConstraintWithLength(currentRopeLength);
                if (newId != null) constraintId = newId;
            }

            setChanged();
            sendData();
            return;
        }

        if (consumeRope(amount)) {
            currentRopeLength = newLength;
            setChanged();
            sendData();
        }
    }


    private void retractRope(double amount) {
        if (level.isClientSide) return;

        if (!hasTarget) {
            return;
        }

        double newLength = currentRopeLength - amount;
        if (newLength < baseRopeLength) newLength = baseRopeLength;

        if (lastMode == PulleyMode.MANUAL) {
            currentRopeLength = newLength;
            returnRope(amount);

            if (constraintId != null && level instanceof ServerLevel serverLevel) {
                removeExistingConstraint(true);

                Integer newId = createConstraintWithLength(currentRopeLength);
                if (newId != null) constraintId = newId;
            }

            setChanged();
            sendData();
            return;
        }

        returnRope(amount);
        currentRopeLength = newLength;
        setChanged();
        sendData();
    }


    public double getCurrentRopeLength() {
        return currentRopeLength;
    }

    private boolean isConstraintValid(int id, ServerLevel serverLevel) {
        return ConstraintTracker.constraintExists(serverLevel, id);
    }

    private void clearConstraint() {
        constraintId = null;
        ropeStateInitialized = false;
        isRopeRendering = false;
        setChanged();
        sendData();
    }
    public boolean isLowering() {
        return isLowering;
    }

    public boolean isRopeRendering() {
        return isRopeRendering;
    }




    private Integer createConstraintWithLength(double length) {
        if (level.isClientSide || !hasTarget || !hasRope() || constraintId != null) {
            return null;
        }
        if (!updateLocalPositions()) {
            System.err.println("Cannot create constraint: localPosA or localPosB is null");
            return null;
        }



        if (localPosA != null && localPosB != null) {
            double currentDist = localPosA.distance(localPosB);

            if (currentDist < 0.1) {
                localPosB = new Vector3d(localPosA.x, localPosA.y - length, localPosA.z);
                System.out.println("Adjusted localPosB to maintain rope length after cut: " + localPosB);
            }
        }

        try {
            ServerLevel serverLevel = (ServerLevel) level;
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(serverLevel);

            Long groundShipId = shipWorld.getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(serverLevel));

            Long realShipA = shipA;
            if (realShipA == null || shipWorld.getAllShips().getById(realShipA) == null) {
                System.out.println("Ship A missing or invalid, using groundShipId instead");
                realShipA = groundShipId;
            }

            Long realShipB = shipB;
            if (realShipB == null || shipWorld.getAllShips().getById(realShipB) == null) {
                System.out.println("Ship B missing or invalid, using groundShipId instead");
                realShipB = groundShipId;
            }

            System.out.println("Creating constraint: shipA=" + realShipA + ", shipB=" + realShipB + ", length=" + length);

            VSRopeConstraint constraint = new VSRopeConstraint(
                    realShipA,
                    realShipB,
                    1e-9,
                    localPosA,
                    localPosB,
                    999999999,
                    length
            );

            Integer newConstraintId = shipWorld.createNewConstraint(constraint);

            if (newConstraintId != null) {
                constraintId = newConstraintId;
                consumedRopeLength = Math.max(0, length - baseRopeLength);
                ropeStateInitialized = true;
                isRopeRendering = true;

                ConstraintTracker.cleanupOrphanedConstraints(serverLevel, getBlockPos());

                ConstraintTracker.addConstraintWithPersistence(serverLevel, constraintId, realShipA, realShipB,
                        localPosA, localPosB, length, 1e-9, 50000000,
                        ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY, getBlockPos(),
                        new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.BASIC, "vstuff.ropes.normal"));

                System.out.println("Created constraint " + constraintId + " with length " + length);
                return constraintId;
            } else {
                System.err.println("Failed to create constraint");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating constraint: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



    private boolean isConstraintValid(ServerLevel serverLevel) {
        if (constraintId == null) return false;
        return ConstraintTracker.constraintExists(serverLevel, constraintId);
    }

    private double getRawMaxRopeLength() {
        ItemStack ropeStack = ropeInventory.getStackInSlot(0);
        if (ropeStack.isEmpty()) return 0.0;

        double ropePerItem;
        if (ropeStack.getItem() == VStuffItems.LEAD_CONSTRAINT_ITEM.get()) {
            ropePerItem = 2.0;
        } else if (ropeStack.getItem() == Items.LEAD) {
            ropePerItem = 1.5;
        } else if (ropeStack.getItem() == Items.STRING) {
            ropePerItem = 1.0;
        } else {
            return 0.0;
        }

        return ropeStack.getCount() * ropePerItem;
    }
    private void forceConstraintRefresh(Player player) {
        if (constraintId != null && level instanceof ServerLevel serverLevel) {
            System.out.println("Force refreshing constraint " + constraintId);

            // Store current state
            double oldLength = currentRopeLength;
            boolean hadTarget = hasTarget;
            BlockPos oldTargetPos = targetPos;

            // Clear everything
            removeExistingConstraint();
            ropeStateInitialized = false;

            // Restore state
            hasTarget = hadTarget;
            targetPos = oldTargetPos;
            currentRopeLength = oldLength;

            if (hasTarget && hasRope()) {
                createConstraint(player);
            }
        }
    }


    private void restoreConstraintAfterLoad(ServerLevel serverLevel) {
        try {
            if (targetPos == null || shipA == null || shipB == null || localPosA == null || localPosB == null) {
                System.out.println("Missing constraint data, cannot restore. Clearing constraint ID.");
                constraintId = null;
                ropeStateInitialized = false;
                setChanged();
                return;
            }

            Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, getBlockPos());
            Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, targetPos);

            Long newShipA = pulleyShip != null ? Long.valueOf(pulleyShip.getId()) : getGroundBodyId(serverLevel);
            Long newShipB = targetShip != null ? Long.valueOf(targetShip.getId()) : getGroundBodyId(serverLevel);

            Vector3d newLocalPosA = getLocalPosition(serverLevel, getBlockPos(), pulleyShip, newShipA);
            Vector3d newLocalPosB = getLocalPosition(serverLevel, targetPos, targetShip, newShipB);

            double currentRopeInInventory = getRawMaxRopeLength();
            double totalRopeCapacity = baseRopeLength + currentRopeInInventory;
            double restoredLength = Math.min(currentRopeLength, totalRopeCapacity);
            double shouldBeConsumed = Math.max(0, restoredLength - baseRopeLength);

            shipA = newShipA;
            shipB = newShipB;
            localPosA = newLocalPosA;
            localPosB = newLocalPosB;
            consumedRopeLength = shouldBeConsumed;

            System.out.println("Restoring constraint with updated data:");
            System.out.println("  - ShipA: " + shipA + " -> " + newShipA);
            System.out.println("  - ShipB: " + shipB + " -> " + newShipB);
            System.out.println("  - Length: " + restoredLength);
            System.out.println("  - Consumed: " + consumedRopeLength);

            if (constraintId != null) {
                try {
                    VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(constraintId);
                    ConstraintTracker.removeConstraintWithPersistence(serverLevel, constraintId);
                } catch (Exception e) {
                    System.out.println("Old constraint was already gone: " + e.getMessage());
                }
            }

            VSRopeConstraint restoredConstraint = new VSRopeConstraint(
                    shipA.longValue(), shipB.longValue(),
                    1e-9,
                    localPosA, localPosB,
                    900000000,
                    restoredLength
            );

            Integer newConstraintId = VSGameUtilsKt.getShipObjectWorld(serverLevel).createNewConstraint(restoredConstraint);
            if (newConstraintId != null) {
                constraintId = newConstraintId;
                currentRopeLength = restoredLength;
                ropeStateInitialized = true;
                isRopeRendering = true;

                ConstraintTracker.addConstraintWithPersistence(serverLevel, constraintId, shipA, shipB,
                        localPosA, localPosB, currentRopeLength, 1e-9, 50000000,
                        ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY, getBlockPos(),
                        new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.BASIC, "vstuff.ropes.normal"));

                System.out.println("Successfully restored constraint " + constraintId + " with length " + currentRopeLength);
                setChanged();
                sendData();

            } else {
                System.err.println("Failed to restore constraint after world load");
                constraintId = null;
                ropeStateInitialized = false;
                setChanged();
            }
        } catch (Exception e) {
            System.err.println("Error restoring constraint after load: " + e.getMessage());
            e.printStackTrace();
            constraintId = null;
            ropeStateInitialized = false;
            setChanged();
        }
    }

    public void onBlockRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            cleanupAllConstraints(serverLevel);
        }
    }
    @Override
    public void invalidate() {
        super.invalidate();
        if (level instanceof ServerLevel serverLevel) {
            cleanupAllConstraints(serverLevel);
        }
    }

    public void removeExistingConstraint(boolean force) {
        wasCut = true;  // mark pulley as cut
        if (isManualMode() && !force) {
            System.out.println("MANUAL MODE: Skipping removeExistingConstraint");
            return;
        }

        if (constraintId != null && level instanceof ServerLevel serverLevel) {
            ConstraintTracker.removeConstraintWithPersistence(serverLevel, constraintId);
            constraintId = null;
            setChanged();
            sendData();
            System.out.println("Removed constraint and cleaned up");
        }

        this.previewTargetPos = null;
        this.previewAttachVec = null;

        if (!isManualMode()) {
            this.currentRopeLength = baseRopeLength;
            this.hasTarget = false;
            this.targetPos = null;
        }
    }





    public void cleanupAllConstraints(ServerLevel serverLevel) {
        if (constraintId != null) {
            try {
                VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(constraintId);
                ConstraintTracker.removeConstraintWithPersistence(serverLevel, constraintId);
                System.out.println("Cleaned up constraint " + constraintId);
            } catch (Exception e) {
                System.out.println("Constraint " + constraintId + " was already gone");
            }
            constraintId = null;
        }

        isRopeRendering = false;


        ConstraintTracker.cleanupOrphanedConstraints(serverLevel, getBlockPos());
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        Item leadItem = VStuffItems.LEAD_CONSTRAINT_ITEM.get();

        if (player.isShiftKeyDown()) {
            waitingForTarget = true;
            setChanged();
            sendData();

            PhysPulleyItem.setWaitingPulley(player, this);


            player.sendSystemMessage(Component.literal("§aPulley manual targeting mode enabled. Next rope click sets target."));

            return InteractionResult.SUCCESS;
        }



        if (heldItem.getItem() == leadItem) {
            if (heldItem.getCount() > 0) {
                return insertRope(player, hand, heldItem);
            } else {
                //  player.sendSystemMessage(Component.literal("§cCannot insert rope, none available!"));
                return InteractionResult.FAIL;
            }
        }


        if (constraintId != null) {
            //   player.sendSystemMessage(Component.literal("§aCurrent rope length: " +
            //         String.format("%.1f", currentRopeLength) + " / " +
            //         String.format("%.1f", getRawMaxRopeLength()) + " blocks"));
        }

        return InteractionResult.PASS;
    }




    public void createManualConstraint() {
        if (level.isClientSide) {
            System.out.println("MANUAL MODE: Skipping constraint creation on client side");
            return;
        }



        this.setManualMode(true);


        System.out.println("MANUAL MODE: Starting constraint creation");

        if (!hasTarget) {
            System.out.println("MANUAL MODE: No target set, cannot create constraint");
            return;
        }
        if (!hasRope()) {
            System.out.println("MANUAL MODE: No rope available, cannot create constraint");
            return;
        }
        if (constraintId != null) {
            System.out.println("MANUAL MODE: Constraint already exists with ID: " + constraintId);
            return;
        }

        try {
            ServerLevel serverLevel = (ServerLevel) level;

            Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, getBlockPos());
            Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, targetPos);

            if (pulleyShip == null) {
                System.out.println("MANUAL MODE: pulleyShip is null");
            } else {
                System.out.println("MANUAL MODE: pulleyShip ID = " + pulleyShip.getId());
            }

            if (targetShip == null) {
                System.out.println("MANUAL MODE: targetShip is null");
            } else {
                System.out.println("MANUAL MODE: targetShip ID = " + targetShip.getId());
            }

            shipA = pulleyShip != null ? Long.valueOf(pulleyShip.getId()) : getGroundBodyId(serverLevel);
            shipB = targetShip != null ? Long.valueOf(targetShip.getId()) : getGroundBodyId(serverLevel);

            System.out.println("MANUAL MODE: Using shipA ID = " + shipA);
            System.out.println("MANUAL MODE: Using shipB ID = " + shipB);

            localPosA = getLocalPosition(serverLevel, getBlockPos(), pulleyShip, shipA);
            localPosB = getLocalPosition(serverLevel, targetPos, targetShip, shipB);

            System.out.println("MANUAL MODE: localPosA = " + localPosA);
            System.out.println("MANUAL MODE: localPosB = " + localPosB);

            Vector3d pulleyWorldPos = getWorldPosition(serverLevel, getBlockPos(), pulleyShip);
            Vector3d targetWorldPos = getWorldPosition(serverLevel, targetPos, targetShip);

            System.out.println("MANUAL MODE: pulleyWorldPos = " + pulleyWorldPos);
            System.out.println("MANUAL MODE: targetWorldPos = " + targetWorldPos);

            double distance = pulleyWorldPos.distance(targetWorldPos);
            System.out.println("MANUAL MODE: Distance between pulley and target = " + distance);

            double maxAvailable = getRawMaxRopeLength();
            System.out.println("MANUAL MODE: maxAvailable rope length = " + maxAvailable);


            double targetLength = Math.min(distance + 1.0, maxAvailable);
            targetLength = Math.max(targetLength, minRopeLength);
            System.out.println("MANUAL MODE: Calculated target rope length = " + targetLength);

            this.targetRopeLength = targetLength;
            this.isExtending = true;


            double initialLength = targetLength;
            this.currentRopeLength = initialLength;

            VSRopeConstraint constraint = new VSRopeConstraint(
                    shipA.longValue(), shipB.longValue(),
                    1e-9, localPosA, localPosB, 900000000, initialLength
            );






            Integer newConstraintId = VSGameUtilsKt.getShipObjectWorld(serverLevel).createNewConstraint(constraint);

            System.out.println("MANUAL MODE: createNewConstraint returned ID: " + newConstraintId);

            if (newConstraintId != null) {
                constraintId = newConstraintId;
                consumedRopeLength = Math.max(0, currentRopeLength - baseRopeLength);
                ropeStateInitialized = true;
                isRopeRendering = true;

                ConstraintTracker.addConstraintWithPersistence(serverLevel, constraintId, shipA, shipB,
                        localPosA, localPosB, currentRopeLength, 1e-9, 15000000.0,
                        ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY, getBlockPos(),
                        new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.BASIC, "vstuff.ropes.normal"));

                setChanged();
                sendData();

                System.out.println("MANUAL MODE: Successfully created constraint with ID " + constraintId);
            } else {
                System.err.println("MANUAL MODE: Failed to create constraint (null ID returned)");
            }
        } catch (Exception e) {
            System.err.println("MANUAL MODE: Exception while creating constraint: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void setManualTarget(BlockPos targetPos) {
        if (this.level.isClientSide) return;
        this.targetPos = targetPos;
        this.hasTarget = true;
        this.waitingForTarget = false;

        createManualConstraint();

        this.isRopeRendering = true;
        setChanged();
        sendData();
    }

    private InteractionResult handleConstraintCreation(Player player) {
        if (constraintId != null) {
            player.sendSystemMessage(Component.literal("§aConstraint active! Length: " +
                    String.format("%.1f", currentRopeLength) + " blocks"));
            return InteractionResult.SUCCESS;
        } else if (hasTarget && hasRope()) {
            createConstraint(player);
            return InteractionResult.SUCCESS;
        } else if (!hasRope()) {
            player.sendSystemMessage(Component.literal("§cNeed rope to create constraint!"));
            return InteractionResult.SUCCESS;
        } else if (!hasTarget) {
            player.sendSystemMessage(Component.literal("§cNeed target position! Shift+Right-click to set target."));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult insertRope(Player player, InteractionHand hand, ItemStack heldItem) {
        System.out.println("Attempting to insert rope: held count = " + heldItem.getCount());
        ItemStack remainder = ropeInventory.insertItem(0, heldItem, false);
        System.out.println("After insert: remainder count = " + remainder.getCount());

        if (remainder.getCount() != heldItem.getCount()) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, remainder);
            }

            int inserted = heldItem.getCount() - remainder.getCount();

            if (hasTarget && constraintId == null && hasRope()) {
                System.out.println("Creating constraint after rope insert.");
                createConstraint(player);
            }
            return InteractionResult.SUCCESS;
        }
        System.out.println("No rope inserted.");
        return InteractionResult.PASS;
    }

    private void initializeRopeInventory() {
        ItemStack existingStack = ItemStack.EMPTY;
        if (ropeInventory != null) {
            existingStack = ropeInventory.getStackInSlot(0);
        }

        ropeInventory = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() == VStuffItems.LEAD_CONSTRAINT_ITEM.get() ||
                        stack.getItem() == Items.STRING ||
                        stack.getItem() == Items.LEAD;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                sendData();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }
        };

        if (!existingStack.isEmpty()) {
            ropeInventory.setStackInSlot(0, existingStack);
        }

        if (ropeInventoryOptional != null) {
            ropeInventoryOptional.invalidate();
        }
        ropeInventoryOptional = LazyOptional.of(() -> ropeInventory);
    }
    private boolean createConstraint(Player player) {
        if (!hasTarget || !hasRope()) {
            //   player.sendSystemMessage(Component.literal("§cNeed both target and rope to create constraint!"));
            return false;
        }

        if (constraintId != null) {
            //    player.sendSystemMessage(Component.literal("§cConstraint already exists!"));
            return false;
        }

        try {
            ServerLevel serverLevel = (ServerLevel) level;

            Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, getBlockPos());
            Ship targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, targetPos);



            return createConstraintImmediate(serverLevel, player);

        } catch (Exception e) {
            System.err.println("Error creating constraint: " + e.getMessage());
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("§cError creating constraint: " + e.getMessage()));
            return false;
        }
    }








    private boolean hasRope() {
        ItemStack ropeStack = ropeInventory.getStackInSlot(0);
        System.out.println("hasRope check: stack = " + ropeStack + ", count = " + ropeStack.getCount());
        return !ropeStack.isEmpty() && ropeStack.getCount() > 0;
    }

    private double getMaxRopeLength() {
        ItemStack ropeStack = ropeInventory.getStackInSlot(0);
        if (ropeStack.isEmpty()) {
            return baseRopeLength;
        }

        double ropePerItem = getRopePerItem(ropeStack.getItem());
        double availableFromItems = ropeStack.getCount() * ropePerItem;

        return baseRopeLength + availableFromItems;
    }



    private double getAvailableRopeForExtension() {
        double maxTotal = getMaxRopeLength();
        double alreadyUsed = Math.max(0, currentRopeLength - baseRopeLength);
        return maxTotal - currentRopeLength;
    }


    private double getRopePerItem(Item item) {
        if (item == VStuffItems.LEAD_CONSTRAINT_ITEM.get()) {
            return 2.0;
        } else if (item == Items.LEAD) {
            return 1.5;
        } else if (item == Items.STRING) {
            return 1.0;
        }
        return 0.0;
    }
    private boolean isValidRopeItem(ItemStack stack) {
        return stack.getItem() == Items.STRING ||
                stack.getItem() == Items.LEAD ||
                stack.getItem() == VStuffItems.LEAD_CONSTRAINT_ITEM.get();
    }


    private Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    private Vector3d getWorldPosition(ServerLevel level, BlockPos pos, Ship ship) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (ship != null) {
            Vector3d worldPos = new Vector3d();
            ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
            return worldPos;
        }
        return localPos;
    }


    private Vector3d getLocalPosition(ServerLevel level, BlockPos pos, Ship pulleyShip, Long targetShipId) {
        Vector3d blockWorldPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (targetShipId.equals(getGroundBodyId(level))) {
            return blockWorldPos;
        }

        Ship targetShip = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(targetShipId);
        if (targetShip != null) {
            Vector3d worldPos = new Vector3d();

            if (pulleyShip != null) {
                pulleyShip.getTransform().getShipToWorld().transformPosition(blockWorldPos, worldPos);
            } else {
                worldPos.set(blockWorldPos);
            }

            Vector3d localPos = new Vector3d();
            targetShip.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
            return localPos;
        }

        return blockWorldPos;
    }


    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        tooltip.add(Component.literal(" "));


        ItemStack ropeStack = ropeInventory.getStackInSlot(0);
        if (!ropeStack.isEmpty()) {
            double maxLength = getMaxRopeLength() - baseRopeLength;
            tooltip.add(Component.literal("Rope: " + ropeStack.getCount() + "/64 (" +
                            String.format("%.1f", maxLength) + " blocks)")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("No Rope - Right-click with rope item")
                    .withStyle(ChatFormatting.RED));
        }

        if (constraintId != null) {
            tooltip.add(Component.literal("Length: " + String.format("%.1f", currentRopeLength) + " blocks")
                    .withStyle(ChatFormatting.BLUE));
            float speed = getSpeed();
            if (Math.abs(speed) > 4) {
                String direction = speed > 0 ? "Extending" : "Retracting";
                tooltip.add(Component.literal("Status: " + direction + " (" + String.format("%.1f", speed))
                        .withStyle(ChatFormatting.GREEN));

            } else {
                tooltip.add(Component.literal("Status: Idle")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            if (hasTarget && hasRope()) {
                tooltip.add(Component.literal("Ready - Right-click to create constraint")
                        .withStyle(ChatFormatting.YELLOW));
            } else if (waitingForTarget) {
                tooltip.add(Component.literal("§e§lClick any block to set target")
                        .withStyle(ChatFormatting.YELLOW));
            } else if (!hasRope()){
                tooltip.add(Component.literal("Need rope and a target")
                        .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.literal("No Target - Shift+Right-click to set")
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        return true;
    }



    private InteractionResult handleTargetSetting(Player player) {
        if (!waitingForTarget) {
            waitingForTarget = true;
            hasTarget = false;
            targetPos = null;
            player.sendSystemMessage(Component.literal("§aClick a block to set the rope target!"));
            setChanged();
            sendData();
            return InteractionResult.SUCCESS;
        } else {
            waitingForTarget = false;
            player.sendSystemMessage(Component.literal("§cTarget setting cancelled."));
            setChanged();
            sendData();
            return InteractionResult.SUCCESS;
        }
    }




    private boolean createConstraintImmediate(ServerLevel serverLevel, Player player) {
        try {
            Ship pulleyShip = null;
            Ship targetShip = null;

            for (int attempt = 0; attempt < 3; attempt++) {
                pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, getBlockPos());
                targetShip = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, targetPos);

                if (pulleyShip != null || targetShip != null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    break;
                }
            }

            shipA = pulleyShip != null ? Long.valueOf(pulleyShip.getId()) : getGroundBodyId(serverLevel);
            shipB = targetShip != null ? Long.valueOf(targetShip.getId()) : getGroundBodyId(serverLevel);

            localPosA = getLocalPosition(serverLevel, getBlockPos(), pulleyShip, shipA);
            localPosB = getLocalPosition(serverLevel, targetPos, targetShip, shipB);

            Vector3d pulleyWorldPos = getWorldPosition(serverLevel, getBlockPos(), pulleyShip);
            Vector3d targetWorldPos = getWorldPosition(serverLevel, targetPos, targetShip);
            double distance = pulleyWorldPos.distance(targetWorldPos);

            double maxAvailable = getRawMaxRopeLength();
            double initialLength = Math.min(distance + 2.0, maxAvailable);
            initialLength = Math.max(initialLength, minRopeLength);
            currentRopeLength = initialLength;

            double compliance = (pulleyShip != null || targetShip != null) ? 1.2e-7 : 8e-8;
            double maxForce = (pulleyShip != null || targetShip != null) ? 8000000.0 : 12000000.0;

            VSRopeConstraint constraint = new VSRopeConstraint(
                    shipA.longValue(), shipB.longValue(),
                    compliance,
                    localPosA, localPosB,
                    maxForce,
                    currentRopeLength
            );

            constraintId = VSGameUtilsKt.getShipObjectWorld(serverLevel).createNewConstraint(constraint);

            if (constraintId != null) {
                consumedRopeLength = 0.0;
                ropeStateInitialized = true;

                ConstraintTracker.addConstraintWithPersistence(serverLevel, constraintId, shipA, shipB,
                        localPosA, localPosB, currentRopeLength, compliance, maxForce,
                        ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY, getBlockPos(),
                        new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.BASIC, "vstuff.ropes.normal"));

                setChanged();
                sendData();

                String pulleyType = pulleyShip != null ? "ship" : "ground";
                String targetType = targetShip != null ? "ship" : "ground";

                //    player.sendSystemMessage(Component.literal("§aConstraint created! " + pulleyType + " to " + targetType +
                //            " - Length: " + String.format("%.1f", currentRopeLength)));

                System.out.println("Created rope pulley constraint " + constraintId +
                        " (" + pulleyType + " to " + targetType + ") with length " + currentRopeLength);
                forceConstraintRefresh(player);

                return true;
            } else {
                player.sendSystemMessage(Component.literal("§cFailed to create constraint!"));
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error in immediate constraint creation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private double calculateWorldDistance(BlockPos pos1, BlockPos pos2) {
        if (level == null) return Double.MAX_VALUE;

        try {
            Vector3d worldPos1 = getWorldPosition(pos1);
            Vector3d worldPos2 = getWorldPosition(pos2);

            double distance = worldPos1.distance(worldPos2);
            System.out.println("World positions - Pos1: " + worldPos1 + ", Pos2: " + worldPos2 + ", Distance: " + distance);

            return distance;
        } catch (Exception e) {
            System.err.println("Error calculating world distance: " + e.getMessage());
            return Math.sqrt(pos1.distSqr(pos2));
        }
    }

    private Vector3d getWorldPosition(BlockPos pos) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        Ship shipObject = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, pos);
        if (shipObject != null) {
            Vector3d worldPos = new Vector3d();
            shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
            return worldPos;
        }

        return localPos;
    }


    private boolean consumeRope(double extensionAmount) {
        return true;
    }



    private void actuallyConsumeRope(double extensionAmount) {
    }

    private void returnRope(double amount) {
    }


    public void recreateConstraintOnLoad(ServerLevel serverLevel) {
        if (restoring) {
            return;
        }
    }








    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("RopeInventory", ropeInventory.serializeNBT());

        if (targetPos != null) {
            tag.putLong("TargetPos", targetPos.asLong());
        }

        // Aghytyytf
        tag.putInt("PulleyMode", MODE != null ? MODE.getValue() : 0);

        tag.putBoolean("HasTarget", hasTarget);
        tag.putBoolean("WaitingForTarget", waitingForTarget);

        tag.putDouble("ConsumedRopeLength", consumedRopeLength);
        tag.putDouble("BaseRopeLength", baseRopeLength);
        tag.putBoolean("RopeStateInitialized", ropeStateInitialized);

        if (constraintId != null) {
            tag.putInt("ConstraintId", constraintId);
        }

        tag.putDouble("CurrentRopeLength", currentRopeLength);
        tag.putDouble("MinRopeLength", minRopeLength);

        tag.putBoolean("ManualMode", isManualMode());


        // Save constraint data
        if (shipA != null && shipB != null && localPosA != null && localPosB != null) {
            tag.putLong("ShipA", shipA);
            tag.putLong("ShipB", shipB);
            tag.putDouble("LocalPosAX", localPosA.x);
            tag.putDouble("LocalPosAY", localPosA.y);
            tag.putDouble("LocalPosAZ", localPosA.z);
            tag.putDouble("LocalPosBX", localPosB.x);
            tag.putDouble("LocalPosBY", localPosB.y);
            tag.putDouble("LocalPosBZ", localPosB.z);
        }

        tag.putBoolean("DataSavedProperly", true);
        tag.putBoolean("IsLowering", isLowering);
        tag.putBoolean("IsRopeRendering", isRopeRendering);

        tag.putBoolean("WasCut", wasCut);

        System.out.println("Saving rope state - Length: " + currentRopeLength +
                ", Consumed: " + consumedRopeLength +
                ", Constraint: " + constraintId +
                ", Mode: " + (MODE != null ? MODE.getValue() : "null"));
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        ropeInventory.deserializeNBT(tag.getCompound("RopeInventory"));

        if (tag.contains("TargetPos")) {
            targetPos = BlockPos.of(tag.getLong("TargetPos"));
        }

        if (tag.contains("PulleyMode")) {
            int savedMode = tag.getInt("PulleyMode");
            if (savedMode >= 0 && savedMode < MODES.length) {
                loadedPulleyMode = savedMode;
            }
        }
        isLowering = tag.getBoolean("IsLowering");
        isRopeRendering = tag.getBoolean("IsRopeRendering");

        wasCut = tag.getBoolean("WasCut");

        manualMode = tag.getBoolean("ManualMode");

        lastMode = getPulleyMode();

        hasTarget = tag.getBoolean("HasTarget");
        waitingForTarget = tag.getBoolean("WaitingForTarget");

        consumedRopeLength = tag.contains("ConsumedRopeLength") ? tag.getDouble("ConsumedRopeLength") : 0.0;
        baseRopeLength = tag.contains("BaseRopeLength") ? tag.getDouble("BaseRopeLength") : 2.0;
        ropeStateInitialized = tag.getBoolean("RopeStateInitialized");

        if (tag.contains("ConstraintId")) {
            constraintId = tag.getInt("ConstraintId");
            System.out.println("Loaded constraint ID: " + constraintId);
        } else {
            constraintId = null;
        }

        currentRopeLength = tag.contains("CurrentRopeLength") ?
                Math.max(tag.getDouble("CurrentRopeLength"), minRopeLength) : minRopeLength;

        minRopeLength = tag.contains("MinRopeLength") ?
                Math.max(tag.getDouble("MinRopeLength"), 0.1) : 0.1;

        if (tag.contains("ShipA")) {
            shipA = tag.getLong("ShipA");
            shipB = tag.getLong("ShipB");
            localPosA = new Vector3d(
                    tag.getDouble("LocalPosAX"),
                    tag.getDouble("LocalPosAY"),
                    tag.getDouble("LocalPosAZ")
            );
            localPosB = new Vector3d(
                    tag.getDouble("LocalPosBX"),
                    tag.getDouble("LocalPosBY"),
                    tag.getDouble("LocalPosBZ")
            );
        }



        System.out.println("Loaded rope state - Length: " + currentRopeLength +
                ", Consumed: " + consumedRopeLength +
                ", Constraint: " + constraintId +
                ", Mode: " + (MODE != null ? MODE.getValue() : "null") +
                ", HasTarget: " + hasTarget);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("IsLowering", isLowering);
        tag.putBoolean("IsRopeRendering", isRopeRendering);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        isLowering = tag.getBoolean("IsLowering");
        isRopeRendering = tag.getBoolean("IsRopeRendering");
    }


    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ropeInventoryOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        ropeInventoryOptional.invalidate();
    }

    @Override
    public float calculateStressApplied() {
        return 32f;
    }


}
