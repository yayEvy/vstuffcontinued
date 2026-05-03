package yay.evy.everest.vstuff.content.ships.thrust;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joml.Vector3d;

public class ThrusterData {
    private volatile float thrust;
    private volatile Vector3d direction;

    public ThrusterData() {
        this.direction = new Vector3d(0, 0, 1);
    }

    @JsonCreator
    public ThrusterData(
            @JsonProperty("thrust") float thrust,
            @JsonProperty("direction") Vector3d direction
    ) {
        this.thrust = thrust;
        this.direction = direction != null ? direction : new Vector3d(0, 0, 1);
    }

    public float getThrust() { return thrust; }
    public void setThrust(float thrust) { this.thrust = thrust; }
    public Vector3d getDirection() { return direction; }
    public void setDirection(Vector3d direction) { this.direction = direction; }
}