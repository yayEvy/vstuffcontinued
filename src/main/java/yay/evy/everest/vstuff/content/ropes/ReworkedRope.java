package yay.evy.everest.vstuff.content.ropes;

import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.*;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VStuffConfig;
import yay.evy.everest.vstuff.content.ropes.styler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.internal.RopeStyleManager;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;
import yay.evy.everest.vstuff.internal.utility.*;

import javax.annotation.Nullable;

import static yay.evy.everest.vstuff.internal.utility.RopeUtils.getRopeType;

public class ReworkedRope {

    public Integer ropeId;
    public Integer jointId = null;
    public RopePosData posData0;
    public RopePosData posData1;
    public JointValues jointValues;
    public ResourceLocation style;
    public RopeUtils.RopeType type;
    public boolean hasRestored = false;
    public VSDistanceJoint joint;

    /**
     * yes rope very cool wow
     * this is used for anything with ropes, it stores all data
     */
    private ReworkedRope(Integer ropeId, RopePosData posData0, RopePosData posData1, JointValues values, ResourceLocation style, RopeUtils.RopeType type) {
        this.ropeId = ropeId;
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.jointValues = values;
        this.style = style;
        this.type = type;
    }

    public static Pair<ReworkedRope, String> create(ServerLevel level, Long ship0, Long ship1, BlockPos blockPos0, BlockPos blockPos1, Player player, boolean taut) {
        ship0 = (ShipUtils.getGroundBodyId(level).equals(ship0)) ? null : ship0;
        ship1 = (ShipUtils.getGroundBodyId(level).equals(ship1)) ? null : ship1;
        RopePosData posData0tmp = RopePosData.create(level, ship0, blockPos0);
        RopePosData posData1tmp = RopePosData.create(level, ship1, blockPos1);
        RopePosData posData0;
        RopePosData posData1;

        if (posData1tmp.isWorld() && !posData0tmp.isWorld()) {
            posData0 = posData1tmp;
            posData1 = posData0tmp;
        } else {
            posData0 = posData0tmp;
            posData1 = posData1tmp;
        }

        double length = posData0.getWorldPos(level).distance(posData1.getWorldPos(level));
        double maxAllowedLength = VStuffConfig.MAX_ROPE_LENGTH.get();
        if (length > maxAllowedLength) {
            return new Pair<>(null, "rope.too_long");
        }

        length = taut ? length : length + 0.5f;
        double mass0 = ShipUtils.getMassForShip(level, ship0);
        double mass1 = ShipUtils.getMassForShip(level, ship1);
        double effectiveMass = Math.max(Math.min(mass0, mass1), 100.0);
        double compliance = 1e-12 / effectiveMass * (posData0.isWorld() || posData1.isWorld() ? 0.05 : 1);
        double massRatio = Math.max(mass0, mass1) / Math.min(mass0, mass1);
        double maxForce = 5e13f * Math.min(massRatio, 20.0f) * (posData0.isWorld() || posData1.isWorld() ? 10f : 1f);

        ResourceLocation style = player != null ? RopeStyleHandlerServer.getStyle(player.getUUID()) : RopeStyleManager.defaultId;

        ReworkedRope rope = new ReworkedRope(RopeManager.getNextId(), posData0, posData1, JointValues.withDefault(new VSJointMaxForceTorque((float) maxForce, (float) maxForce), (float) length, compliance), style, getRopeType(posData0, posData1));

        if (posData0.getShipIdSafe(level).equals(posData1.getShipIdSafe(level))) {
            RopeManager.addRopeWithPersistence(level, rope);

            if (player instanceof ServerPlayer serverPlayer) {
                RopeManager.syncAllRopesToPlayer(serverPlayer);
            }
            return new Pair<>(rope, "rope.created");
        }
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

    public void removeJoint(ServerLevel level) {
        if (this.type != RopeUtils.RopeType.WW) {
            if (!this.hasRestored()) {
                VStuff.LOGGER.warn("Cannot remove joint for rope id {} because it has not restored its joint.", this.ropeId);
                return;
            }

            GameToPhysicsAdapter gtpa = GTPAUtils.getGTPA(level);
            gtpa.removeJoint(this.jointId);

            this.jointId = null;
            this.joint = null;
            this.hasRestored = false;

            RopeManager.removeRopeWithPersistence(level, this.ropeId);
        } else {
            RopeManager.removeRopeWithPersistence(level, this.ropeId);
        }
    }

    public void setJointLength(ServerLevel level, Float newLength) {
        setJointValues(level, null, null, newLength, null, null, null, null);
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

    public boolean isRopeOnShip(ServerLevel level, Long shipId) {
        if (shipId == null) {
            return this.posData0.isWorld() || this.posData1.isWorld();
        }
        return this.posData0.getShipIdSafe(level).equals(shipId) || this.posData1.getShipIdSafe(level).equals(shipId);
    }

    public boolean hasRestored() {
        return this.hasRestored || (this.jointId != null && this.jointId != -1);
    }

    public CompoundTag toTag() {
        CompoundTag ropeTag = new CompoundTag();

        ropeTag.putInt("ropeId", ropeId);
        ropeTag.put("posData0", posData0.toTag());
        ropeTag.put("posData1", posData1.toTag());
        ropeTag.put("jointValues", JointValues.writeJointValues(jointValues));
        ropeTag.putString("namespace", style.getNamespace());
        ropeTag.putString("path", style.getPath());
        ropeTag.putString("type", type.name());

        return ropeTag;
    }

    public static ReworkedRope fromTag(CompoundTag ropeTag) {
        return new ReworkedRope(
                ropeTag.getInt("ropeId"),
                RopePosData.fromTag(ropeTag.getCompound("posData0")),
                RopePosData.fromTag(ropeTag.getCompound("posData1")),
                JointValues.readJointValues(ropeTag.getCompound("jointValues")),
                new ResourceLocation(ropeTag.getString("namespace"), ropeTag.getString("path")),
                RopeUtils.RopeType.valueOf(ropeTag.getString("type"))
        );
    }
}

/*
hidden devlog 1
wren here
i've been lost in the rope forest for so many days now... i don't know if i'll ever get out
so many ropes...
 */