package noppes.npcs.util;

public class RayTraceVec {

	public double yaw, radius, x, y, z;

	public RayTraceVec() {
		this.x = 0.0d;
		this.y = 0.0d;
		this.z = 0.0d;
		this.radius = 0.0d;
		this.yaw = 0.0d;
	}

	public RayTraceVec(double x, double y, double z, double yaw) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = 0.0d;
		this.yaw = yaw;
	}

	public void calculatePos(double cx, double cy, double cz, double yaw, double pitch, double radius) {
		if (radius < 0.0d) {
			radius *= -1.0d;
		}
		this.x = cx + Math.sin(yaw * Math.PI / 180) * radius * -1;
		this.y = cy + Math.sin(pitch * Math.PI / 180) * radius;
		this.z = cz + Math.cos(yaw * Math.PI / 180) * radius;
		this.radius = radius;
		this.yaw = yaw;
	}

	public void calculateVec(double dx, double dy, double dz, double mx, double my, double mz) {
		double hVal = 1.5d + my - dy, rad0 = Math.PI / 180.0d, rad1 = 180.0d / Math.PI;
		this.radius = Math.sqrt(Math.pow(mx - dx, 2.0d) + Math.pow(my - dy, 2.0d) + Math.pow(mz - dz, 2.0d));
		if (hVal < 0.35d) {
			hVal = 0.35d;
		}
		double xVal = mx - dx, zVal = mz - dz;
		this.y = (-3.0d + Math.sqrt(9.0d - 16.0d * (-0.75d - hVal))) / 8.0d;

		double radiusXZ = Math.sqrt(Math.pow(xVal, 2.0d) + Math.pow(zVal, 2.0d));
		double impXZ = -4.5d + Math.sqrt(20.25d + 4.0d * radiusXZ);

		if (xVal == 0.0d) {
			this.yaw = dz > mz ? 180.0d : 0.0d;
		} else if (xVal <= 0.0d) {
			this.yaw = 90.0d + Math.atan(zVal / xVal) * rad1;
		} else {
			this.yaw = 270.0d + Math.atan(zVal / xVal) * rad1;
		}
		this.yaw %= 360.0d;
		if (this.yaw < 0.0d) {
			this.yaw += 360.0d;
		}

		this.x = Math.sin(this.yaw * rad0) * impXZ * -1;
		this.z = Math.cos(this.yaw * rad0) * impXZ;
	}

}
