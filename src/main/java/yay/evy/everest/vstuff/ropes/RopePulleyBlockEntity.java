package yay.evy.everest.vstuff.ropes;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.block.ModBlockEntities;

import java.util.List;

public class RopePulleyBlockEntity extends KineticBlockEntity {
    private static final double MIN_ROPE_LENGTH = 0.1;
    private static final double MAX_ROPE_LENGTH = 64.0;
    private static final double EXTENSION_RATE = 0.1;
    private static final double MIN_SPEED_THRESHOLD = 8.0;
    private static final double INITIAL_ROPE_LENGTH = 0.0;
    private static final int CONSTRAINT_UPDATE_COOLDOWN = 10;

    private Integer vsConstraintId = null;
    private int constraintUpdateTimer = 0;
    private boolean needsConstraintUpdate = false;
    private double currentRopeLength = INITIAL_ROPE_LENGTH;
    private BlockPos anchorPoint = null;
    boolean isExtending = false;
    private boolean isRetracting = false;
    private double accumulatedRotation = 0.0;
    private boolean hasFoundAnchor = false;
    private boolean isInitialized = false;
    private double lastRopeLength = currentRopeLength;

    public RopePulleyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROPE_PULLEY.get(), pos, state);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public boolean isSpeedRequirementFulfilled() {
        return Math.abs(getSpeed()) >= MIN_SPEED_THRESHOLD;
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        if (level != null && !level.isClientSide) {
            handleRotationInput();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        if (!level.isClientSide) {
            if (!isInitialized) {
                initialize();
                isInitialized = true;
            }

            handleRotationInput();

            if (Math.abs(currentRopeLength - lastRopeLength) > 0.01) {
                checkForAnchorPoint();
            }

            if (constraintUpdateTimer > 0) {
                constraintUpdateTimer--;
            }

            if (needsConstraintUpdate && constraintUpdateTimer <= 0) {
                updateConstraint(); // Always update constraint
                needsConstraintUpdate = false;
                constraintUpdateTimer = CONSTRAINT_UPDATE_COOLDOWN;
            }

            lastRopeLength += (currentRopeLength - lastRopeLength) * 0.25;
        }
    }

    private void updateConstraint() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        removeExistingConstraint();

        if (currentRopeLength > 0.1) {
            if (hasFoundAnchor && anchorPoint != null) {
                vsConstraintId = RopePhysicsManager.createRopeConstraint(
                        serverLevel, worldPosition, anchorPoint, currentRopeLength);
            } else {
                Vec3 ropeStart = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
                Vec3 ropeEnd = new Vec3(ropeStart.x, ropeStart.y - currentRopeLength, ropeStart.z);
                BlockPos virtualAnchor = new BlockPos((int) Math.floor(ropeEnd.x), (int) Math.floor(ropeEnd.y), (int) Math.floor(ropeEnd.z));

                vsConstraintId = RopePhysicsManager.createFreeDanglingRopeConstraint(
                        serverLevel, worldPosition, virtualAnchor, currentRopeLength);
            }

            if (vsConstraintId != null) {
                System.out.println("Created constraint ID: " + vsConstraintId +
                        " (anchored: " + hasFoundAnchor + ", extending: " + isExtending +
                        ", retracting: " + isRetracting + ")");
            }
        }
    }


    private Vec3 getRopeStartPosition() {
        return new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.1, worldPosition.getZ() + 0.5);
    }

    public Vec3[] getPhysicsRopePositions() {
        if (currentRopeLength <= 0.1) {
            return null; // No rope to render
        }

        int segments = Math.max(3, (int)(currentRopeLength * 2));
        Vec3[] positions = new Vec3[segments];

        Vec3 start = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);

        Vec3 end;
        if (hasFoundAnchor && anchorPoint != null) {
            end = new Vec3(anchorPoint.getX() + 0.5, anchorPoint.getY() + 1.0, anchorPoint.getZ() + 0.5);
        } else {
            end = new Vec3(start.x, start.y - currentRopeLength, start.z);
        }

        for (int i = 0; i < segments; i++) {
            float t = (float) i / (segments - 1);
            Vec3 linearPos = start.lerp(end, t);

            double sagAmount = 0.2 * Math.sin(t * Math.PI) * Math.min(currentRopeLength * 0.1, 1.0);
            if (!hasFoundAnchor) {
                sagAmount *= 1.5;
            }

            positions[i] = new Vec3(
                    linearPos.x + sagAmount * 0.5,
                    linearPos.y - sagAmount,
                    linearPos.z
            );
        }

        return positions;
    }


    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        write(tag, true);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        read(tag, true);
    }

    public void syncToClient() {
        if (level instanceof ServerLevel serverLevel) {
            setChanged();
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public double getInterpolatedRopeLength() {
        return lastRopeLength;
    }

    public void initialize() {
        if (currentRopeLength <= 0.0) {
            currentRopeLength = 0.0;
        }
        lastRopeLength = currentRopeLength;

        // Create initial constraint if needed
        if (currentRopeLength > 0.1) {
            needsConstraintUpdate = true;
        }
    }

    private void handleRotationInput() {
        float speed = getSpeed();

        if (Math.abs(speed) < MIN_SPEED_THRESHOLD) {
            isExtending = false;
            isRetracting = false;
            return;
        }

        double extensionAmount = Math.abs(speed) * EXTENSION_RATE * 0.05;

        if (speed < -MIN_SPEED_THRESHOLD) {
            isExtending = true;
            isRetracting = false;
            extendRope(extensionAmount);
        } else if (speed > MIN_SPEED_THRESHOLD) {
            isExtending = false;
            isRetracting = true;
            retractRope(extensionAmount);
        }
    }
    public double getPreviousRopeLength() {
        return lastRopeLength;
    }


    private BlockPos findBlockAtRopeEnd(double ropeLength) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        Long pulleyShipId = getShipIdAtPos(serverLevel, worldPosition);
        Vec3 ropeEnd;

        if (pulleyShipId != null) {
            Vector3d pulleyWorldPos = getWorldPosition(serverLevel, worldPosition, pulleyShipId);
            Vector3d ropeEndWorld = new Vector3d(pulleyWorldPos.x, pulleyWorldPos.y - ropeLength, pulleyWorldPos.z);
            ropeEnd = new Vec3(ropeEndWorld.x, ropeEndWorld.y, ropeEndWorld.z);
        } else {
            Vec3 ropeStart = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            ropeEnd = new Vec3(ropeStart.x, ropeStart.y - ropeLength, ropeStart.z);
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = new BlockPos(
                            (int) Math.floor(ropeEnd.x) + dx,
                            (int) Math.floor(ropeEnd.y) + dy,
                            (int) Math.floor(ropeEnd.z) + dz
                    );

                    if (checkPos.getY() >= level.getMinBuildHeight() &&
                            checkPos.getY() <= level.getMaxBuildHeight()) {
                        BlockState state = level.getBlockState(checkPos);
                        if (!state.isAir() && isValidAnchorBlock(state, checkPos)) {
                            Long blockShipId = getShipIdAtPos(serverLevel, checkPos);
                            Vector3d blockWorldPos = getWorldPosition(serverLevel, checkPos, blockShipId);
                            blockWorldPos.add(0, 0.5, 0); // Top of block

                            double distance = ropeEnd.distanceTo(new Vec3(blockWorldPos.x, blockWorldPos.y, blockWorldPos.z));
                            if (distance <= 1.5) {
                                System.out.println("Found anchor block at " + checkPos +
                                        " (world pos: " + ropeEnd + ", distance: " + distance +
                                        ", ship: " + blockShipId + ")");
                                return checkPos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    private void extendRope(double amount) {
        double actualAmount = Math.max(amount * 2.0, 0.05);
        double newLength = Math.min(currentRopeLength + actualAmount, MAX_ROPE_LENGTH);

        BlockPos hitBlock = findBlockAtRopeEnd(newLength);
        if (hitBlock != null) {
            double blockHitLength;
            if (level instanceof ServerLevel serverLevel) {
                Long pulleyShipId = getShipIdAtPos(serverLevel, worldPosition);
                Long anchorShipId = getShipIdAtPos(serverLevel, hitBlock);
                Vector3d pulleyWorldPos = getWorldPosition(serverLevel, worldPosition, pulleyShipId);
                Vector3d blockWorldPos = getWorldPosition(serverLevel, hitBlock, anchorShipId);
                blockWorldPos.add(0, 0.5, 0);
                blockHitLength = pulleyWorldPos.distance(blockWorldPos);
            } else {
                blockHitLength = Math.sqrt(
                        Math.pow(worldPosition.getX() + 0.5 - (hitBlock.getX() + 0.5), 2) +
                                Math.pow(worldPosition.getY() + 0.5 - (hitBlock.getY() + 1.0), 2) +
                                Math.pow(worldPosition.getZ() + 0.5 - (hitBlock.getZ() + 0.5), 2)
                );
            }

            if (blockHitLength > currentRopeLength && blockHitLength <= newLength) {
                currentRopeLength = blockHitLength;
                anchorPoint = hitBlock;
                hasFoundAnchor = true;
                needsConstraintUpdate = true;
                setChanged();
                syncToClient();
                return;
            }
        }

        if (Math.abs(newLength - currentRopeLength) > 0.001) {
            currentRopeLength = newLength;
            checkForAnchorPoint();
            needsConstraintUpdate = true;
            setChanged();
            syncToClient();
        }
    }

    private void retractRope(double amount) {
        double actualAmount = Math.max(amount * 2.0, 0.05);
        double newLength = Math.max(currentRopeLength - actualAmount, 0.0);

        if (Math.abs(newLength - currentRopeLength) > 0.001) {
            currentRopeLength = newLength;

            if (hasFoundAnchor && anchorPoint != null) {
                double distanceToAnchor = Math.sqrt(
                        Math.pow(worldPosition.getX() + 0.5 - (anchorPoint.getX() + 0.5), 2) +
                                Math.pow(worldPosition.getY() + 0.5 - (anchorPoint.getY() + 1.0), 2) +
                                Math.pow(worldPosition.getZ() + 0.5 - (anchorPoint.getZ() + 0.5), 2)
                );

                if (currentRopeLength < distanceToAnchor - 0.5) {
                    hasFoundAnchor = false;
                    anchorPoint = null;
                }
            }

            if (currentRopeLength <= 0.01) {
                removeExistingConstraint();
                needsConstraintUpdate = false;
            } else {
                needsConstraintUpdate = true;
            }

            setChanged();
            syncToClient();
        }
    }

    private void checkForAnchorPoint() {
        if (currentRopeLength < 0.5) return;

        BlockPos hitBlock = findBlockAtRopeEnd(currentRopeLength);
        if (hitBlock != null && !hitBlock.equals(anchorPoint)) {
            BlockState state = level.getBlockState(hitBlock);
            if (isValidAnchorBlock(state, hitBlock)) {
                anchorPoint = hitBlock;
                hasFoundAnchor = true;
                System.out.println("Found anchor at: " + hitBlock + " for rope length: " + currentRopeLength);
                needsConstraintUpdate = true;
            }
        } else if (hasFoundAnchor && anchorPoint != null) {
            BlockState anchorState = level.getBlockState(anchorPoint);
            if (!isValidAnchorBlock(anchorState, anchorPoint)) {
                System.out.println("Lost anchor point - block no longer valid");
                hasFoundAnchor = false;
                anchorPoint = null;
                removeExistingConstraint();
                needsConstraintUpdate = true;
            } else {
                // Check if anchor is still in range
                double currentDistanceToAnchor;
                if (level instanceof ServerLevel serverLevel) {
                    Long pulleyShipId = getShipIdAtPos(serverLevel, worldPosition);
                    Long anchorShipId = getShipIdAtPos(serverLevel, anchorPoint);

                    if (pulleyShipId != null || anchorShipId != null) {
                        Vector3d pulleyWorldPos = getWorldPosition(serverLevel, worldPosition, pulleyShipId);
                        Vector3d anchorWorldPos = getWorldPosition(serverLevel, anchorPoint, anchorShipId);
                        currentDistanceToAnchor = pulleyWorldPos.distance(anchorWorldPos);
                    } else {
                        currentDistanceToAnchor = Math.sqrt(
                                Math.pow(worldPosition.getX() + 0.5 - (anchorPoint.getX() + 0.5), 2) +
                                        Math.pow(worldPosition.getY() + 0.5 - (anchorPoint.getY() + 1.0), 2) +
                                        Math.pow(worldPosition.getZ() + 0.5 - (anchorPoint.getZ() + 0.5), 2)
                        );
                    }
                } else {
                    currentDistanceToAnchor = Math.sqrt(
                            Math.pow(worldPosition.getX() + 0.5 - (anchorPoint.getX() + 0.5), 2) +
                                    Math.pow(worldPosition.getY() + 0.5 - (anchorPoint.getY() + 1.0), 2) +
                                    Math.pow(worldPosition.getZ() + 0.5 - (anchorPoint.getZ() + 0.5), 2)
                    );
                }

                if (currentDistanceToAnchor > currentRopeLength + 1.0) {
                    System.out.println("Lost anchor point - too far away (distance: " + currentDistanceToAnchor + ", rope: " + currentRopeLength + ")");
                    hasFoundAnchor = false;
                    anchorPoint = null;
                    removeExistingConstraint();
                    needsConstraintUpdate = true;
                }
            }
        }
    }

    private boolean isValidAnchorBlock(BlockState state, BlockPos pos) {
        return !state.isAir() &&
                state.isSolidRender(level, pos) &&
                !state.is(net.minecraft.tags.BlockTags.LEAVES) &&
                state.getDestroySpeed(level, pos) >= 0;
    }

    private void maintainRopePhysics() {
        if (!(level instanceof ServerLevel)) return;

        removeExistingConstraint();

        if (currentRopeLength > 0.5) {
            createPhysicsRopeConstraint();
        }
    }


    private double calculateDistanceToBlock(Vec3 ropeEnd, BlockPos blockPos) {
        double dx = ropeEnd.x - (blockPos.getX() + 0.5);
        double dy = ropeEnd.y - (blockPos.getY() + 1.0);
        double dz = ropeEnd.z - (blockPos.getZ() + 0.5);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void createFreeDanglingRopeConstraint() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (constraintUpdateTimer > 0) return;

        removeExistingConstraint();

        Vec3 ropeStart = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
        Vec3 ropeEnd = new Vec3(ropeStart.x, ropeStart.y - currentRopeLength, ropeStart.z);

        BlockPos virtualAnchor = new BlockPos((int) Math.floor(ropeEnd.x), (int) Math.floor(ropeEnd.y), (int) Math.floor(ropeEnd.z));

        vsConstraintId = RopePhysicsManager.createFreeDanglingRopeConstraint(
                serverLevel, worldPosition, virtualAnchor, currentRopeLength
        );

        if (vsConstraintId != null) {
            System.out.println("Created free-dangling rope constraint with ID: " + vsConstraintId);
        }
    }


    private void createPhysicsRopeConstraint() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (constraintUpdateTimer > 0) return;

        removeExistingConstraint();

        if (hasFoundAnchor && anchorPoint != null) {
            vsConstraintId = RopePhysicsManager.createRopeConstraint(
                    serverLevel, worldPosition, anchorPoint, currentRopeLength
            );
            if (vsConstraintId != null) {
                System.out.println("Created rope constraint with ID: " + vsConstraintId);
            }
        } else if (currentRopeLength > 0.5) {
            Vec3 ropeStart = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            Vec3 ropeEnd = new Vec3(ropeStart.x, ropeStart.y - currentRopeLength, ropeStart.z);
            BlockPos virtualAnchor = new BlockPos((int) Math.floor(ropeEnd.x), (int) Math.floor(ropeEnd.y), (int) Math.floor(ropeEnd.z));

            vsConstraintId = RopePhysicsManager.createFreeDanglingRopeConstraint(
                    serverLevel, worldPosition, virtualAnchor, currentRopeLength
            );
            if (vsConstraintId != null) {
                System.out.println("Created free-dangling rope constraint with ID: " + vsConstraintId);
            }
        }
    }


    private double calculateRopeCompliance() {
        return 1e-7;
    }

    private double calculateMaxForce() {
        return 5e7;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("gui.goggles.kinetic_stats")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.builder()
                .add(Component.literal("Rope Length: "))
                .text(ChatFormatting.AQUA + String.format("%.1f", currentRopeLength) + "/" + (int) MAX_ROPE_LENGTH + " blocks")
                .forGoggles(tooltip);

        String status = isExtending ? "Extending" : isRetracting ? "Retracting" : "Idle";
        ChatFormatting statusColor = isExtending ? ChatFormatting.GREEN :
                isRetracting ? ChatFormatting.RED : ChatFormatting.GRAY;

        Lang.builder()
                .add(Component.literal("Status: "))
                .text(statusColor + status)
                .forGoggles(tooltip);

        if (hasFoundAnchor && anchorPoint != null) {
            Lang.builder()
                    .add(Component.literal("Anchored to: "))
                    .text(ChatFormatting.YELLOW + anchorPoint.toShortString())
                    .forGoggles(tooltip);
        } else {
            Lang.builder()
                    .add(Component.literal("Status: "))
                    .text(ChatFormatting.GOLD + "Swaying freely")
                    .forGoggles(tooltip);
        }

        if (Math.abs(getSpeed()) < MIN_SPEED_THRESHOLD && (isExtending || isRetracting)) {
            Lang.builder()
                    .add(Component.literal("Needs more speed: "))
                    .text(ChatFormatting.RED + String.format("%.1f/%.1f RPM", Math.abs(getSpeed()), MIN_SPEED_THRESHOLD))
                    .forGoggles(tooltip);
        }

        return true;
    }

    public void removeExistingConstraint() {
        if (vsConstraintId != null && level instanceof ServerLevel serverLevel) {
            RopePhysicsManager.removeRopeConstraint(serverLevel, vsConstraintId);
            vsConstraintId = null;
        }
    }

    public Integer getVsConstraintId() {
        return vsConstraintId;
    }

    public void resetRope() {
        currentRopeLength = INITIAL_ROPE_LENGTH;
        lastRopeLength = currentRopeLength;
        hasFoundAnchor = false;
        anchorPoint = null;
        removeExistingConstraint();
        setChanged();
    }

    public void showStatus(Player player) {
        String anchorStatus = hasFoundAnchor ? "Anchored at " + anchorPoint.toShortString() : "Swaying freely";
        player.sendSystemMessage(Component.literal(String.format(
                "§6Rope Pulley Status:\n§7Length: %.1f/%.1f blocks\n§7Speed: %.1f RPM\n§7Mode: %s\n§7%s",
                currentRopeLength, MAX_ROPE_LENGTH, getSpeed(),
                isExtending ? "Extending" : isRetracting ? "Retracting" : "Idle",
                anchorStatus
        )));
    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        Ship shipObject = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        return shipObject != null ? shipObject.getId() : null;
    }

    private Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    private Vector3d getWorldPosition(ServerLevel level, BlockPos pos, Long shipId) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d worldPos = new Vector3d();
                shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                return worldPos;
            }
        }
        return localPos;
    }

    private Vector3d convertWorldToLocal(ServerLevel level, Vector3d worldPos, Long shipId) {
        Long groundBodyId = getGroundBodyId(level);
        if (shipId != null && !shipId.equals(groundBodyId)) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d localPos = new Vector3d();
                shipObject.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
                return localPos;
            }
        }
        return new Vector3d(worldPos);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putDouble("currentRopeLength", currentRopeLength);
        tag.putDouble("accumulatedRotation", accumulatedRotation);
        tag.putBoolean("isExtending", isExtending);
        tag.putBoolean("isRetracting", isRetracting);
        tag.putBoolean("hasFoundAnchor", hasFoundAnchor);
        tag.putBoolean("isInitialized", isInitialized);

        if (vsConstraintId != null) {
            tag.putInt("vsConstraintId", vsConstraintId);
        }
        if (anchorPoint != null) {
            tag.putLong("anchorPoint", anchorPoint.asLong());
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        currentRopeLength = tag.contains("currentRopeLength") ? tag.getDouble("currentRopeLength") : INITIAL_ROPE_LENGTH;
        lastRopeLength = currentRopeLength;
        accumulatedRotation = tag.getDouble("accumulatedRotation");
        isExtending = tag.getBoolean("isExtending");
        isRetracting = tag.getBoolean("isRetracting");
        hasFoundAnchor = tag.getBoolean("hasFoundAnchor");
        isInitialized = tag.getBoolean("isInitialized");

        if (tag.contains("vsConstraintId")) {
            vsConstraintId = tag.getInt("vsConstraintId");
        }
        if (tag.contains("anchorPoint")) {
            anchorPoint = BlockPos.of(tag.getLong("anchorPoint"));
        }
    }

    @Override
    public void invalidate() {
        removeExistingConstraint();
        super.invalidate();
    }

    @Override
    public float calculateStressApplied() {
        float baseStress = 4.0f;
        float lengthMultiplier = (float) (currentRopeLength / MAX_ROPE_LENGTH);
        float operationMultiplier = (isExtending || isRetracting) ? 1.5f : 1.0f;
        float anchorMultiplier = hasFoundAnchor ? 1.2f : 0.8f;
        return baseStress * (1.0f + lengthMultiplier) * operationMultiplier * anchorMultiplier;
    }

    // Public getters for renderer and other systems
    public double getCurrentRopeLength() {
        return currentRopeLength;
    }

    public boolean isOperating() {
        return isExtending || isRetracting;
    }

    public boolean hasAnchor() {
        return hasFoundAnchor;
    }

    public BlockPos getAnchorPoint() {
        return anchorPoint;
    }

    public boolean isSwaying() {
        // Rope is "swaying" if it has a constraint (VS will handle the physics)
        return vsConstraintId != null;

    }

}