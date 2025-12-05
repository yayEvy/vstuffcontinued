package yay.evy.everest.vstuff.content.constraintrework;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.constraintrework.ropes.AbstractRope;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RopeManager {

    public static final Map<Integer, AbstractRope> activeRopes = new ConcurrentHashMap<>();
    private static long lastJoinTime = 0L;


    public static void ADD(ServerLevel level, AbstractRope rope) {
        activeRopes.put(rope.ID, rope);

        RopePersistenceYesItWorksNowIPromise3dsmile persistence = RopePersistenceYesItWorksNowIPromise3dsmile.get(level);

        persistence.addRope(rope);
        NetworkHandler.sendConstraintAdd(rope.ID, rope.ship0, rope.ship1, rope.localPos0, rope.localPos1, rope.maxLength, rope.style);
    }

    public static void putOrReplaceRope(Integer id, AbstractRope rope) {
        activeRopes.put(id, rope);
    }

    public static void REMOVE(ServerLevel level, Integer ropeId) {

        AbstractRope data = activeRopes.remove(ropeId);
        if (data != null) {

            RopePersistenceYesItWorksNowIPromise3dsmile persistence = RopePersistenceYesItWorksNowIPromise3dsmile.get(level);
            persistence.removeRope(ropeId);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                NetworkHandler.sendConstraintRemoveToPlayer(player, ropeId);
                level.getServer().tell(
                        new TickTask(
                                0,
                                () -> NetworkHandler.sendConstraintRemoveToPlayer(player, ropeId)
                        )
                );
            }

            NetworkHandler.sendConstraintRemove(ropeId);
        }
    }

    public static void syncAllConstraintsToPlayer(ServerPlayer player) {
        NetworkHandler.sendClearAllConstraintsToPlayer(player);
        VStuff.LOGGER.info("Attempting to sync all constraints to player {} ({})", player.getName().getString(), player.getUUID());

        for (Map.Entry<Integer, AbstractRope> entry : activeRopes.entrySet()) {
            AbstractRope data = entry.getValue();
            NetworkHandler.sendConstraintAddToPlayer(
                    player,
                    entry.getKey(),
                    data.ship0,
                    data.ship1,
                    data.localPos0,
                    data.localPos1,
                    data.maxLength,
                    data.style
            );
        }
    }

    public static Map<Integer, AbstractRope> getActiveRopes() {
        return new HashMap<>(activeRopes);
    }
}
