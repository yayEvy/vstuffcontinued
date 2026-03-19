package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RopeManager {

    private static final Map<Integer, ReworkedRope> activeRopes = new ConcurrentHashMap<>();
    private static int nextId = 1;


    public static int getNextId() {
        return ++nextId;
    }

    public static void resetId() {
        nextId = 1;
    }

    public static void addRopeWithPersistence(ServerLevel level, ReworkedRope rope) {
        activeRopes.put(rope.ropeId, rope);

        RopePersistence persistence = RopePersistence.get(level);

        persistence.addRope(rope);
        NetworkHandler.sendConstraintAdd(rope.ropeId, rope.posData0.shipId(), rope.posData1.shipId(), rope.posData0.localPos(), rope.posData1.localPos(), rope.jointValues.maxLength(), rope.style);
    }

    public static ReworkedRope getRope(Integer id) {
        return activeRopes.get(id);
    }

    public static boolean hasRope(Integer id) {
        return activeRopes.containsKey(id);
    }

    public static void replaceRope(Integer id, ReworkedRope rope) {
        activeRopes.put(id, rope);
    }

    public static void removeRopeWithPersistence(ServerLevel level, Integer constraintId) {

        ReworkedRope data = activeRopes.remove(constraintId);
        if (data != null) {

            RopePersistence persistence = RopePersistence.get(level);
            persistence.removeRope(constraintId);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
            }

            NetworkHandler.sendConstraintRemove(constraintId);

        }
    }

    public static void syncAllRopesToPlayer(ServerPlayer player) {
        NetworkHandler.sendClearAllConstraintsToPlayer(player);
        VStuff.LOGGER.info("Syncing all ropes to player {}", player.getName().getString());

        for (Map.Entry<Integer, ReworkedRope> entry : activeRopes.entrySet()) {
            ReworkedRope data = entry.getValue();
            NetworkHandler.sendConstraintAddToPlayer(
                    player,
                    entry.getKey(),
                    data.posData0.shipId(),
                    data.posData1.shipId(),
                    data.posData0.localPos(),
                    data.posData1.localPos(),
                    data.jointValues.maxLength(),
                    data.style
            );
        }
    }

    public static Map<Integer, ReworkedRope> getActiveRopes() {
        return new HashMap<>(activeRopes);
    }

    public static void addRopeToManager(ReworkedRope rope) {
        if (rope == null || rope.ropeId == null) return;

        activeRopes.put(rope.ropeId, rope);
    }


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        NetworkHandler.sendClearAllConstraintsToPlayer(player);

        syncAllRopesToPlayer(player);
    }
}