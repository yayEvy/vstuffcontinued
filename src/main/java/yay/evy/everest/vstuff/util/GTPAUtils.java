package yay.evy.everest.vstuff.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaterniond;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.content.roperework.NewRope;
import yay.evy.everest.vstuff.content.roperework.RopeManager;
import yay.evy.everest.vstuff.content.ropes.RopeTracker;
import yay.evy.everest.vstuff.network.NetworkManager;

import java.util.Collections;

public class GTPAUtils {

    public static GameToPhysicsAdapter getGTPA(ServerLevel level) {
        String dimId = ValkyrienSkies.getDimensionId(level);
        return ValkyrienSkiesMod.getOrCreateGTPA(dimId);
    }

    public static void addRopeJoint(ServerLevel level, Player player, NewRope rope) {
        VSDistanceJoint distanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        gtpa.addJoint(distanceJoint, 0, (jointId) -> {
            rope.jointId = jointId;
            rope.joint = distanceJoint;

            RopeManager.addRope(level, rope);

            if (player instanceof ServerPlayer serverPlayer) {
                RopeManager.syncAllConstraintsToPlayer(serverPlayer);
            }
        });
    }

    public static void restoreJoint(ServerLevel level, NewRope rope) {
        VSDistanceJoint distanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        if (!rope.hasRestored()) {
            gtpa.addJoint(distanceJoint, 0, (jointId) -> {
                if (jointId != -1) {
                    rope.jointId = jointId;
                    rope.joint = distanceJoint;
                    rope.hasRestored = true;

                    MinecraftServer server = level.getServer();
                    for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                        RopeTracker.syncAllConstraintsToPlayer(sp);
                    }
                }
            });
        }
    }

}
