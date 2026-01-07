package yay.evy.everest.vstuff.content.roperework;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.Rope;
import yay.evy.everest.vstuff.content.ropes.RopeTracker;
import yay.evy.everest.vstuff.util.BodyUtils;

import javax.annotation.Nullable;

import java.util.Map;

import static yay.evy.everest.vstuff.util.PosUtils.getBlockType;

public class NewRopeUtils {

    public static Vector3f getRopeConnectionPos(ServerLevel level, BlockPos pos) {
        Vector3f blockPos;
        try {
            VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
            Vector3f vec = shape.bounds().getCenter().add(pos.getCenter()).toVector3f();
            blockPos = new Vector3f(vec.x - 0.5f, vec.y - 0.5f, vec.z - 0.5f);
        } catch (UnsupportedOperationException ex) {
            blockPos = new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        }

        return blockPos;
    }

    public static Vector3f convertLocalToWorld(ServerLevel level, Vector3f localPos, Long shipId) {
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d transformedPos = shipObject.getTransform().getShipToWorld().transformPosition(new Vector3d(localPos), new Vector3d());
                return new Vector3f((float) transformedPos.x, (float) transformedPos.y, (float) transformedPos.z);
            }
        }
        return localPos;
    }


    public static double getDistanceToRope(Vec3 eyePos, Vec3 lookVec, Vector3f ropeStart, Vector3f ropeEnd, double maxDistance) {
        Vec3 start = new Vec3(ropeStart.x, ropeStart.y, ropeStart.z);
        Vec3 end = new Vec3(ropeEnd.x, ropeEnd.y, ropeEnd.z);
        double minDistanceToRope = Double.MAX_VALUE;

        for (double t = 0; t <= maxDistance; t += 0.5) {
            Vec3 rayPoint = eyePos.add(lookVec.scale(t));
            Vec3 ropeVec = end.subtract(start);
            Vec3 startToRay = rayPoint.subtract(start);
            double ropeLength = ropeVec.length();
            if (ropeLength < 0.01) continue;

            double projection = startToRay.dot(ropeVec) / (ropeLength * ropeLength);
            projection = Math.max(0, Math.min(1, projection));

            Vec3 closestPointOnRope = start.add(ropeVec.scale(projection));
            double distanceToRope = rayPoint.distanceTo(closestPointOnRope);
            minDistanceToRope = Math.min(minDistanceToRope, distanceToRope);
        }

        return minDistanceToRope;
    }

    public static Integer getTargetedRope(ServerLevel level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        double maxDistance = player.getBlockReach();
        double minDistance = Double.MAX_VALUE;
        Integer closestConstraintId = null;

        for (Map.Entry<Integer, NewRope> entry : RopeManager.getActiveRopes().entrySet()) {
            Integer constraintId = entry.getKey();

            NewRope rope = entry.getValue();

            Vector3f worldPos0 = rope.posData0.getWorldPos();
            Vector3f worldPos1 = rope.posData1.getWorldPos();

            double distance = getDistanceToRope(eyePos, lookVec, worldPos0, worldPos1, maxDistance);
            if (distance < minDistance && distance <= 1.0) {
                minDistance = distance;
                closestConstraintId = constraintId;
            }
        }

        return closestConstraintId;
    }


    public enum BlockType {
        NORMAL,
        PULLEY,
        PULLEY_ANCHOR
    }

    public enum PosType {
        SHIP,
        WORLD,
    }

    public enum RopeType {
        WW,
        WS,
        SS
    }

    public static RopeType getRopeType(RopePosData posData0, RopePosData posData1) {
        if (posData0.isWorld() && !posData1.isWorld()) {
            return RopeType.WS;
        } else if (posData0.isWorld()) {
            return RopeType.WW;
        } else {
            return RopeType.SS;
        }
    }

    public record RopePosData(ServerLevel level, @Nullable Long shipId, BlockPos blockPos, Vector3f localPos, PosType posType, BlockType blockType) {
        public static RopePosData create(ServerLevel level, Long id, BlockPos pos) {
            if (BodyUtils.getGroundBodyId(level).equals(id)) {
                VStuff.LOGGER.warn("RopePosData received actual id for ground body identifier instead of null (expected value), correcting");
                id = null;
            }
            PosType posType = id == null ? PosType.WORLD : PosType.SHIP;
            Vector3f localPos = getRopeConnectionPos(level, pos);

            return new RopePosData(level, id, pos, localPos, posType, getBlockType(level, pos));
        }

        public boolean isWorld() {
            return this.posType == PosType.WORLD;
        }

        public Vector3f getWorldPos() {
            return convertLocalToWorld(this.level, this.localPos, this.shipId);
        }
    }

    public static void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message.rope_" + name),
                true
        );
    }
}
