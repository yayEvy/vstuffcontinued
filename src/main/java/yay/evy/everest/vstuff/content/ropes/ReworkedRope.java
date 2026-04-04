package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.valkyrienskies.core.internal.joints.*;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.type.RopeType;
import yay.evy.everest.vstuff.content.ropes.type.RopeTypeManager;
import yay.evy.everest.vstuff.internal.utility.*;

import javax.annotation.Nullable;
import java.util.Objects;

public class ReworkedRope {

    Integer ropeId;
    Integer jointId;
    public RopePosData posData0;
    public RopePosData posData1;
    public JointValues jointValues;
    public RopeType type;
    public final boolean hasJoint;

    protected ReworkedRope(RopePosData posData0, RopePosData posData1, JointValues values, ResourceLocation type) {
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.jointValues = values;
        this.type = RopeTypeManager.get(type);
        this.hasJoint = !posData0.sameShip(posData1);
    }

    public Integer getRopeId() {
        return ropeId;
    }

    public Integer getJointId() {
        return hasTrackedJoint() ? jointId : -1;
    }

    public ReworkedRope setRopeId(Integer to) {
        if (ropeId != null) {
            VStuff.LOGGER.warn("Blocking attempt to set ropeId when it has already been set.");
        } else {
            this.ropeId = Objects.requireNonNull(to, "Cannot set ropeId to a null value!");
        }

        return this;
    }

    public ReworkedRope setJointId(Integer to) {
        if (jointId != null) {
            VStuff.LOGGER.warn("Blocking attempt to set jointId when it has already been set.");
        } else if (!hasJoint) {
            VStuff.LOGGER.warn("Cannot set jointId for a rope that does not have a joint!");
        } else if (!(to >= 0)) {
            VStuff.LOGGER.warn("Received invalid value for jointId: {}", to);
        } else {
            this.jointId = Objects.requireNonNull(to, "Cannot set jointId to a null value!");
        }

        return this;
    }

    public boolean atBlockPos(BlockPos blockPos) {
        return this.posData0.blockPos().equals(blockPos) || this.posData1.blockPos().equals(blockPos);
    }

    public void unsafeSetJointId(Integer to) {
        this.jointId = to;
    }

    public void setJointLength(ServerLevel level, Float newLength) {
        setJointValues(level, null, null, newLength, null, null, null, null);
    }

    /**
     * sets a rope's joint values. any parameters that are given null will not be changed
     */
    public void setJointValues(ServerLevel level, @Nullable VSJointMaxForceTorque maxForceTorque, @Nullable Float minLength, @Nullable Float maxLength,
                               @Nullable Double compliance, @Nullable Float tolerance, @Nullable Float stiffness, @Nullable Float damping) {
        this.jointValues = this.jointValues.withChanged(maxForceTorque, minLength, maxLength, compliance, tolerance, stiffness, damping);

        GTPAUtils.editJoint(level, this);
    }

    public VSDistanceJoint makeJoint() {
        return this.jointValues.makeJoint(posData0.shipId(), posData0.localPos(), posData1.shipId(), posData1.localPos());
    }

    public boolean hasTrackedJoint() {
        return this.jointId != null && this.jointId != -1;
    }

    public void attachActors(ServerLevel level) {
        posData0.attach(level, ropeId);
        posData1.attach(level, ropeId);
    }

    public void detachActors(ServerLevel level) {
        posData0.remove(level, ropeId);
        posData1.remove(level, ropeId);
    }
}
