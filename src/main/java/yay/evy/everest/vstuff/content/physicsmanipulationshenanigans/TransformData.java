package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record TransformData(Vector3f offset, Vector3f rotation) {

    public @NotNull String toString() {
        return "TransformData with offset=" + this.offset + ", rotation=" + this.rotation;
    }

}
