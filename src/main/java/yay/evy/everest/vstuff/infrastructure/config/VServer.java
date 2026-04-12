package yay.evy.everest.vstuff.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.impl.shadow.Co;

import java.util.List;

public class VServer extends ConfigBase {

    public final ConfigGroup server = group(0, "server", Comments.server);

    public final VStress stress = nested(0, VStress::new, Comments.stress);

    public final ConfigGroup ropes = group(1, "ropes", Comments.ropes);
    public final ConfigFloat ropeMaxLength = f(32, 8, 256, "ropeMaxLength", Comments.inBlocks, Comments.ropeMaxLength);
    public final ConfigBool ropeSounds = b(true, "ropeSounds", Comments.ropeSounds);

    public final ConfigFloat physRopeSegmentLength = f(1f, 0.5f, 16f, "physRopeSegmentLength", Comments.physRopeSegmentLength);
    public final ConfigFloat physRopeCapsuleRadius = f(0.1f, 0.01f, 1.0f, "physRopeCapsuleRadius", Comments.inBlocks, Comments.physRopeCapsuleRadius);
    public final ConfigFloat physRopeMassPerSegment = f(10.0f, 0.1f, 10000.0f, "physRopeMassPerSegment", Comments.physRopeMassPerSegment);

    public final ConfigGroup thruster = group(1, "thruster", Comments.thruster);
    public final ConfigFloat thrustMultiplier = f(1, 0, "thrusterMultiplier", Comments.thrusterMultiplier);
    public final ConfigInt thrusterMaxSpeed = i(10, 0, "thrusterMaxSpeed", "[in Blocks per second]", Comments.thrusterMaxSpeed);
    public final ConfigFloat thrusterMaxPushDistance = f(32, 0, 256, "thrusterMaxPushDistance", Comments.inBlocks, Comments.thrusterMaxPushDistance);

    public final ConfigGroup physPulley = group(1, "physPulley", Comments.physPulley);
    //public final ConfigFloat pulleySpeed = f(0.5f, 0.1f, 64, "pulleySpeed", "[in Blocks per second per RPM]", Comments.pulleySpeed);

    public final ConfigGroup physGrabber = group(1, "physGrabber", Comments.physGrabber);
    public final ConfigFloat physGrabberMaxMass = f(500000, 0, "physGrabberMaxMass", Comments.physGrabberMaxMass);

    public final ConfigGroup reactionWheel = group(1, "reactionWheel", Comments.reactionWheel);
    public final ConfigFloat reactionWheelMaxSpeed = f(256, 1, 256, "reactionWheelMaxSpeed", Comments.inRpm, Comments.reactionWheelMaxSpeed);
    public final ConfigFloat reactionWheelMaxTorque = f(50000, 0, 100000, "reactionWheelMaxTorque", Comments.reactionWheelMaxTorque);

    public final ConfigGroup levituff = group(1, "levituff");
    public final ConfigFloat levituffStrengthMultiplier = f(3, 0, 64, "levituffStrengthMultiplier");
    public final ConfigFloat levituffForceDamping = f(0.15f, 0, 1, "levituffForceDamping");

    @Override
    public @NotNull String getName() {
        return "server";
    }

    private static class Comments {
        static String inBlocks = "[in Blocks]";
        static String inRpm = "[in RPM]";
        static String server = "VStuff server config, most values are here.";
        static String stress = "Stress impacts of VStuff's blocks";

        static String ropes = "Values for ropes";
        static String ropeMaxLength = "The maximum length of a rope";
        static String ropeSounds = "Whether ropes make sounds when created";

        // phys ropes
        static String physRopeSegmentLength = "length of phys segments in a phys rope";
        static String physRopeCapsuleRadius = "Radius of each phys rope capsule collider";
        static String physRopeMassPerSegment = "Mass of each phys rope segment";

        static String thruster = "Values for the Mechanical Thruster";
        static String thrusterMultiplier = "The multiplier for the amount of thrust produced";
        static String thrusterMaxSpeed = "The maximum speed of the thruster";
        static String thrusterMaxPushDistance = "The maximum distance the air current from the thruster can push or pull.";

        static String physPulley = "Values for the Phys Pulley";
        static String pulleySpeed = "How fast the pulley retracts/extends";

        static String physGrabber = "Values for the Phys Grabber";
        static String physGrabberMaxMass = "The maximum weight the Phys Grabber can grab. Set to 0 to make the grabber ignore mass.";

        static String reactionWheel = "Values for the Reaction Wheel";
        static String reactionWheelMaxSpeed = "Maximum RPM the Reaction Wheel scales torque against";
        static String reactionWheelMaxTorque = "Maximum torque applied by Reaction Wheel at max RPM";
    }
}
