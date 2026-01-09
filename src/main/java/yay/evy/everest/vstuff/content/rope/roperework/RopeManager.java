package yay.evy.everest.vstuff.content.rope.roperework;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.foundation.network.NetworkManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RopeManager {

    public static Map<Integer, NewRope> activeRopes = new ConcurrentHashMap<>();
    public static Integer ropeId;

    public static Integer getNextId() {
        return ++ropeId;
    }

    public static void resetId() {
        ropeId = 0;
    }

    public static void addRope(ServerLevel level, NewRope rope) {

        activeRopes.put(rope.ropeId, rope);

        RopePersistence ropePersistence = RopePersistence.getOrCreate(level);

        ropePersistence.addRope(rope);
        NetworkManager.sendRopeAdd(rope.ropeId, rope.posData0, rope.posData1, rope.jointValues.maxLength(), rope.style.getStyle());
    }

    public static void replaceRope(Integer id, NewRope rope) {
        activeRopes.put(id, rope);
    }

    public static void replaceId(Integer oldId, Integer newId) {
        NewRope rope = activeRopes.remove(oldId);
        if (rope != null) {
            rope.ropeId = newId;
            activeRopes.put(newId, rope);
        }
    }

    public static void removeRope(ServerLevel level, Integer constraintId) {

        NewRope data = activeRopes.remove(constraintId);
        if (data != null) {

            RopePersistence persistence = RopePersistence.getOrCreate(level);
            persistence.removeRope(data.ropeId);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                NetworkManager.sendRopeRemoveToPlayer(player, constraintId);
            }

            NetworkManager.sendRopeRemove(constraintId);
        }
    }



    public static void syncAllRopesToPlayer(ServerPlayer player) {
        NetworkManager.sendClearAllRopesToPlayer(player);

        for (Map.Entry<Integer, NewRope> entry : activeRopes.entrySet()) {
            NewRope data = entry.getValue();
            NetworkManager.sendRopeAddToPlayer(
                    player,
                    entry.getKey(),
                    data.posData0,
                    data.posData1,
                    data.jointValues.maxLength(),
                    data.style.getStyle()
            );
        }
    }


    public static Map<Integer, NewRope> getActiveRopes() {
        return new HashMap<>(activeRopes);
    }


    public static void addRopeToManager(NewRope rope) {
        if (rope == null) return;

        Integer id = getNextId();
        activeRopes.put(id, rope);

        rope.ropeId = id;
    }

    public static void clearAllRopes() {
        activeRopes.clear();
        VStuff.LOGGER.info("Clearing all ropes from manager...");
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        NetworkManager.sendClearAllRopesToPlayer(player);

        syncAllRopesToPlayer(player);
    }
}
