package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LevituffForceApplier {

    public double baseStrength = 100000.0;

    public LevituffForceApplier() {}

    public LevituffForceApplier(double baseStrength) {
        this.baseStrength = baseStrength;
    }

    public double getLiftMultiplier(double y) {

        if (y < 0) {
            double t = (y + 64) / 64.0;
            return lerp(1.6, 1.3, t);
        }

        if (y < 100) {
            double t = y / 100.0;
            return lerp(1.3, 1.1, t);
        }

        if (y < 200) {
            double t = (y - 100) / 100.0;
            return lerp(1.1, 0.9, t);
        }

        if (y < 300) {
            double t = (y - 200) / 100.0;
            return lerp(0.9, 0.5, t);
        }

        return 0.3;
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}