package noppes.npcs.util;

public class RayTraceRotate {

	public double yaw, pitch, radiusXZ, distance;

	public RayTraceRotate() {
		this.yaw = 0.0d;
		this.pitch = 0.0d;
		this.radiusXZ = 0.0d;
		this.distance = 0.0d;
	}

	public void calculate(double dx, double dy, double dz, double mx, double my, double mz) {
		double xVal = mx - dx, yVal = my - dy, zVal = mz - dz;
		this.radiusXZ = Math.sqrt(Math.pow(xVal, 2) + Math.pow(zVal, 2));
		this.distance = Math.sqrt(Math.pow(this.radiusXZ, 2) + Math.pow(yVal, 2));

		double rad = 180.0d / Math.PI;
		this.pitch = -Math.atan(yVal / this.radiusXZ) * rad;
		if (this.pitch < -90.0d) {
			this.pitch = -90.0d;
		}
		if (this.pitch > 90.0d) {
			this.pitch = 90.0d;
		}

		if (xVal == 0.0d) {
			this.yaw = dz > mz ? 180.0d : 0.0d;
		} else {
			if (xVal <= 0.0d) {
				this.yaw = 90.0d + Math.atan(zVal / xVal) * rad;
			} else {
				this.yaw = 270.0d + Math.atan(zVal / xVal) * rad;
			}
		}
		this.yaw %= 360.0d;
		if (this.yaw < 0.0d) {
			this.yaw += 360.0d;
		}
	}

}
