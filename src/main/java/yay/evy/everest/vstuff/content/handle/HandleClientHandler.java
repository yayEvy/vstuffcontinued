package yay.evy.everest.vstuff.content.handle;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.WeakHashMap;

public class HandleClientHandler {

    private static final WeakHashMap<Player, Vec3> holdingPlayers = new WeakHashMap<>();

    public static void setHolding(Player player, boolean holding, Vec3 handlePos) {
        if (holding && handlePos != null)
            holdingPlayers.put(player, handlePos);
        else
            holdingPlayers.remove(player);
    }

    public static boolean isHolding(Player player) {
        return holdingPlayers.containsKey(player);
    }

    public static Vec3 getHandlePos(Player player) {
        return holdingPlayers.get(player);
    }
}
