package yay.evy.everest.vstuff.content.rope.roperework;

import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VStuffConfig;
import yay.evy.everest.vstuff.content.rope.styler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.foundation.utility.BodyUtils;
import yay.evy.everest.vstuff.foundation.utility.GTPAUtils;
import yay.evy.everest.vstuff.foundation.utility.JointValues;
import yay.evy.everest.vstuff.foundation.RopeStyles;

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
        this.ropeId = ropeId;
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.jointValues = jointValues;
        this.style = style;
        this.type = type;
    }

    public NewRope(NewRopeUtils.RopePosData posData0, NewRopeUtils.RopePosData posData1, JointValues jointValues, RopeStyles.RopeStyle style, NewRopeUtils.RopeType type) {
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.jointValues = jointValues;
        this.style = style;
        this.type = type;
    }

    public static Pair<NewRope, String> create(ServerLevel level, @Nullable Long ship0, @Nullable Long ship1, BlockPos blockPos0, BlockPos blockPos1, Player player, boolean taut) {
        ship0 = (BodyUtils.getGroundBodyId(level).equals(ship0)) ? null : ship0;
        ship1 = (BodyUtils.getGroundBodyId(level).equals(ship1)) ? null : ship1;
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
            return new Pair<>(null, "rope.too_long");
        }

        length = taut ? length : length + 0.5f;
        float mass0 = BodyUtils.getMassForShip(level, ship0);
        float mass1 = BodyUtils.getMassForShip(level, ship1);
        double effectiveMass = Math.max(Math.min(mass0, mass1), 100.0);
        double compliance = 1e-12 / effectiveMass * (posData0.isWorld() || posData1.isWorld() ? 0.05 : 1);
        float massRatio = Math.max(mass0, mass1) / Math.min(mass0, mass1);
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

        return new Pair<>(rope, "rope.created");
    }

    public void restoreJoint(ServerLevel level) {
        if (this.hasRestored()) {
            VStuff.LOGGER.info("Not creating joint for rope that has already restored joint!");
            return;
        }

        GTPAUtils.restoreJoint(level, this);
    }

    public boolean removeJoint(ServerLevel level) {
        if (this.jointId == null || this.joint == null || !this.hasRestored) {
            VStuff.LOGGER.warn("Cannot remove joint for rope id {} because the joint or joint id is null, or the rope has not restored its joint.", this.ropeId);
            return false;
        }

        GameToPhysicsAdapter gtpa = GTPAUtils.getGTPA(level);
        gtpa.removeJoint(this.jointId);

        this.jointId = null;
        this.joint = null;
        this.hasRestored = false;

        RopeManager.removeRope(level, this.ropeId);

        return true;
    }

    /**
     * sets a rope's joint values. any parameters that are given null will not be changed
     */
    public void setJointValues(ServerLevel level, VSJointMaxForceTorque maxForceTorque, @Nullable Float  minLength, @Nullable Float  maxLength,
                               @Nullable Double compliance, @Nullable Float  tolerance, @Nullable Float  stiffness, @Nullable Float  damping) {
        this.jointValues = this.jointValues.withChanged(maxForceTorque, minLength, maxLength, compliance, tolerance, stiffness, damping);

        GTPAUtils.editJoint(level, this);
    }

    /**
     * sets a rope's joint values. any parameters that are given null will not be changed
     */
    public void setJointValues(ServerLevel level, @Nullable Float maxForce, @Nullable Float maxTorque, @Nullable Float minLength, @Nullable Float maxLength,
                               @Nullable Double compliance, @Nullable Float tolerance, @Nullable Float stiffness, @Nullable Float  damping) {
        if (maxForce == null && maxTorque == null) {
            setJointValues(level, null, minLength, maxLength, compliance, tolerance, stiffness, damping);
            return;
        }
        VSJointMaxForceTorque maxForceTorque = new VSJointMaxForceTorque(
                maxForce == null ? jointValues.maxForceTorque().getMaxForce() : maxForce,
                maxTorque == null ? jointValues.maxForceTorque().getMaxTorque() : maxTorque
        );
        this.jointValues = this.jointValues.withChanged(maxForceTorque, minLength, maxLength, compliance, tolerance, stiffness, damping);

        GTPAUtils.editJoint(level, this);
    }

    public VSDistanceJoint makeJoint() {
        return this.jointValues.makeJoint(posData0.shipId(), posData0.localPos(), posData1.shipId(), posData1.localPos());
    }

    public boolean isRopeOnShip(Long shipId) {
        if (shipId == null) {
            return this.posData0.isWorld() || this.posData1.isWorld();
        }
        return this.posData0.getShipIdSafe().equals(shipId) || this.posData1.getShipIdSafe().equals(shipId);
    }

    public boolean hasRestored() {
        return this.hasRestored || this.joint != null || (this.jointId != null && this.jointId != -1);
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

    public static NewRope fromTag(ServerLevel level, CompoundTag ropeTag) {
        return new NewRope(
                NewRopeUtils.RopePosData.fromTag(level, ropeTag.getCompound("posData0")),
                NewRopeUtils.RopePosData.fromTag(level, ropeTag.getCompound("posData0")),
                JointValues.readJointValues(ropeTag.getCompound("jointValues")),
                RopeStyles.fromString(ropeTag.getString("style")),
                NewRopeUtils.RopeType.valueOf(ropeTag.getString("type"))
        );
    }

}
