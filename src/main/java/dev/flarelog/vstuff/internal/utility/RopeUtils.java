package dev.flarelog.vstuff.internal.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ClientVsBody;
import org.valkyrienskies.core.api.bodies.VsBody;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import dev.flarelog.vstuff.content.ropes.phys_ropes.ReworkedPhysRope;
import dev.flarelog.vstuff.content.ropes.phys_ropes.ReworkedPhysRopeManager;
import dev.flarelog.vstuff.content.ropes.util.IRopeActor;

import javax.annotation.Nullable;

public class RopeUtils {

    public static Vector3d getLocalPos(Level level, BlockPos pos) {
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

    public static Vector3d getWorldPos(Level level, BlockPos pos, Long shipId) {
        Vector3d localPos = getLocalPos(level, pos);
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                return shipObject.getTransform().getShipToWorld().transformPosition(localPos, new Vector3d());
            }
        }
        return localPos;
    }

    public static Vector3d getWorldPos(Level level, BlockPos pos) {
        return getWorldPos(level, pos,  ShipUtils.getLoadedShipIdAtPos(level, pos));
    }

    public static BlockPos containingBlockPos(Vector3d pos) {
        return BlockPos.containing(pos.x, pos.y, pos.z);
    }

    public static Vector3d convertLocalToWorld(Level level, Vector3d localPos, Long ship) {
        if (ship == null || level == null) return localPos;

        try {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(ship);
            if (shipObject != null) {
                Vector3d worldPos = new Vector3d();
                shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                return worldPos;
            }

            return new Vector3d(localPos);
        } catch (Exception e) {
            return new Vector3d(localPos);
        }
    }

    public static Vector3d renderLocalToWorld(Level level, Vector3d localPos, Long ship) {
        if (ship == null || level == null) return localPos;

        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        ClientShip clientShip = (ClientShip) shipWorld.getAllShips().getById(ship);
        if (clientShip == null) return localPos;
        Vector3d transformedPos = clientShip.getRenderTransform().getShipToWorld().transformPosition(new Vector3d(localPos), new Vector3d());
        return new Vector3d(transformedPos.x, transformedPos.y, transformedPos.z);
    }

    public static Vector3d getClientBodyPosition(ClientLevel level, Long bodyId) {
        VsBody body = VSGameUtilsKt.getAllBodies(level).getById(bodyId);
        if (body != null) {
            return new Vector3d(((ClientVsBody) body).getRenderTransform().getPosition());
        }
        return null;
    }

    public static Vector3d getClientBodyPosition(Long bodyId) {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null ? getClientBodyPosition(level, bodyId) : null;
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

    public static Vector3d worldToShipLocal(Level level, Vector3d worldPos, Long shipId) {
        if (shipId == null) return new Vector3d(worldPos);

        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
        if (shipObject != null) {
            Vector3d local = new Vector3d();
            shipObject.getTransform().getWorldToShip().transformPosition(worldPos, local);
            return local;
        }

        return new Vector3d(worldPos);
    }

    public static @Nullable Integer findRopeId(ServerLevel level, Player player) {
        ReworkedPhysRope rope = findPhysRope(level, player);
        return rope == null ? null : rope.getRopeId();
    }

    public static @Nullable ReworkedPhysRope findPhysRope(ServerLevel level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        double maxDistance = player.getBlockReach();
        double minDistance = Double.MAX_VALUE;
        ReworkedPhysRope closest = null;

        for (ReworkedPhysRope rope : ReworkedPhysRopeManager.get(level).getRopeList()) {
            Vector3d a = rope.posData0.getWorldPos(level);
            Vector3d b = rope.posData1.getWorldPos(level);

            double dist = getDistanceToRope(eyePos, lookVec, a, b, maxDistance);
            if (dist < minDistance && dist <= 1.0) {
                minDistance = dist;
                closest = rope;
            }
        }

        return closest;
    }

    public static void playSound(ServerLevel serverLevel, BlockPos pos, SoundEvent sound) {
        serverLevel.playSound(
                null,
                pos,
                sound,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }

    public static SelectType getSelectType(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IRopeActor) {
            return SelectType.ACTOR;
        }
        return SelectType.NORMAL;
    }

    public enum ConnectionType {
        NORMAL,
        PULLEY
    }

    public enum PosType {
        SHIP,
        WORLD,
    }

    public enum SelectType {
        NORMAL,
        ACTOR
    }
}