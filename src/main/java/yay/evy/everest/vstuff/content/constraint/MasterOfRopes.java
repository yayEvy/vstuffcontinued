package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.NetworkManager;
import yay.evy.everest.vstuff.content.constraint.ropes.AbstractRope;
import yay.evy.everest.vstuff.content.constraint.ropes.PulleyRope;
import yay.evy.everest.vstuff.content.constraint.ropes.Rope;
import yay.evy.everest.vstuff.content.constraint.ropes.JointlessRope;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MasterOfRopes {

    private static long lastJoinTime = 0L;

    public static final Map<Integer, Rope> activeRopes = new ConcurrentHashMap<>();
    public static final Map<Integer, PulleyRope> activePulleyRopes = new ConcurrentHashMap<>();
    public static final Map<Integer, JointlessRope> activeWorldRopes = new ConcurrentHashMap<>();


    public static <T extends AbstractRope> void ADD(ServerLevel level, T rope) {
        rope.createJoint(level);
        PUT(rope.ID, rope);

        RopePersistenceYesItWorksNowIPromise3dsmile persistence = RopePersistenceYesItWorksNowIPromise3dsmile.get(level);

        persistence.addRope(rope);
        NetworkManager.sendConstraintAdd(rope);
    }

    public static <T extends AbstractRope> void PUT(Integer id, T rope) {
        switch (rope.getClass().getSimpleName()) {
            case "Rope" -> activeRopes.put(id, (Rope) rope);
            case "PulleyRope" -> activePulleyRopes.put(id, (PulleyRope) rope);
            case "WorldToWorldRope" -> activeWorldRopes.put(id, (JointlessRope) rope);
        }
    }

    public static AbstractRope GET(Integer id) {
        if (activeRopes.containsKey(id)) {
            return activeRopes.get(id);

        } else if (activePulleyRopes.containsKey(id)) {
            return activePulleyRopes.get(id);

        } else if (activeWorldRopes.containsKey(id)) {
            return activeWorldRopes.get(id);

        } else {
            VStuff.LOGGER.warn("Could not get rope with id {} because it was not found", id);
            return null;
        }
    }

    private static AbstractRope remove(Integer ropeId) {
        if (activeRopes.containsKey(ropeId)) {
            return activeRopes.remove(ropeId);

        } else if (activePulleyRopes.containsKey(ropeId)) {
            return activePulleyRopes.remove(ropeId);

        } else if (activeWorldRopes.containsKey(ropeId)) {
            return activeWorldRopes.remove(ropeId);

        } else {
            VStuff.LOGGER.warn("Could not remove rope with id {} because it was not found", ropeId);
            return null;
        }
    }

    public static void REMOVE(ServerLevel level, Integer ropeId) {

        AbstractRope rope = remove(ropeId);
        if (rope != null) {
            rope.removeJoint(level);

            RopePersistenceYesItWorksNowIPromise3dsmile persistence = RopePersistenceYesItWorksNowIPromise3dsmile.get(level);
            persistence.removeRope(ropeId);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                NetworkManager.sendConstraintRemoveToPlayer(player, rope);
                level.getServer().tell(
                        new TickTask(
                                0,
                                () -> NetworkManager.sendConstraintRemoveToPlayer(player, rope)
                        )
                );
            }

            NetworkManager.sendConstraintRemove(rope);
        }
    }

    public static void syncAllConstraintsToPlayer(ServerPlayer player) {
        NetworkManager.sendClearAllConstraintsToPlayer(player);
        VStuff.LOGGER.info("Attempting to sync all constraints to player {} ({})", player.getName().getString(), player.getUUID());

        for (Map.Entry<Integer, Rope> entry : activeRopes.entrySet()) {
            AbstractRope rope = entry.getValue();
            NetworkManager.sendConstraintAddToPlayer(player, rope);
        }

        for (Map.Entry<Integer, PulleyRope> entry : activePulleyRopes.entrySet()) {
            AbstractRope rope = entry.getValue();
            NetworkManager.sendConstraintAddToPlayer(player, rope);
        }
        for (Map.Entry<Integer, JointlessRope> entry : activeWorldRopes.entrySet()) {
            AbstractRope rope = entry.getValue();
            NetworkManager.sendConstraintAddToPlayer(player, rope);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (Map.Entry<Integer, Rope> entry : activeRopes.entrySet()) {
                Rope rope = entry.getValue();

                NetworkManager.sendConstraintAddToPlayer(player, rope);
            }

            NetworkManager.sendClearAllConstraintsToPlayer(player);
            syncAllConstraintsToPlayer(player);

            lastJoinTime = System.currentTimeMillis();
        }
    }

    /**
     * probably shouldn't be used unless absolutely needed
     * @return a HashMap containing ALL ropes.
     */
    public static Map<Integer, AbstractRope> getAllActiveRopes() {
        HashMap<Integer, AbstractRope> ropes = new HashMap<>();
        ropes.putAll(activePulleyRopes);
        ropes.putAll(activeRopes);
        ropes.putAll(activeWorldRopes);
        return ropes;
    }

    public static Map<Integer, Rope> getActiveRopes() {
        return new HashMap<>(activeRopes);
    }
    public static Map<Integer, PulleyRope> getActivePulleyRopes() {
        return new HashMap<>(activePulleyRopes);
    }
    public static Map<Integer, JointlessRope> getActiveWorldRopes() {
        return new HashMap<>(activeWorldRopes);
    }
}
