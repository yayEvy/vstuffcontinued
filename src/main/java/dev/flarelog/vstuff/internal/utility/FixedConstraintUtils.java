package dev.flarelog.vstuff.internal.utility;

import dev.flarelog.vstuff.content.physics.VSUtil;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.AxisAngle4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.internal.joints.VSFixedJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.util.McMathUtilKt;

public class FixedConstraintUtils {

    public static void createFixedConstraint(Level level, BlockPos posOne, Ship ship2, Direction direction){

        VSJointPose pose1;
        Ship ship = null;

        BlockPos newPos = BlockPos.containing(RopeUtil.getLocalPos(level, posOne).x,
                                              RopeUtil.getLocalPos(level, posOne).y,
                                              RopeUtil.getLocalPos(level, posOne).z);

       Vector3d worldPos = new Vector3d(posOne.getCenter().toVector3f());
      //  Vector3d worldPos = RopeUtil.getLocalPos(level, posOne);

        if(ValkyrienSkies.getShipById(level, VSUtil.getLoadedShipIdAtPos(level, posOne) )!= null) {
          ship = ValkyrienSkies.getShipById(level, VSUtil.getLoadedShipIdAtPos(level, posOne));
        }
        //System.out.println(ship.getShipAABB().center(new Vector3d()));
        //System.out.println(ship.getShipAABB().center(new Vector3d()).add(getOffset(direction)));

        if (ship != null){

            pose1 = new VSJointPose(RopeUtil.getLocalPos(level, posOne).add(getOffset(direction)), ship.getTransform().getRotation());
           // pose1 = new VSJointPose(ValkyrienSkies.getShipManagingBlock(level, posOne).getTransform().getPositionInModel().add(getOffset(direction),new Vector3d()), ship.getTransform().getRotation());


        } else  pose1 = new VSJointPose(worldPos, new Quaterniond());

        System.out.println("rot: " + ship2.getTransform().getRotation().mul(getRotationOffset(direction, 45.0), new Quaterniond()).normalize());
        VSJointPose pose2 = new VSJointPose(ship2.getShipAABB().center(new Vector3d()),
                getRotationOffset(direction, 45.0).normalize().mul(ship2.getTransform().getRotation()));


        System.out.println("id 1: " + ship.getId() + "id 2: " + ship2.getId());

        // new VSJointMaxForceTorque(1e19F, 1e19F)
        VSFixedJoint joint = new VSFixedJoint(ship.getId(), pose1, ship2.getId(), pose2, null, VSJoint.DEFAULT_COMPLIANCE );

        if (level instanceof ServerLevel serverLevel){
            System.out.println("adding joint" );
        VSUtil.getGTPA(serverLevel).addJoint(joint, 8,(id) -> {});
            joint.serialized();

            System.out.println("joint added");
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