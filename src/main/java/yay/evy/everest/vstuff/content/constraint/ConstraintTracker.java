package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstraintTracker {

    public static final Map<Integer, RopeConstraintData> activeConstraints = new ConcurrentHashMap<>();
    private static final Map<Integer, String> constraintToPersistenceId = new ConcurrentHashMap<>();
    private static long lastJoinTime = 0L;


    public static class RopeConstraintData {
        public final Long shipA;
        public final Long shipB;
        public final Vector3d localPosA;
        public final Vector3d localPosB;
        public final double maxLength;
        public final double compliance;
        public final double maxForce;
        public final ConstraintType constraintType;
        public final net.minecraft.core.BlockPos sourceBlockPos;
        public final BlockPos anchorBlockPosA;
        public final BlockPos anchorBlockPosB;
        public final boolean isShipA;
        public final boolean isShipB;
        public RopeStyles.RopeStyle style;

        public enum ConstraintType {
            ROPE_PULLEY,
            GENERIC
        }

        // This is the primary constructor. Keep this one.
        public RopeConstraintData(ServerLevel level, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce,
                                  ConstraintType constraintType, net.minecraft.core.BlockPos sourceBlockPos, RopeStyles.RopeStyle style) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
            this.constraintType = constraintType;
            this.sourceBlockPos = sourceBlockPos;
            this.anchorBlockPosA = null;
            this.anchorBlockPosB = null;
            this.style = style;

            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable().get(VSGameUtilsKt.getDimensionId(level));
            this.isShipA = !shipA.equals(groundBodyId);
            this.isShipB = !shipB.equals(groundBodyId);
        }

        // This is the old constructor, which now calls the main constructor
        public RopeConstraintData(ServerLevel level, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce, RopeStyles.RopeStyle style) {
            this(level, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, ConstraintType.GENERIC, null, style);
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
                                                    double compliance, double maxForce,
                                                    RopeConstraintData.ConstraintType constraintType,
                                                    net.minecraft.core.BlockPos sourceBlockPos, RopeStyles.RopeStyle style) {

        if (constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY && sourceBlockPos != null) {
            boolean existingConstraintFound = activeConstraints.values().stream()
                    .anyMatch(existing -> existing.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY
                            && existing.sourceBlockPos != null
                            && existing.sourceBlockPos.equals(sourceBlockPos)
                            && existing.style == style); // check style too

            if (existingConstraintFound) {
               // System.out.println("Constraint already exists for rope pulley at " + sourceBlockPos + " with style " + style + ", skipping");
                return;
            }
        }

        RopeConstraintData data = new RopeConstraintData(level, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, constraintType, sourceBlockPos, style);
        activeConstraints.put(constraintId, data);
       // System.out.println("Added " + constraintType + " constraint " + constraintId + " with style " + style);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = java.util.UUID.randomUUID().toString();
        constraintToPersistenceId.put(constraintId, persistenceId);

        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level, constraintType, sourceBlockPos, style);
        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength, style);
    }



    public static void addConstraintWithPersistence(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                                    Vector3d localPosA, Vector3d localPosB, double maxLength,
                                                    double compliance, double maxForce, RopeStyles.RopeStyle style) {
        addConstraintWithPersistence(level, constraintId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                RopeConstraintData.ConstraintType.GENERIC, null, style);
    }
    public static String persistanceIdViaConstraintId (Integer constraintId){

        return constraintToPersistenceId.get(constraintId);
    }



    public static void removeConstraintWithPersistence(ServerLevel level, Integer constraintId) {
        RopeConstraintData data = activeConstraints.remove(constraintId);
        if (data != null) {
            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);

            ConstraintPersistence persistence = ConstraintPersistence.get(level);
            String persistenceId = constraintToPersistenceId.remove(constraintId);
            if (persistenceId != null) {
                persistence.markConstraintAsRemoved(persistenceId);
                persistence.setDirty();
            }

            if (level.getServer() != null) {
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
                    level.getServer().tell(new net.minecraft.server.TickTask(0, () -> {
                        NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
                    }));
                }
            }

            NetworkHandler.sendConstraintRemove(constraintId);

            if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY && data.sourceBlockPos != null) {
                cleanupOrphanedConstraints(level, data.sourceBlockPos);
            }
        }
    }


    public static void syncAllConstraintsToPlayer(ServerPlayer player) {
        NetworkHandler.sendClearAllConstraintsToPlayer(player);

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            RopeConstraintData data = entry.getValue();
            NetworkHandler.sendConstraintAddToPlayer(
                    player,
                    entry.getKey(),
                    data.shipA,
                    data.shipB,
                    data.localPosA,
                    data.localPosB,
                    data.maxLength,
                    data.style
            );
        }
    }



    public static void mapConstraintToPersistenceId(Integer constraintId, String persistenceId) {
        constraintToPersistenceId.put(constraintId, persistenceId);
        //   System.out.println("Mapped constraint " + constraintId + " to persistence ID " + persistenceId);
    }

    public static Map<Integer, RopeConstraintData> getActiveConstraints() {
        return new HashMap<>(activeConstraints);
    }


    public static void addConstraintToTracker(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce,
                                              RopeConstraintData.ConstraintType constraintType,
                                              net.minecraft.core.BlockPos sourceBlockPos, RopeStyles.RopeStyle style) {
        if (activeConstraints.containsKey(constraintId)) {
          //  System.out.println("Constraint " + constraintId + " already exists in tracker, skipping");
            return;
        }

        RopeConstraintData data = new RopeConstraintData(level, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, constraintType, sourceBlockPos, style);
        activeConstraints.put(constraintId, data);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength, style);
        //System.out.println("Added " + constraintType + " constraint " + constraintId + " to tracker (restoration) with source block " + sourceBlockPos);
    }




    private static boolean isShipValid(ServerLevel level, Long shipId, Long groundBodyId) {
        if (shipId == null) return false;

        if (shipId.equals(groundBodyId)) {
            return true;
        }

        try {
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
            var ship = shipWorld.getAllShips().getById(shipId);
            boolean exists = ship != null;

            if (!exists) {
                //  System.out.println("Ship " + shipId + " not found in ship world");
                // Try alternative lookup methods
                var allShips = shipWorld.getAllShips();
                //System.out.println("Available ships: " + allShips.stream().map(s -> s.getId()).toList());
            }

            return exists;
        } catch (Exception e) {
            System.err.println("Exception checking ship validity for " + shipId + ": " + e.getMessage());
            return false;
        }
    }


    private static final Map<Integer, Long> delayedValidations = new ConcurrentHashMap<>();

    private static void scheduleDelayedValidation(ServerLevel level, Integer constraintId, long delayMs) {
        delayedValidations.put(constraintId, System.currentTimeMillis() + delayMs);
        //  System.out.println("Scheduled delayed validation for constraint " + constraintId + " in " + (delayMs/1000) + " seconds");
    }



    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
                Integer constraintId = entry.getKey();
                RopeConstraintData data = entry.getValue();

                NetworkHandler.sendConstraintAddToPlayer(player, constraintId, data.shipA, data.shipB,
                        data.localPosA, data.localPosB, data.maxLength, data.style);
            }
            // Sync all constraints to player
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
            syncAllConstraintsToPlayer(player);

            // Update the last join time to delay the cleanup process
            lastJoinTime = System.currentTimeMillis();
            //System.out.println("Player joined, setting lastJoinTime for delayed cleanup.");
        }
    }


    public static void cleanupOrphanedConstraints(ServerLevel level, net.minecraft.core.BlockPos sourceBlockPos) {
        // System.out.println("Cleaning up orphaned constraints for block at " + sourceBlockPos);

        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY &&
                    data.sourceBlockPos != null &&
                    data.sourceBlockPos.equals(sourceBlockPos)) {

                //   System.out.println("Found orphaned constraint " + constraintId + " for block " + sourceBlockPos);
                constraintsToRemove.add(constraintId);
            }
        }

        for (Integer constraintId : constraintsToRemove) {
            try {
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
                //   System.out.println("Cleaned up orphaned constraint " + constraintId);
            } catch (Exception e) {
                //   System.err.println("Error cleaning up orphaned constraint " + constraintId + ": " + e.getMessage());
            }
        }
    }


    private static boolean areAttachmentChunksLoaded(ServerLevel level, RopeConstraintData data, Long groundBodyId) {
        try {
            Vector3d worldPosA = data.getWorldPosA(level, 0.0f);
            net.minecraft.core.BlockPos blockPosA = new net.minecraft.core.BlockPos(
                    (int) Math.floor(worldPosA.x),
                    (int) Math.floor(worldPosA.y),
                    (int) Math.floor(worldPosA.z)
            );

            Vector3d worldPosB = data.getWorldPosB(level, 0.0f);
            net.minecraft.core.BlockPos blockPosB = new net.minecraft.core.BlockPos(
                    (int) Math.floor(worldPosB.x),
                    (int) Math.floor(worldPosB.y),
                    (int) Math.floor(worldPosB.z)
            );

            boolean chunkALoaded = level.isLoaded(blockPosA);
            boolean chunkBLoaded = level.isLoaded(blockPosB);

            return chunkALoaded && chunkBLoaded;
        } catch (Exception e) {
            System.err.println("Error checking chunk loading status: " + e.getMessage());
            return false;
        }
    }

    public static void validateAndCleanupConstraints(ServerLevel level) {
        // Skip validation for 15 seconds after a player joins
        if (System.currentTimeMillis() - lastJoinTime < 15000) return;

        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();

        Long groundBodyId;
        try {
            groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
        } catch (Exception e) {
            return; // Cannot validate without ground body
        }
        if (groundBodyId == null) return;

        long currentTime = System.currentTimeMillis();

        java.util.List<Integer> delayedToProcess = new java.util.ArrayList<>();
        for (Map.Entry<Integer, Long> entry : delayedValidations.entrySet()) {
            if (currentTime >= entry.getValue()) delayedToProcess.add(entry.getKey());
        }

        for (Integer constraintId : delayedToProcess) {
            delayedValidations.remove(constraintId);
            RopeConstraintData data = activeConstraints.get(constraintId);
            if (data == null) continue;

            boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
            boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);

            if (!shipAExists || !shipBExists) {
                constraintsToRemove.add(constraintId);
            }
        }

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            // Skip if already scheduled for delayed validation
            if (delayedValidations.containsKey(constraintId)) continue;

            boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
            boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);

            if (!shipAExists || !shipBExists) {
                // Ship missing → schedule delayed validation instead of removing
                scheduleDelayedValidation(level, constraintId, 5000);
                continue;
            }

            // Skip validation if chunks are not loaded
            if (!areAttachmentChunksLoaded(level, data, groundBodyId)) continue;

            boolean validA = isValidAttachmentPoint(level, data.localPosA, data.shipA, groundBodyId, data.isShipA);
            boolean validB = isValidAttachmentPoint(level, data.localPosB, data.shipB, groundBodyId, data.isShipB);

            if (!validA || !validB) {
                // Invalid attachment → schedule delayed validation
                scheduleDelayedValidation(level, constraintId, 5000);
            }
        }

        for (Integer constraintId : constraintsToRemove) {
            try {
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
            } catch (Exception ignored) {}
        }
    }
    public static boolean constraintExists(ServerLevel level, Integer constraintId) {
        if (constraintId == null) return false;

        try {
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

            return getActiveConstraints().containsKey(constraintId);
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isValidAttachmentPoint(
            ServerLevel level,
            Vector3d localPos,
            Long shipId,
            Long groundBodyId,
            boolean isShip
    ) {
        try {
            if (!isShip) {
                // Ground block
                BlockPos blockPos = BlockPos.containing(localPos.x, localPos.y, localPos.z);

                if (!level.isLoaded(blockPos)) return false;

                BlockState state = level.getBlockState(blockPos);
                return !state.isAir();
            } else {
                // Ship block
                Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) return false;

                Vector3d worldPos = new Vector3d();
                ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);

                BlockPos worldBlockPos = BlockPos.containing(worldPos.x, worldPos.y, worldPos.z);

                if (!level.isLoaded(worldBlockPos)) return false;

                BlockState state = level.getBlockState(worldBlockPos);
                return !state.isAir();
            }
        } catch (Exception e) {
            // If anything fails, treat it as invalid so we don't keep ghost ropes
            return false;
        }
    }

    public record FluidConverterLink(Long shipA, Vector3d localA, Long shipB, Vector3d localB, ResourceKey<Level> level) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FluidConverterLink other)) return false;
            return java.util.Objects.equals(shipA, other.shipA)
                    && localA.equals(other.localA)
                    && java.util.Objects.equals(shipB, other.shipB)
                    && localB.equals(other.localB)
                    && level.equals(other.level);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(shipA, localA, shipB, localB, level);
        }

        @Override
        public String toString() {
            return "FluidConverterLink{" +
                    "shipA=" + shipA +
                    ", localA=" + localA +
                    ", shipB=" + shipB +
                    ", localB=" + localB +
                    ", level=" + level +
                    '}';
        }
        public Vector3d worldA(Level level) {
            Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipA);
            if (ship == null) return localA;
            return ship.getTransform().getShipToWorld().transformPosition(localA, new Vector3d());
        }

        public Vector3d worldB(Level level) {
            Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipB);
            if (ship == null) return localB;
            return ship.getTransform().getShipToWorld().transformPosition(localB, new Vector3d());
        }
    }




    public static List<FluidConverterLink> getFluidLinks(Level level) {
        var dim = level.dimension();
        return fluidConstraints.values().stream()
                .filter(link -> link.level().equals(dim))
                .toList();
    }


    public static void addFluidConstraint(int id,
                                          Long shipIdA, Vector3d localPosA,
                                          Long shipIdB, Vector3d localPosB,
                                          ResourceKey<Level> level) {

        fluidConstraints.put(id, new FluidConverterLink(shipIdA, localPosA, shipIdB, localPosB, level));
    }




    public static class FluidConstraintData {
        public final BlockPos posA;
        public final BlockPos posB;
        public final ResourceKey<Level> dimension;

        public FluidConstraintData(BlockPos posA, BlockPos posB, ResourceKey<Level> dimension) {
            this.posA = posA;
            this.posB = posB;
            this.dimension = dimension;
        }
    }
    public static void removeFluidConstraint(int constraintId) {
        fluidConstraints.remove(constraintId);
        System.out.println("Removed fluid constraint " + constraintId);
    }

    public static void validateFluidConstraints(ServerLevel level) {
        java.util.List<Integer> toRemove = new java.util.ArrayList<>();
        Long groundId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));

        for (Map.Entry<Integer, FluidConverterLink> entry : fluidConstraints.entrySet()) {
            FluidConverterLink data = entry.getValue();

            // Skip constraints from other dimensions
            if (!data.level().equals(level.dimension())) continue;

            Vector3d worldPosA = data.shipA().equals(groundId)
                    ? data.localA()
                    : VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipA())
                    .getTransform().getShipToWorld().transformPosition(data.localA(), new Vector3d());

            Vector3d worldPosB = data.shipB().equals(groundId)
                    ? data.localB()
                    : VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipB())
                    .getTransform().getShipToWorld().transformPosition(data.localB(), new Vector3d());


            // Convert to BlockPos for block checks
            BlockPos posA = new BlockPos(
                    (int) Math.floor(worldPosA.x),
                    (int) Math.floor(worldPosA.y),
                    (int) Math.floor(worldPosA.z)
            );

            BlockPos posB = new BlockPos(
                    (int) Math.floor(worldPosB.x),
                    (int) Math.floor(worldPosB.y),
                    (int) Math.floor(worldPosB.z)
            );

            boolean existsA = level.isLoaded(posA) && !level.getBlockState(posA).isAir();
            boolean existsB = level.isLoaded(posB) && !level.getBlockState(posB).isAir();

            if (!existsA || !existsB) {
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(ConstraintTracker::removeFluidConstraint);
    }

    private static final Map<Integer, FluidConverterLink> fluidConstraints = new ConcurrentHashMap<>();



    public static Map<Integer, FluidConverterLink> getFluidConstraints() {
        return new HashMap<>(fluidConstraints);
    }


}