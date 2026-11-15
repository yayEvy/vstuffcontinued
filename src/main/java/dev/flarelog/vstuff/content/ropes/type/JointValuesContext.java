package dev.flarelog.vstuff.content.ropes.type;

import dev.flarelog.vstuff.content.ropes.RopeFactory;

import java.util.Map;

/**
 * @param maxDistance It is spacing * (1 + SAG_FACTOR) where spacing is the gap between the bodies and sag factor is as given in {@link RopeFactory#SAG_FACTOR}
 * @param segments Number of segments in the rope
 * @param sag Value from {@link RopeFactory#SAG_FACTOR}
 */
public record JointValuesContext(float maxDistance, int segments, double sag) {
    public static final JointValuesContext SAMPLE = create(1,  1);

    public static JointValuesContext create(float maxDistance, int segments) {
        return new JointValuesContext(
                maxDistance,
                segments,
                RopeFactory.SAG_FACTOR
        );
    }


    public Map<String, Object> toMap() {
        return Map.of(
                "maxDistance", maxDistance,
                "segments", segments,
                "sag", sag
        );
    }
}