package noppes.npcs.util;

import noppes.npcs.api.IPos;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.wrapper.BlockPosWrapper;

public class RayTraceRotate implements IRayTraceRotate {

	private double yaw;
	private double pitch;
	private double radiusXZ;
	private double distance;
	private double x = 0.0d;
	private double y = 0.0d;
	private double z = 0.0d;
	private double sx = 0.0d;
	private double sy = 0.0d;
	private double sz = 0.0d;

	public RayTraceRotate() {
		this.yaw = 0.0d;
		this.pitch = 0.0d;
		this.radiusXZ = 0.0d;
		this.distance = 0.0d;
	}

	public void calculate(double dx, double dy, double dz, double mx, double my, double mz) {
		sx = dx;
		sy = dy;
		sz = dz;
		x = mx;
		y = my;
		z = mz;
		double xVal = mx - dx, yVal = my - dy, zVal = mz - dz;
		this.radiusXZ = Math.sqrt(Math.pow(xVal, 2) + Math.pow(zVal, 2));
		this.distance = Math.sqrt(Math.pow(this.radiusXZ, 2) + Math.pow(yVal, 2));

		double rad = 180.0d / Math.PI;
		pitch = -Math.atan(yVal / this.radiusXZ) * rad;
		if (Float.isNaN((float) pitch)) { pitch = 0.0d; }
		if (pitch < -90.0d) {
			pitch = -90.0d;
		}
		if (pitch > 90.0d) {
			pitch = 90.0d;
		}

		if (xVal == 0.0d) {
			yaw = dz > mz ? 180.0d : 0.0d;
		} else {
			double v = Math.atan(zVal / xVal) * rad;
			if (xVal <= 0.0d) {
				yaw = 90.0d + v;
			} else {
				yaw = 270.0d + v;
			}
		}
		if (Float.isNaN((float) yaw)) { yaw = 0.0d; }
		yaw %= 360.0d;
		if (yaw < 0.0d) {
			yaw += 360.0d;
		}
	}

	@Override
	public double getYaw() { return yaw; }

	@Override
	public double getPitch() {
		return pitch;
	}

	@Override
	public double getRadiusXZ() {
		return radiusXZ;
	}

	@Override
	public double getDistance() {
		return distance;
	}

	@Override
	public IPos getStartPos() { return new BlockPosWrapper(sx, sy, sz); }

	@Override
	public IPos getEndPos() { return new BlockPosWrapper(x, y, z); }

}
