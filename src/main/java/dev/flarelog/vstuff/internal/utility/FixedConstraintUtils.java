package dev.flarelog.vstuff.internal.utility;


import dev.flarelog.vstuff.content.physics.ships.nails.Nail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.internal.joints.*;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.physics_api.Pose;
import org.valkyrienskies.physics_api.joints.JointAndId;
import org.valkyrienskies.physics_api.joints.JointData;
import org.valkyrienskies.physics_api.joints.JointType;
import org.valkyrienskies.physics_api.joints.MaxForceTorque;

import java.util.HashMap;
import java.util.Objects;

public class FixedConstraintUtils {

    public static HashMap<Nail, JointAndId> nailToJoints = new HashMap<>();

    public static VSFixedJoint makeFixedJoint(Level level, BlockPos posOne, @NotNull Ship nailHead, @NotNull Nail nail){

        VSJointPose pose1;
        VSFixedJoint joint = null;
        Long ship1ID = VSUtils.getLoadedShipIdAtPos(level, posOne);

       Vector3d ikThisIsGoofyButIdcRnTbl = new Vector3d(posOne.getCenter().toVector3f());

       Vector3d worldsPos = ikThisIsGoofyButIdcRnTbl.add(getOffset(nail.direction));


        VSJointPose pose2 = new VSJointPose(nailHead.getShipAABB().center(new Vector3d()),getRotationOffset(nail.direction, nail.rotation).normalize().mul(nailHead.getTransform().getRotation()));

        if (ship1ID == null) {

            pose1 = new VSJointPose(worldsPos, new Quaterniond());
            joint = new VSFixedJoint(
                    null,
                    pose1,
                    nailHead.getId(),
                    pose2,
                    null,
                    VSJoint.DEFAULT_COMPLIANCE);
        }

        if (ship1ID != null) {

            Ship ship = ValkyrienSkies.getShipById(level, VSUtils.getLoadedShipIdAtPos(level, posOne));
            pose1 = new VSJointPose(VSUtils.getLocalPos(level, posOne).add(getOffset(nail.direction)), ship.getTransform().getRotation());
            joint = new VSFixedJoint(
                    ship.getId(),
                    pose1,
                    nailHead.getId(),
                    pose2,
                    null,
                    VSJoint.DEFAULT_COMPLIANCE );

        }

        return joint;

       }


    public static void createFixedConstraint(Level level, BlockPos posOne, Ship ship2, Nail nail){

        VSFixedJoint joint = makeFixedJoint(level, posOne, ship2, nail);

        if (level instanceof ServerLevel serverLevel){
            VSUtils.getGTPA(serverLevel).addJoint(joint, 5,(id) -> {
                JointAndId JnI = new JointAndId(id,dataFromJoint(joint));
                nailToJoints.put(nail,JnI);
            });
            joint.serialized();

        }
    }

    public static void updateFixedConstraintRotation(Level level, Nail nail){

        VSFixedJoint newJoint;
        System.out.println("joint and ID : " + nailToJoints.get(nail));

        @Nullable VSFixedJoint oldJoint = getJointFromData(nailToJoints.get(nail).getJointData());
        Long id0 = oldJoint != null ? oldJoint.getShipId0() : null;
        
        System.out.println("old joint: " + oldJoint);

        nail.rotation = nail.rotation + 15.0;

        if (id0 != null){
            newJoint = new VSFixedJoint(
                    oldJoint.getShipId0(),
                    oldJoint.getPose0(),
                    oldJoint.getShipId1(),
                    new VSJointPose(oldJoint.getPose1().getPos(),getRotationOffset(nail.direction, nail.rotation)),
                null,
//                oldJoint.getMaxForceTorque(),
                oldJoint.getCompliance()
                );
        } else {
            newJoint = new VSFixedJoint(
                    null,
                    oldJoint.getPose0(),
                    oldJoint.getShipId1(),
                    new VSJointPose(oldJoint.getPose1().getPos(),getRotationOffset(nail.direction, nail.rotation)),
                    null,
//                oldJoint.getMaxForceTorque(),
                    oldJoint.getCompliance()
            );

        }


        VSJointAndId jointAndId = new VSJointAndId(nailToJoints.get(nail).getJointId(),newJoint);
    if (level instanceof ServerLevel serverLevel){

        VSUtils.getGTPA(serverLevel).updateJoint(jointAndId);

        }
    }

    private static @Nullable VSFixedJoint getJointFromData(JointData oldJointData) {
        VSJointPose p1 = new VSJointPose(oldJointData.getLocalFrame0().getPos(),oldJointData.getLocalFrame0().getRot());
        VSJointPose p2 = new VSJointPose(oldJointData.getLocalFrame1().getPos(), oldJointData.getLocalFrame1().getRot());
//        VSJointMaxForceTorque forceTorque = new VSJointMaxForceTorque(
//                (
//                        oldJointData.getBreakForceTorque()).getMaxForce(),
//                        oldJointData.getBreakForceTorque().getMaxTorque());
     Long id0;

         if(oldJointData.getPhysicsBodyId0() != null) {

             return new VSFixedJoint(
                     Objects.requireNonNull(oldJointData.getPhysicsBodyId0()).longValue(),
                     p1,
                     Objects.requireNonNull(oldJointData.getPhysicsBodyId1()).longValue(),
                     p2,
                     new VSJointMaxForceTorque(0.0f, 0.0f), // we dont use this but mfs in runtime want it
                     oldJointData.getCompliance());
         }
        else {

            return new VSFixedJoint(
                    null,
                    p1,
                    Objects.requireNonNull(oldJointData.getPhysicsBodyId1()).longValue(),
                    p2,
                    new VSJointMaxForceTorque(0.0f,0.0f), // we dont use this but mfs in runtime want it
                    oldJointData.getCompliance());

        }




    }


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


}

public static JointData dataFromJoint(VSJoint joint){

        return new JointData() {
            @Override
            public @Nullable Integer getPhysicsBodyId0() {
                if (joint.getShipId0() != null) return Math.toIntExact(joint.getShipId0());
                else return null;
            }

            @Override
            public @NotNull Pose getLocalFrame0() {
                return new Pose(joint.getPose0().getPos(),joint.getPose0().getRot());
            }

            @Override
            public @Nullable Integer getPhysicsBodyId1() {
                return Math.toIntExact(joint.getShipId1());
            }

            @Override
            public @NotNull Pose getLocalFrame1() {
                return new Pose(joint.getPose1().getPos(),joint.getPose1().getRot());
            }

            @Override
            public @Nullable MaxForceTorque getBreakForceTorque() {
               return new MaxForceTorque((joint.getMaxForceTorque()).getMaxForce(),joint.getMaxForceTorque().getMaxTorque());
            }

            @Override
            public @NotNull JointType getConstraintType() {
                return JointType.valueOf(joint.getJointType().name());
            }

            @Override
            public double getCompliance() {
                return joint.getCompliance();
            }
        };


}

}


/*
stuff for core maybe?

way to get jointdata class from joint class

way to get maxforcetorque from VSmaxforcetorque

 */

