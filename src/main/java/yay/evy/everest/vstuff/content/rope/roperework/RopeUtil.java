package yay.evy.everest.vstuff.content.rope.roperework;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VStuffConfig;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.client.ClientRopeUtil;
import yay.evy.everest.vstuff.foundation.RopeStyles;
import yay.evy.everest.vstuff.foundation.utility.BodyUtils;
import yay.evy.everest.vstuff.foundation.utility.PosUtils;
import yay.evy.everest.vstuff.index.VStuffItems;

import javax.annotation.Nullable;

import java.util.Map;

import static yay.evy.everest.vstuff.foundation.utility.PosUtils.getBlockType;

public class RopeUtil {

    public static Vector3f getRopeConnectionPos(Level level, BlockPos pos) {
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

    public static Vector3f getLocalPos(ServerLevel level, BlockPos pos) {
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

    public static Vector3f convertLocalToWorld(Level level, Vector3f localPos, Long shipId) {
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d transformedPos = shipObject.getTransform().getShipToWorld().transformPosition(new Vector3d(localPos), new Vector3d());
                return new Vector3f((float) transformedPos.x, (float) transformedPos.y, (float) transformedPos.z);
            }
        }
        return localPos;
    }

    public static Vector3f getRopeConnectionPosWorld(Level level, BlockPos pos, Long shipId) {
        Vector3f localPos = getRopeConnectionPos(level, pos);
        if (shipId != null) {
            Ship ship = PosUtils.getShipAtPos(level, pos);
            if (ship != null) {
                Vector3d transformedPos = ship.getTransform().getShipToWorld().transformPosition(new Vector3d(localPos), new Vector3d());
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

        for (Map.Entry<Integer, Rope> entry : RopeManager.getActiveRopes().entrySet()) {
            Integer constraintId = entry.getKey();

            Rope rope = entry.getValue();

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

    public enum SelectType {
        NORMAL,
        PULLEY
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
        public RopePosData(ServerLevel level, @Nullable Long shipId, BlockPos blockPos, Vector3f localPos, PosType posType, BlockType blockType) {
            this.level = level;
            this.shipId = BodyUtils.getGroundBodyId(level).equals(shipId) ? null : shipId; // if made from tag, we convert the ground body id back into null
            this.blockPos = blockPos;
            this.localPos = localPos;
            this.posType = posType;
            this.blockType = blockType;
        }

        public static RopePosData create(ServerLevel level, Long id, BlockPos pos) {
            if (BodyUtils.getGroundBodyId(level).equals(id)) {
                VStuff.LOGGER.warn("RopePosData received actual id for ground body identifier instead of null (expected value), correcting");
                id = null;
            }
            PosType posType = id == null ? PosType.WORLD : PosType.SHIP;
            Vector3f localPos = getLocalPos(level, pos);

            return new RopePosData(level, id, pos, localPos, posType, getBlockType(level, pos));
        }

        public boolean isWorld() {
            return this.posType == PosType.WORLD;
        }

        public @NotNull Long getShipIdSafe() {
            return shipId == null ? BodyUtils.getGroundBodyId(level) : shipId;
        }

        public Vector3f getWorldPos() {
            return convertLocalToWorld(this.level, this.localPos, this.shipId);
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();

            tag.putLong("shipId", shipId == null ? BodyUtils.getGroundBodyId(this.level) : shipId); // use ground body id for tag
            tag.put("blockPos", NbtUtils.writeBlockPos(blockPos));
            tag.put("localPos", writeVector3f(localPos));
            tag.putString("posType", posType.name());
            tag.putString("blockType", blockType.name());

            return tag;
        }

        public static RopePosData fromTag(ServerLevel level, CompoundTag tag) {
            Long shipId = tag.getLong("shipId");
            BlockPos blockPos = NbtUtils.readBlockPos(tag.getCompound("blockPos"));
            Vector3f localPos = readVector3f(tag.getCompound("localPos"));
            PosType posType = PosType.valueOf(tag.getString("posType"));
            BlockType blockType = BlockType.valueOf(tag.getString("blockType"));

            return new RopePosData(level, shipId, blockPos, localPos, posType, blockType);
        }
    }

    public static CompoundTag writeVector3f(Vector3f vector3f) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("X", vector3f.x);
        tag.putFloat("Y", vector3f.y);
        tag.putFloat("Z", vector3f.z);
        return tag;
    }

    public static Vector3f readVector3f(CompoundTag tag) {
        return new Vector3f(tag.getFloat("X"), tag.getFloat("Y"), tag.getFloat("Z"));
    }

}
