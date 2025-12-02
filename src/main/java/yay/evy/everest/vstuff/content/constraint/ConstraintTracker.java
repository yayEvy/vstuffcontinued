package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.NetworkHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstraintTracker {

    public static final Map<Integer, Rope> activeRopes = new ConcurrentHashMap<>();
    private static long lastJoinTime = 0L;


    public static void addConstraintWithPersistence(Rope rope) {

        if (rope.constraintType == RopeUtil.ConstraintType.PULLEY && rope.sourceBlockPos != null) {
            boolean existingConstraintFound = activeRopes.values().stream()
                    .anyMatch(existing -> existing.constraintType == RopeUtil.ConstraintType.PULLEY
                            && existing.sourceBlockPos != null
                            && existing.sourceBlockPos.equals(rope.sourceBlockPos)
                            && existing.style == rope.style);

            if (existingConstraintFound) return;
        }


        activeRopes.put(rope.ID, rope);

        ConstraintPersistence persistence = ConstraintPersistence.get(rope.getLevel());

        persistence.addConstraint(rope);
        NetworkHandler.sendConstraintAdd(rope.ID, rope.shipA, rope.shipB, rope.localPosA, rope.localPosB, rope.maxLength, rope.style);
    }

    public static void replaceConstraint(Integer id, Rope rope) {
        activeRopes.put(id, rope);
    }

    public static void removeConstraintWithPersistence(ServerLevel level, Integer constraintId) {

        Rope data = activeRopes.remove(constraintId);
        if (data != null) {
            data.removeJoint(level);

            ConstraintPersistence persistence = ConstraintPersistence.get(level);
                persistence.markConstraintAsRemoved(constraintId);
                persistence.setDirty();

            if (level.getServer() != null) {
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
                    level.getServer().tell(new net.minecraft.server.TickTask(0, () -> {
                        NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
                    }));
                }
            }

            NetworkHandler.sendConstraintRemove(constraintId);

            if (data.constraintType == RopeUtil.ConstraintType.GENERIC && data.sourceBlockPos != null) {
                cleanupOrphanedConstraints(level, data.sourceBlockPos);
            }
        }
    }



    public static void syncAllConstraintsToPlayer(ServerPlayer player) {
        NetworkHandler.sendClearAllConstraintsToPlayer(player);
        VStuff.LOGGER.info("Attempting to sync all constraints to player {}", player.getName());

        for (Map.Entry<Integer, Rope> entry : activeRopes.entrySet()) {
            Rope data = entry.getValue();
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


    public static Map<Integer, Rope> getActiveRopes() {
        return new HashMap<>(activeRopes);
    }


    public static void addConstraintToTracker(Rope rope) {
        if (activeRopes.containsKey(rope.ID)) return;

        activeRopes.put(rope.ID, rope);
        VStuff.LOGGER.info("Adding constraint {} to activeRopes", rope.ID);


        NetworkHandler.sendConstraintAdd(rope.ID, rope.shipA, rope.shipB, rope.localPosA, rope.localPosB, rope.maxLength, rope.style);
    }



    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (Map.Entry<Integer, Rope> entry : activeRopes.entrySet()) {
                Integer constraintId = entry.getKey();
                Rope rope = entry.getValue();

                NetworkHandler.sendConstraintAddToPlayer(player, constraintId, rope.shipA, rope.shipB,
                        rope.localPosA, rope.localPosB, rope.maxLength, rope.style);
            }
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
            syncAllConstraintsToPlayer(player);

            lastJoinTime = System.currentTimeMillis();
        }
    }



    public static void cleanupOrphanedConstraints(ServerLevel level, BlockPos sourceBlockPos) {
        List<Integer> constraintsToRemove = getIDsToRemove(sourceBlockPos);

        var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(level));

        for (Integer constraintId : constraintsToRemove) {
            try {
                gtpa.removeJoint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
            } catch (Exception ignored) {}
        }
    }

    private static @NotNull List<Integer> getIDsToRemove(BlockPos sourceBlockPos) {
        List<Integer> constraintsToRemove = new java.util.ArrayList<>();

        for (Map.Entry<Integer, Rope> entry : activeRopes.entrySet()) {
            Integer constraintId = entry.getKey();
            Rope rope = entry.getValue();

            if (rope.constraintType == RopeUtil.ConstraintType.PULLEY &&
                    rope.sourceBlockPos != null &&
                    rope.sourceBlockPos.equals(sourceBlockPos)) {
                constraintsToRemove.add(constraintId);
            }
        }
        return constraintsToRemove;
    }


    private static boolean areAttachmentChunksLoaded(ServerLevel level, Rope rope, Long groundBodyId) {
        try {
            Vector3d worldPosA = rope.getWorldPosA(level);
            BlockPos blockPosA = new BlockPos(
                    (int) Math.floor(worldPosA.x),
                    (int) Math.floor(worldPosA.y),
                    (int) Math.floor(worldPosA.z)
            );

            Vector3d worldPosB = rope.getWorldPosB(level);
            BlockPos blockPosB = new BlockPos(
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


    public static boolean constraintExists(ServerLevel level, Integer constraintId) {
        if (constraintId == null) return false;

        try {
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

            return getActiveRopes().containsKey(constraintId);
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
                BlockPos blockPos = BlockPos.containing(localPos.x, localPos.y, localPos.z);

                if (!level.isLoaded(blockPos)) return false;

                BlockState state = level.getBlockState(blockPos);
                return !state.isAir();
            } else {
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
        List<Integer> toRemove = new java.util.ArrayList<>();
        Long groundId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));

        for (Map.Entry<Integer, FluidConverterLink> entry : fluidConstraints.entrySet()) {
            FluidConverterLink data = entry.getValue();

            if (!data.level().equals(level.dimension())) continue;

            Vector3d worldPosA = data.shipA().equals(groundId)
                    ? data.localA()
                    : VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipA())
                    .getTransform().getShipToWorld().transformPosition(data.localA(), new Vector3d());

            Vector3d worldPosB = data.shipB().equals(groundId)
                    ? data.localB()
                    : VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipB())
                    .getTransform().getShipToWorld().transformPosition(data.localB(), new Vector3d());


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