package yay.evy.everest.vstuff;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class VstuffConfig {

    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.ConfigValue<Double> THRUSTER_THRUST_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Integer> THRUSTER_MAX_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Integer> THRUSTER_TICKS_PER_UPDATE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> THRUSTER_DAMAGE_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<Double> THRUSTER_PARTICLE_OFFSET_INCOMING_VEL_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Double> THRUSTER_PARTICLE_COUNT_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Double> MAX_ROPE_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<Double> ROPE_THICKNESS;
    public static final ForgeConfigSpec.BooleanValue ROPE_SOUNDS;
    public static final ForgeConfigSpec.BooleanValue ROPE_ONLY_FULL_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ROPE_BLACKLIST;

    public static final ForgeConfigSpec.ConfigValue<Double> PHYS_GRABBER_MAX_MASS;



    public static final ForgeConfigSpec.ConfigValue<Double> PULLEY_SPEED;

    static {

        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();

        serverBuilder.push("thruster");
        THRUSTER_THRUST_MULTIPLIER = serverBuilder.define("thrustMultiplier", 0.5);
        THRUSTER_MAX_SPEED = serverBuilder.define("maxSpeed", 20);
        THRUSTER_TICKS_PER_UPDATE = serverBuilder.define("ticksPerUpdate", 1);
        THRUSTER_DAMAGE_ENTITIES = serverBuilder.define("damageEntities", true);
        THRUSTER_PARTICLE_OFFSET_INCOMING_VEL_MODIFIER = serverBuilder.define("particleOffsetIncomingVelModifier", 1.0);
        THRUSTER_PARTICLE_COUNT_MULTIPLIER = serverBuilder.define("particleCountMultiplier", 1.0);
        serverBuilder.pop();

        serverBuilder.push("rope");
        MAX_ROPE_LENGTH = serverBuilder.define("max_rope_length", 200.0);
        ROPE_ONLY_FULL_BLOCKS = serverBuilder.define("ropeOnlyFullBlocks", true);
        serverBuilder.pop();

        serverBuilder.push("pulley");
        PULLEY_SPEED = serverBuilder.define("pulley_speed", 0.1);
        serverBuilder.pop();

        serverBuilder.push("phys grabber");
        PHYS_GRABBER_MAX_MASS = serverBuilder
                .comment("The maximum weight the Phys Grabber can grab. (default: 89120.0)")
                .define("grabber_max_mass", 89120.0);
        serverBuilder.pop();

        List<String> blacklist = List.of(new String[]{
                "minecraft:vines",
                "minecraft:bedrock",
                "minecraft:end_portal",
                "minecraft:end_gateway",
                "minecraft:air"
        });

        ROPE_BLACKLIST = serverBuilder
                .comment("List of blocks that ropes cannot connect to.")
                .defineList("rope_blacklist", blacklist, block -> true);

        SERVER_CONFIG = serverBuilder.build();


        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

        clientBuilder.push("rope_client");
        ROPE_THICKNESS = clientBuilder.defineInRange("thickness", 0.28, 0.01, 1.0);
        ROPE_SOUNDS = clientBuilder.define("ropeSounds", true);
        clientBuilder.pop();

        CLIENT_CONFIG = clientBuilder.build();
    }
}
