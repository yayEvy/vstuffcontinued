package yay.evy.everest.vstuff.content.roperework;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import yay.evy.everest.vstuff.VStuffConfig;
import yay.evy.everest.vstuff.content.ropes.RopeUtil;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.GTPAUtils;
import yay.evy.everest.vstuff.util.JointValues;
import yay.evy.everest.vstuff.util.RopeStyles;

import javax.annotation.Nullable;


public class NewRope {

    public Integer ropeId;
    public Integer jointId = null;
    public NewRopeUtils.RopePosData posData0;
    public NewRopeUtils.RopePosData posData1;
    public JointValues jointValues;
    public RopeStyles.RopeStyle style;
    public NewRopeUtils.RopeType type;
    public boolean hasRestored = false;
    public VSDistanceJoint joint;
    public double renderLength;

    public NewRope(Integer ropeId, NewRopeUtils.RopePosData posData0, NewRopeUtils.RopePosData posData1, JointValues jointValues, RopeStyles.RopeStyle style, NewRopeUtils.RopeType type) {

    }

    public static NewRope create(ServerLevel level, @Nullable Long ship0, @Nullable Long ship1, BlockPos blockPos0, BlockPos blockPos1, Player player) {
        NewRopeUtils.RopePosData posData0tmp = NewRopeUtils.RopePosData.create(level, ship0, blockPos0);
        NewRopeUtils.RopePosData posData1tmp = NewRopeUtils.RopePosData.create(level, ship1, blockPos1);
        NewRopeUtils.RopePosData posData0;
        NewRopeUtils.RopePosData posData1;

        if (posData1tmp.isWorld() && !posData0tmp.isWorld()) {
            posData0 = posData1tmp;
            posData1 = posData0tmp;
        } else {
            posData0 = posData0tmp;
            posData1 = posData1tmp;
        }

        float length = posData0.getWorldPos().distance(posData1.getWorldPos());
        float maxAllowedLength = VStuffConfig.MAX_ROPE_LENGTH.get();
        if (length > maxAllowedLength) {
            NewRopeUtils.sendRopeMessage(player, "length_fail");
            return null;
        }

        length += 0.5f;
        float massA = RopeUtil.getFMassForShip(level, ship0);
        float massB = RopeUtil.getFMassForShip(level, ship1);
        double effectiveMass = Math.max(Math.min(massA, massB), 100.0);
        double compliance = 1e-12 / effectiveMass * (posData0.isWorld() || posData1.isWorld() ? 0.05 : 1);
        float massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        float maxForce = 5e13f * Math.min(massRatio, 20.0f) * (posData0.isWorld() || posData1.isWorld() ? 10f : 1f);

        RopeStyles.RopeStyle style = null;
        if (player instanceof ServerPlayer serverPlayer) {
            style = RopeStyleHandlerServer.getStyle(serverPlayer.getUUID());
        }

        if (style == null) {
            style = RopeStyles.normal();
        }

        NewRope rope = new NewRope(RopeManager.getNextId(), posData0, posData1, JointValues.withDefault(new VSJointMaxForceTorque(maxForce, maxForce), length, compliance), style, NewRopeUtils.getRopeType(posData0, posData1));

        GTPAUtils.addRopeJoint(level, player, rope);

        return rope;
    }

    public void createOrRestoreJoint(ServerLevel level) {

    }

    public void removeJoint(ServerLevel level) {

    }

    public VSDistanceJoint makeJoint() {
        return this.jointValues.makeJoint(posData0.shipId(), posData0.localPos(), posData1.shipId(), posData1.localPos());
    }

    public CompoundTag toTag() {
        CompoundTag ropeTag = new CompoundTag();

        ropeTag.put("posData0", posData0.toTag());
        ropeTag.put("posData1", posData1.toTag());
        ropeTag.put("jointValues", JointValues.writeJointValues(jointValues));
        ropeTag.putString("style", style.getStyle());
        ropeTag.putString("type", type.name());

        return ropeTag;
    }

    public static NewRope fromTag(ServerLevel level, Integer id, CompoundTag ropeTag) {
        return new NewRope(
                id,
                NewRopeUtils.RopePosData.fromTag(level, ropeTag.getCompound("posData0")),
                NewRopeUtils.RopePosData.fromTag(level, ropeTag.getCompound("posData0")),
                JointValues.readJointValues(ropeTag.getCompound("jointValues")),
                RopeStyles.fromString(ropeTag.getString("style")),
                NewRopeUtils.RopeType.valueOf(ropeTag.getString("type"))
        );
    }

}
