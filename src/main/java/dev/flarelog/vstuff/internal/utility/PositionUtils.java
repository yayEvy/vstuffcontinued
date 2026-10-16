package dev.flarelog.vstuff.internal.utility;

import dev.flarelog.vstuff.content.physics.VSUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ClientVsBody;
import org.valkyrienskies.core.api.bodies.VsBody;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class PositionUtils {
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
        return getWorldPos(level, pos,  VSUtil.getLoadedShipIdAtPos(level, pos));
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
}
