package yay.evy.everest.vstuff;

import net.minecraftforge.common.ForgeConfigSpec;

public class VstuffConfig {

    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;

    // Server-side values
    public static final ForgeConfigSpec.ConfigValue<Double> THRUSTER_THRUST_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> THRUSTER_CONSUMPTION_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Integer> THRUSTER_MAX_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Integer> THRUSTER_TICKS_PER_UPDATE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> THRUSTER_DAMAGE_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<Double> THRUSTER_PARTICLE_OFFSET_INCOMING_VEL_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Double> THRUSTER_PARTICLE_COUNT_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> MAX_ROPE_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<Double> PULLEY_SPEED;
    public static final ForgeConfigSpec.BooleanValue ROPE_SOUNDS;

    static {
        // Server builder
        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();

        serverBuilder.push("thruster");
        THRUSTER_THRUST_MULTIPLIER = serverBuilder
                .comment("Multiplier for thruster thrust (default: 1.0)")
                .define("thrustMultiplier", 1.0);
        THRUSTER_CONSUMPTION_MULTIPLIER = serverBuilder
                .comment("Multiplier for thruster energy/fuel consumption (default: 1.0)")
                .define("consumptionMultiplier", 1.0);
        THRUSTER_MAX_SPEED = serverBuilder
                .comment("Maximum speed thrusters can push to (default: 20)")
                .define("maxSpeed", 20);
        THRUSTER_TICKS_PER_UPDATE = serverBuilder
                .comment("How many ticks between thruster updates (default: 1)")
                .define("ticksPerUpdate", 1);
        THRUSTER_DAMAGE_ENTITIES = serverBuilder
                .comment("Whether thrusters damage entities in front of them (default: true)")
                .define("damageEntities", true);
        THRUSTER_PARTICLE_OFFSET_INCOMING_VEL_MODIFIER = serverBuilder
                .comment("Modifier for particle offset by incoming velocity (default: 1.0)")
                .define("particleOffsetIncomingVelModifier", 1.0);
        THRUSTER_PARTICLE_COUNT_MULTIPLIER = serverBuilder
                .comment("Multiplier for thruster particle count (default: 1.0)")
                .define("particleCountMultiplier", 1.0);
        ROPE_SOUNDS = serverBuilder
                .comment("Toggle on or off client rope sounds (default: true)")
                .define("ropeSounds", true);
        MAX_ROPE_LENGTH = serverBuilder
                .comment("Set the max length for ropes (default: 200)")
                .define("max_rope_length", 200.0);
        PULLEY_SPEED = serverBuilder
                .comment("Set the pulley extension/retraction speed (default: 0.1)")
                .define("pulley_speed", 0.1);
        serverBuilder.pop();
// woooooooo
        SERVER_CONFIG = serverBuilder.build();

        // Client builder (empty for now)
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CLIENT_CONFIG = clientBuilder.build();
    }
}
