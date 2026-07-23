package dev.flarelog.vstuff.internal.utility;

import dev.flarelog.vstuff.content.physics.VSUtil;
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
        Ship ship = null;

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



            System.out.println("" + ship.getWorldAABB());
            System.out.println("getWorldtoShipPos: " + RopeUtil.worldToShipLocal(level, worldPos, ship.getId()));
            System.out.println("getWorldPos: " + RopeUtil.getWorldPos(level, posOne, ship.getId()));
        } else  pose1 = new VSJointPose(worldPos, new Quaterniond());

        VSJointPose pose2 = new VSJointPose(ship2.getShipAABB().center(new Vector3d()), ship2.getTransform().getRotation().add(0,.5,0, 0,new Quaterniond()));

        System.out.println("id 1: " + ship.getId() + "id 2: " + ship2.getId());

        VSFixedJoint joint = new VSFixedJoint(ship.getId(), pose1, ship2.getId(), pose2, null, VSJoint.DEFAULT_COMPLIANCE );
        System.out.println("joint created");

        if (level instanceof ServerLevel serverLevel){
            System.out.println("adding joint" );
        VSUtil.getGTPA(serverLevel).addJoint(joint, 8,(id) -> {});

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
}

