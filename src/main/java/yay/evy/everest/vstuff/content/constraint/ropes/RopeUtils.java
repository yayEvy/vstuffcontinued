package yay.evy.everest.vstuff.content.constraint.ropes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.content.constraint.MasterOfRopes;
import yay.evy.everest.vstuff.util.GetterUtils;

import java.util.Map;
import java.util.UUID;

public class RopeUtils {

    public static Vector3d convertWorldToLocal(ServerLevel level, Vector3d worldPos, Long shipId) {
        if (shipId != null && !shipId.equals(GetterUtils.getGroundBodyId(level))) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d localPos = new Vector3d();
                shipObject.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
                return localPos;
            }
        }
        return new Vector3d(worldPos);
    }

    public static Vector3d convertLocalToWorld(ServerLevel level, Vector3d localPos, Long shipId) {
        try {
            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            if (shipId.equals(groundBodyId)) {
                return new Vector3d(localPos);
            } else {
                Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (shipObject != null) {
                    Vector3d worldPos = new Vector3d();
                    shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                    return worldPos;
                }
            }
            return new Vector3d(localPos);
        } catch (Exception e) {
            return new Vector3d(localPos);
        }
    }

    public static Vector3d renderConvertLocalToWorld(Level level, Vector3d localPos, Long ship) { // idk, get world pos without checking ground? too tired
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return new Vector3d(localPos);

            if (ship == null || ship == 0L) {
                return new Vector3d(localPos);
            } else {
                Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(ship);
                if (shipObject != null) {
                    Vector3d worldPos = new Vector3d();


                    try {
                        ((ClientShip) shipObject).getRenderTransform().getShipToWorld().transformPosition(localPos, worldPos);
                    } catch (Exception e) {
                        shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                    }

                    return worldPos;
                }
            }
            return new Vector3d(localPos);
        } catch (Exception e) {
            return new Vector3d(localPos);
        }
    }

    public static Vector3d getWorldPosition(ServerLevel level, BlockPos pos, Long shipId) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d worldPos = new Vector3d();
                shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                return worldPos;
            }
        }
        return localPos;
    }

    public static Vector3d getLocalPosition(BlockPos pos) {

        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static boolean allowedRopeLength(ServerLevel level, BlockPos first, BlockPos second, Long firstShip, Long secondShip) {
        Vector3d worldPos0 = getWorldPosition(level, first, firstShip);
        Vector3d worldPos1 = getWorldPosition(level, second, secondShip);

        double distance = worldPos0.distance(worldPos1);
        return distance < VstuffConfig.MAX_ROPE_LENGTH.get();
    }

    public static Float getMassForShip(ServerLevel level, Long shipId) {
        Long groundBodyId = GetterUtils.getGroundBodyId(level);
        if (shipId.equals(groundBodyId)) {
            return 1e12f;
        }

        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
        if (shipObject != null) {
            try {
                float mass = 1000.0f;
                var bounds = shipObject.getShipAABB();
                if (bounds != null) {
                    float volume = (bounds.maxX() - bounds.minX()) *
                            (bounds.maxY() - bounds.minY()) *
                            (bounds.maxZ() - bounds.minZ());
                    mass = Math.max(volume * 10.0f, 1000f);
                }
                return Math.min(mass, 1e9f);
            } catch (Exception e) {
                return 1000.0f;
            }
        }
        return 1000.0f;
    }

    public static double getDistanceToRope(Vec3 eyePos, Vec3 lookVec, Vector3d ropeStart, Vector3d ropeEnd, double maxDistance) {
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

    public static Integer findRope(ServerLevel level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        double maxDistance = player.getBlockReach();
        double minDistance = Double.MAX_VALUE;
        Integer closestConstraintId = null;

        for (Map.Entry<Integer, AbstractRope> entry : MasterOfRopes.getAllActiveRopes().entrySet()) {
            Integer constraintId = entry.getKey();

            AbstractRope rope = entry.getValue();

            Vector3d worldPosA = convertLocalToWorld(level, rope.localPos0, rope.ship0);
            Vector3d worldPosB = convertLocalToWorld(level, rope.localPos1, rope.ship1);

            double distance = getDistanceToRope(eyePos, lookVec, worldPosA, worldPosB, maxDistance);
            if (distance < minDistance && distance <= 1.0) {
                minDistance = distance;
                closestConstraintId = constraintId;
            }
        }

        return closestConstraintId;
    }

    public static Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip == null ? null : loadedShip.getId();
    }

    public static void putBlockPos(String pKey, BlockPos blockPos, CompoundTag toTag) {
        toTag.putInt(pKey + "_x", blockPos.getX());
        toTag.putInt(pKey + "_y", blockPos.getY());
        toTag.putInt(pKey + "_z", blockPos.getZ());
    }

    public static void putVector3d(String pKey, Vector3d vec3d, CompoundTag toTag) {
        toTag.putDouble(pKey + "_x", vec3d.x);
        toTag.putDouble(pKey + "_y", vec3d.y);
        toTag.putDouble(pKey + "_z", vec3d.z);
    }

    public static BlockPos getBlockPos(String pKey, CompoundTag fromTag) {
        return new BlockPos(
                fromTag.getInt(pKey + "_x"),
                fromTag.getInt(pKey + "_x"),
                fromTag.getInt(pKey + "_x")
        );
    }

    public static Vector3d getVector3d(String pKey, CompoundTag fromTag) {
        return new Vector3d(
                fromTag.getDouble(pKey + "_x"),
                fromTag.getDouble(pKey + "_x"),
                fromTag.getDouble(pKey + "_x")
        );
    }
    public static Vector3d v3fToV3d(Vector3f v3f) {
        return new Vector3d (
                v3f.x,
                v3f.y,
                v3f.x
        );
    }

    public static Vector3f v3dToV3f(Vector3d v3d) { // this cannot be done easier and i hate it
        return new Vector3f (
                (float) v3d.x,
                (float) v3d.y,
                (float) v3d.z
        );
    }

    public static AbstractRope fromTag(CompoundTag tag) {
        String type = tag.getString("type");

        try {
            RopeType.valueOf(type);

            return switch (RopeType.valueOf(type)) {
                case NORMAL -> Rope.fromTag(tag);
                case PULLEY -> PulleyRope.fromTag(tag);
                case WORLDTOWORLD -> JointlessRope.fromTag(tag);
            };
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid rope type " + type + "!");
        }
    }

    public static AbstractRope fromBuf(FriendlyByteBuf buf) {
        return switch (buf.readEnum(RopeType.class)) {
            case NORMAL -> Rope.fromBuf(buf);
            case PULLEY -> PulleyRope.fromBuf(buf);
            case WORLDTOWORLD -> JointlessRope.fromBuf(buf);
        };
    }

    public static Integer createTempId() {
        return UUID.randomUUID().hashCode();
    }

    public enum RopeType {
        WORLDTOWORLD,
        NORMAL,
        PULLEY
    }

}