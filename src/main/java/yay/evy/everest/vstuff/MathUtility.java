package yay.evy.everest.vstuff;

import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector2f;
import org.joml.Vector3d;

import com.simibubi.create.foundation.collision.Matrix3d;

public class MathUtility {

    public static Matrix3d createMatrixFromQuaternion(Quaterniond quaternion) {
        //I need to do this very ugly thing because create matrix class has its elements private
        double qx = quaternion.x;
        double qy = quaternion.y;
        double qz = quaternion.z;
        double qw = quaternion.w;
        double lengthSq = qx * qx + qy * qy + qz * qz + qw * qw;
        double invLength = 1.0 / Math.sqrt(lengthSq);

        double x = qx * invLength;
        double y = qy * invLength;
        double z = qz * invLength;
        double w = qw * invLength;
        double roll, pitch, yaw;

        // Singularity check
        double sinp = 2.0 * (w * y - z * x);

        if (Math.abs(sinp) > 0.999999) { // Gimbal lock prevention
            pitch = Math.PI / 2.0 * Math.signum(sinp);
            roll = Math.atan2(2.0 * (x * y + w * z), 1.0 - 2.0 * (y * y + z * z));
            yaw = 0.0;

        } else {
            roll = Math.atan2(2.0 * (w * x + y * z), 1.0 - 2.0 * (x * x + y * y));
            pitch = Math.asin(sinp);
            yaw = Math.atan2(2.0 * (w * z + x * y), 1.0 - 2.0 * (y * y + z * z));
        }

        Matrix3d resultMatrix = new Matrix3d();
        Matrix3d tempY = new Matrix3d();
        Matrix3d tempX = new Matrix3d();
        resultMatrix.asZRotation((float) yaw);
        tempY.asYRotation((float) pitch);
        resultMatrix.multiply(tempY);
        tempX.asXRotation((float) roll);
        resultMatrix.multiply(tempX);

        return resultMatrix;
    }

    public static Vector2f toHorizontalCoordinateSystem(Quaterniondc shipRotation) {
        Vector3d worldForwardDirection = new Vector3d();
        Vector3d LOCAL_SHIP_FORWARD_NEGATIVE_Z = new Vector3d(0.0, 0.0, -1.0);
        shipRotation.transform(LOCAL_SHIP_FORWARD_NEGATIVE_Z, worldForwardDirection);

        if (worldForwardDirection.lengthSquared() < 1.0e-12) {
            return new Vector2f(0.0f, 0.0f);
        }

        double horizontalDistance = Math.sqrt(worldForwardDirection.x * worldForwardDirection.x + worldForwardDirection.z * worldForwardDirection.z);

        float yaw;
        if (horizontalDistance < 1.0e-9) {
            yaw = 0.0f;
        } else {
            yaw = (float) Math.toDegrees(Math.atan2(worldForwardDirection.x, -worldForwardDirection.z));
        }

        float pitch;
        if (horizontalDistance < 1.0e-9) {
            if (worldForwardDirection.y > 0.0) {
                pitch = 90.0f;
            } else if (worldForwardDirection.y < 0.0) {
                pitch = -90.0f;
            } else {
                pitch = 0.0f;
            }
        } else {
            pitch = (float) Math.toDegrees(Math.atan2(worldForwardDirection.y, horizontalDistance));
        }

        return new Vector2f(yaw, pitch);
    }

}