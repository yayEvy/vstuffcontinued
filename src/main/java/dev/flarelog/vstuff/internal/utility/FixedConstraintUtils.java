package dev.flarelog.vstuff.internal.utility;

import dev.flarelog.vstuff.content.physics.VSUtil;
import dev.flarelog.vstuff.content.physics.ships.nails.NailItem;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.internal.joints.VSFixedJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.api.ValkyrienSkies;

public class FixedConstraintUtils {

    public static void createFixedConstraint(Level level, BlockPos posOne, Ship ship2, Direction direction){

        VSJointPose pose1;
        VSFixedJoint joint =null;
        Long ship1ID = VSUtil.getLoadedShipIdAtPos(level, posOne);

       Vector3d ikThisIsGoofyButIdcRnTbl = new Vector3d(posOne.getCenter().toVector3f());
       Vector3d worldsPos = ikThisIsGoofyButIdcRnTbl.add(getOffset(direction));

        VSJointPose pose2 = new VSJointPose(ship2.getShipAABB().center(new Vector3d()),getRotationOffset(direction, NailItem.rotation).normalize().mul(ship2.getTransform().getRotation()));

        if (ship1ID == null) {

            pose1 = new VSJointPose(worldsPos, new Quaterniond());
            joint = new VSFixedJoint(null, pose1, ship2.getId(), pose2, null, VSJoint.DEFAULT_COMPLIANCE);
        }

        if (ship1ID != null) {

            Ship ship = ValkyrienSkies.getShipById(level, VSUtil.getLoadedShipIdAtPos(level, posOne));
            pose1 = new VSJointPose(RopeUtil.getLocalPos(level, posOne).add(getOffset(direction)), ship.getTransform().getRotation());
            joint = new VSFixedJoint(ship.getId(), pose1, ship2.getId(), pose2, null, VSJoint.DEFAULT_COMPLIANCE );

        }

        if (level instanceof ServerLevel serverLevel){
        VSUtil.getGTPA(serverLevel).addJoint(joint, 8,(id) -> {});
            joint.serialized();
    }}

    public static Vector3d getOffset(Direction direction) {

        System.out.println(direction);

        return switch (direction) {
            case UP ->    new Vector3d(0, 1, 0);
            case DOWN ->  new Vector3d(0, -1, 0);
            case WEST ->  new Vector3d(-1, 0, 0);
            case EAST ->  new Vector3d(1, 0, 0);
            case NORTH -> new Vector3d(0, 0, -1);
            case SOUTH -> new Vector3d(0, 0, 1);
        };
    }

public static Quaterniond getRotationOffset(Direction direction, Double rotation){

    return switch (direction) {
        case UP ->    new Quaterniond().rotateY(Math.toRadians(rotation));
        case DOWN ->  new Quaterniond().rotateY(-(Math.toRadians(rotation)));
        case WEST ->  new Quaterniond().rotateX(-(Math.toRadians(rotation)));
        case EAST ->  new Quaterniond().rotateX((Math.toRadians(rotation)));
        case NORTH -> new Quaterniond().rotateZ(-(Math.toRadians(rotation)));
        case SOUTH -> new Quaterniond().rotateZ((Math.toRadians(rotation)));
    };



//    return switch (direction) {
//        case UP ->    new Quaterniond(0,rotation,0,0);
//        case DOWN ->  new Quaterniond(0,-rotation,0,0);
//        case WEST ->  new Quaterniond(-rotation,0,0,0);
//        case EAST ->  new Quaterniond(rotation,0,0,0);
//        case NORTH -> new Quaterniond(0,0,-rotation,0);
//        case SOUTH -> new Quaterniond(0,0,rotation,0);
//    };
//

}



}