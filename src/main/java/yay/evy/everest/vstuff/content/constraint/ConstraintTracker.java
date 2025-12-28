package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.api.ValkyrienSkies;
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
    private static int lastUsedId = 0;


    public static int getNextId() {
        return ++lastUsedId;
    }

    public static void setLastUsedId(int id) {
        if (id > lastUsedId) {
            lastUsedId = id;
           // VStuff.LOGGER.info("ConstraintTracker ID counter updated to {}", lastUsedId);
        }
    }
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
        //VStuff.LOGGER.info("Adding constraint with id {} to persistence", rope.ID);
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
      //  VStuff.LOGGER.info("Attempting to sync all constraints to player {}", player.getName());

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
     //   VStuff.LOGGER.info("Adding constraint {} to activeRopes", rope.ID);


        NetworkHandler.sendConstraintAdd(rope.ID, rope.shipA, rope.shipB, rope.localPosA, rope.localPosB, rope.maxLength, rope.style);
    }



    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        NetworkHandler.sendClearAllConstraintsToPlayer(player);

        syncAllConstraintsToPlayer(player);
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


}