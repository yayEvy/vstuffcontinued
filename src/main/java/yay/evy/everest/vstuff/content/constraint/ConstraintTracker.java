package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.TickTask;
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

            ConstraintPersistence persistence = ConstraintPersistence.get(level);
                persistence.markConstraintAsRemoved(constraintId);
                persistence.setDirty();

            if (level.getServer() != null) {
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
                    level.getServer().tell(
                            new TickTask(
                                0,
                                () -> NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId)
                            )
                    );
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
}