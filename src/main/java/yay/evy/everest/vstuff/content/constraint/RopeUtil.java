package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RopeUtil {

    /**
     to store levels in rope objects, we store an id for each differing level
     so we can get levels even after loading from a CompoundTag, and we don't
     store the levels by constraint id to save space (or smthn idk it's better)
     */
    private final static Map<String, ServerLevel> ropeLevels = new HashMap<>();


    public static String registerLevel(ServerLevel level) {
        String levelId = null;
        if (ropeLevels.containsValue(level)) { // return the existing key for the level
            for (Map.Entry<String, ServerLevel> entry: ropeLevels.entrySet()) {
                if (entry.getValue() == level) levelId = entry.getKey();
            }
        } else {
            levelId = UUID.randomUUID().toString();
            ropeLevels.put(levelId, level);
        }

        return levelId;
    }

    public static ServerLevel getRegisteredLevel(String id) {
        return ropeLevels.get(id);
    }

    public static Vector3d convertWorldToLocal(ServerLevel level, Vector3d worldPos, Long shipId) {
        if (shipId != null && !shipId.equals(getGroundBodyId(level))) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d localPos = new Vector3d();
                shipObject.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
                return localPos;
            }
        }
        return new Vector3d(worldPos);
    }

    public static Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
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

    public static Long whythefuckisitsupposedtobenullwhyyyyy(Long shipId, ServerLevel level) {
        if (shipId == null) return null;
        if (shipId.equals(getGroundBodyId(level))) return null;
        return shipId;
    }

    public static Vector3d getLocalPositionFixed(ServerLevel level, BlockPos pos, Long clickedShipId, Long targetShipId) {
        Vector3d blockPos;
        try {
            VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
            Vec3 vec = shape.bounds().getCenter().add(pos.getCenter());
            blockPos = new Vector3d(vec.x - 0.5, vec.y - 0.5, vec.z - 0.5);
        } catch (UnsupportedOperationException ex) {
            blockPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }


        if (clickedShipId != null && clickedShipId.equals(targetShipId)) {
            return blockPos;
        }

        if (targetShipId.equals(getGroundBodyId(level))) {
            if (clickedShipId != null) {
                Ship clickedShip = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(clickedShipId);
                if (clickedShip != null) {
                    Vector3d worldPos = new Vector3d();
                    clickedShip.getTransform().getShipToWorld().transformPosition(blockPos, worldPos);
                    return worldPos;
                }
            }
            return blockPos;
        }

        Ship targetShip = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(targetShipId);
        if (targetShip != null) {
            Vector3d worldPos = blockPos;
            if (clickedShipId != null && !clickedShipId.equals(targetShipId)) {
                Ship clickedShip = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(clickedShipId);
                if (clickedShip != null) {
                    worldPos = new Vector3d();
                    clickedShip.getTransform().getShipToWorld().transformPosition(blockPos, worldPos);
                }
            }
            Vector3d localPos = new Vector3d();
            targetShip.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
            return localPos;
        }
        return blockPos;
    }

    public static Vector3d getLocalPos(ServerLevel level, BlockPos pos) {
        Vector3d blockPos;
        try {
            VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
            Vec3 vec = shape.bounds().getCenter().add(pos.getCenter());
            blockPos = new Vector3d(vec.x - 0.5, vec.y - 0.5, vec.z - 0.5);
        } catch (UnsupportedOperationException ex) {
            blockPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        return blockPos;
    }

    public static Double getMassForShip(ServerLevel level, Long shipId) {
        Long groundBodyId = getGroundBodyId(level);
        if (shipId == null || shipId.equals(groundBodyId)) {
            return 1e12;
        }

        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
        if (shipObject != null) {
            try {
                double mass = 1000.0;
                var bounds = shipObject.getShipAABB();
                if (bounds != null) {
                    double volume = (bounds.maxX() - bounds.minX()) *
                            (bounds.maxY() - bounds.minY()) *
                            (bounds.maxZ() - bounds.minZ());
                    mass = Math.max(volume * 10.0, 1000.0);
                }
                return Math.min(mass, 1e9);
            } catch (Exception e) {
                return 1000.0;
            }
        }
        return 1000.0;
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

    public static Integer findTargetedLead(ServerLevel level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        double maxDistance = player.getBlockReach();
        double minDistance = Double.MAX_VALUE;
        Integer closestConstraintId = null;

        for (Map.Entry<Integer, Rope> entry : RopeTracker.getActiveRopes().entrySet()) {
            Integer constraintId = entry.getKey();

            Rope rope = entry.getValue();

            Vector3d worldPosA = rope.getWorldPosA(level);
            Vector3d worldPosB = rope.getWorldPosB(level);

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
        return loadedShip != null ? loadedShip.getId() : null;
    }

    public enum RopeInteractionReturn {
        SUCCESS,
        FAIL,
        RESET
    }

    public enum ConnectionType {
        NORMAL,
        PULLEY
    }

    public enum ConstraintType {
        GENERIC,
        PULLEY
    }

    public enum RopeType {
        WW,
        WS,
        SS
    }

    public record RopeReturn(RopeInteractionReturn result, Rope rope){
        static RopeReturn FAIL = new RopeReturn(RopeInteractionReturn.FAIL, null);
    }

    public static void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
    }


}