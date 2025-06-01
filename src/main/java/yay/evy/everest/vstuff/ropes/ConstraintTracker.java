package yay.evy.everest.vstuff.ropes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstraintTracker {

    private static final Map<Integer, RopeConstraintData> activeConstraints = new ConcurrentHashMap<>();

    public static class RopeConstraintData {
        public final Long shipA;
        public final Long shipB;
        public final Vector3d localPosA;
        public final Vector3d localPosB;
        public final double maxLength;
        public final double compliance;
        public final double maxForce;

        public RopeConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
        }

        public Vector3d getWorldPosA(ServerLevel level, float partialTick) {
            try {
                Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                        .getDimensionToGroundBodyIdImmutable()
                        .get(VSGameUtilsKt.getDimensionId(level));

                if (shipA.equals(groundBodyId)) {
                    return new Vector3d(localPosA);
                } else {
                    Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipA);
                    if (shipObject != null) {
                        Vector3d worldPos = new Vector3d();
                        shipObject.getTransform().getShipToWorld().transformPosition(localPosA, worldPos);
                        return worldPos;
                    }
                }
                return new Vector3d(localPosA);
            } catch (Exception e) {
                return new Vector3d(localPosA);
            }
        }

        public Vector3d getWorldPosB(ServerLevel level, float partialTick) {
            try {
                Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                        .getDimensionToGroundBodyIdImmutable()
                        .get(VSGameUtilsKt.getDimensionId(level));

                if (shipB.equals(groundBodyId)) {
                    return new Vector3d(localPosB);
                } else {
                    Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipB);
                    if (shipObject != null) {
                        Vector3d worldPos = new Vector3d();
                        shipObject.getTransform().getShipToWorld().transformPosition(localPosB, worldPos);
                        return worldPos;
                    }
                }
                return new Vector3d(localPosB);
            } catch (Exception e) {
                return new Vector3d(localPosB);
            }
        }
    }

    public static void addConstraintWithPersistence(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                                    Vector3d localPosA, Vector3d localPosB, double maxLength,
                                                    double compliance, double maxForce) {
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = "constraint_" + constraintId;
        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added constraint " + constraintId + " to tracker, persistence, and synced to clients");
    }


    public static void removeConstraint(Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            NetworkHandler.sendConstraintRemove(constraintId);
            System.out.println("Removed constraint " + constraintId + " from tracker and synced to clients");
        }
    }

    public static void removeConstraintWithPersistence(ServerLevel level, Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            ConstraintPersistence persistence = ConstraintPersistence.get(level);
            String persistenceId = "constraint_" + constraintId;
            persistence.removeConstraint(persistenceId);

            NetworkHandler.sendConstraintRemove(constraintId);
            System.out.println("Removed constraint " + constraintId + " from tracker, persistence, and synced to clients");
        }
    }

    public static Map<Integer, RopeConstraintData> getActiveConstraints() {
        return new HashMap<>(activeConstraints);
    }

    public static void clearAllConstraints() {
        activeConstraints.clear();
        NetworkHandler.sendConstraintClearAll();
        System.out.println("Cleared all constraints and synced to clients");
    }
    public static void addConstraintToTracker(Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce) {
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added constraint " + constraintId + " to tracker (restoration) and synced to clients");
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
                Integer constraintId = entry.getKey();
                RopeConstraintData data = entry.getValue();

                NetworkHandler.sendConstraintAddToPlayer(player, constraintId, data.shipA, data.shipB,
                        data.localPosA, data.localPosB, data.maxLength);
            }
            System.out.println("Synced " + activeConstraints.size() + " constraints to player " + player.getName().getString());
        }
    }

    // clean up constraints that aren't on valid blocks anymore, i think this might get rid of that dumb bug idk
    public static void validateAndCleanupConstraints(ServerLevel level) {
        //System.out.println("Validating " + activeConstraints.size() + " active constraints...");

        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();
        Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                .getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            try {
                // Check if ships still exist
                boolean shipAExists = data.shipA.equals(groundBodyId) ||
                        VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipA) != null;
                boolean shipBExists = data.shipB.equals(groundBodyId) ||
                        VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipB) != null;

                if (!shipAExists || !shipBExists) {
                    System.out.println("Constraint " + constraintId + " references missing ships - marking for removal");
                    constraintsToRemove.add(constraintId);
                    continue;
                }

                // Get world positions to check if blocks still exist
                Vector3d worldPosA = data.getWorldPosA(level, 1.0f);
                Vector3d worldPosB = data.getWorldPosB(level, 1.0f);

                // Convert to block positions
                net.minecraft.core.BlockPos blockPosA = new net.minecraft.core.BlockPos(
                        (int) Math.floor(worldPosA.x),
                        (int) Math.floor(worldPosA.y),
                        (int) Math.floor(worldPosA.z)
                );
                net.minecraft.core.BlockPos blockPosB = new net.minecraft.core.BlockPos(
                        (int) Math.floor(worldPosB.x),
                        (int) Math.floor(worldPosB.y),
                        (int) Math.floor(worldPosB.z)
                );

                // Check if chunks are loaded and blocks exist
                boolean validA = isValidAttachmentPoint(level, blockPosA, data.shipA, groundBodyId);
                boolean validB = isValidAttachmentPoint(level, blockPosB, data.shipB, groundBodyId);

                if (!validA || !validB) {
                    System.out.println("Constraint " + constraintId + " has invalid attachment points (A:" + validA + ", B:" + validB + ") - marking for removal");
                    constraintsToRemove.add(constraintId);
                }

            } catch (Exception e) {
                System.err.println("Error validating constraint " + constraintId + ": " + e.getMessage());
                constraintsToRemove.add(constraintId);
            }
        }

        // Remove invalid constraints
        for (Integer constraintId : constraintsToRemove) {
            try {
                // Remove from VS physics
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                // Remove from our tracking
                removeConstraintWithPersistence(level, constraintId);
                //System.out.println("Cleaned up invalid constraint: " + constraintId);
            } catch (Exception e) {
                System.err.println("Error removing invalid constraint " + constraintId + ": " + e.getMessage());
            }
        }

        //System.out.println("Constraint validation complete. Removed " + constraintsToRemove.size() + " invalid constraints.");
    }

    private static boolean isValidAttachmentPoint(ServerLevel level, net.minecraft.core.BlockPos pos, Long shipId, Long groundBodyId) {
        try {
            // For ground/world attachments, check if chunk is loaded and block exists
            if (shipId.equals(groundBodyId)) {
                if (!level.isLoaded(pos)) {
                    return false; // Chunk not loaded, assume invalid for now
                }
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                return !state.isAir(); // Valid if not air
            } else {
                // For ship attachments, check if ship exists and chunk is loaded
                org.valkyrienskies.core.api.ships.Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) {
                    return false; // Ship doesn't exist
                }

                // Convert world pos back to ship local coordinates to check the block
                Vector3d worldPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                Vector3d localPos = new Vector3d();
                ship.getTransform().getWorldToShip().transformPosition(worldPos, localPos);

                net.minecraft.core.BlockPos shipBlockPos = new net.minecraft.core.BlockPos(
                        (int) Math.floor(localPos.x),
                        (int) Math.floor(localPos.y),
                        (int) Math.floor(localPos.z)
                );

                if (!level.isLoaded(shipBlockPos)) {
                    return false; // Ship chunk not loaded
                }

                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(shipBlockPos);
                return !state.isAir(); // Valid if not air
            }
        } catch (Exception e) {
            System.err.println("Error checking attachment point validity: " + e.getMessage());
            return false;
        }
    }

}
