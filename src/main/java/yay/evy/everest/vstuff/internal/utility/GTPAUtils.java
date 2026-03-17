package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointAndId;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeManager;

public class GTPAUtils {

    public static GameToPhysicsAdapter getGTPA(ServerLevel level) {
        String dimId = ValkyrienSkies.getDimensionId(level);
        return ValkyrienSkiesMod.getOrCreateGTPA(dimId);
    }

    public static void addRopeJoint(ServerLevel level, Player player, ReworkedRope rope) {
        VSDistanceJoint distanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        gtpa.addJoint(distanceJoint, 0, (jointId) -> {
            rope.jointId = jointId;

            RopeManager.addRopeWithPersistence(level, rope);

            rope.posData0.attach(level, rope.ropeId);
            rope.posData1.attach(level, rope.ropeId);

            if (player instanceof ServerPlayer serverPlayer) {
                RopeManager.syncAllRopesToPlayer(serverPlayer);
            }
        });
    }

    public static void editJoint(ServerLevel level, ReworkedRope rope) {
        VSDistanceJoint newDistanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        gtpa.updateJoint(new VSJointAndId(rope.jointId, newDistanceJoint));
    }

    public static void restoreJoint(ServerLevel level, ReworkedRope rope) {
        VSDistanceJoint distanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        if (!rope.hasRestored()) {
            gtpa.addJoint(distanceJoint, 0, (jointId) -> {
                if (jointId != -1) {
                    rope.jointId = jointId;
                    rope.hasRestored = true;

                    rope.posData0.attach(level, rope.ropeId);
                    rope.posData1.attach(level, rope.ropeId);

                    MinecraftServer server = level.getServer();
                    for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                        RopeManager.syncAllRopesToPlayer(sp);
                    }
                }
            });
        }
    }

    public static void removeJoint(ServerLevel level, ReworkedRope rope) {
        GameToPhysicsAdapter gtpa = getGTPA(level);

        gtpa.removeJoint(rope.jointId);

        rope.jointId = null;
        rope.hasRestored = false;

        rope.posData0.remove(level, rope.ropeId);
        rope.posData1.remove(level, rope.ropeId);

        RopeManager.removeRopeWithPersistence(level, rope.ropeId);
    }

}