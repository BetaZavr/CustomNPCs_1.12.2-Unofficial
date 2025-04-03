package noppes.npcs.util;

import noppes.npcs.api.IPos;
import noppes.npcs.api.util.IRayTraceVec;
import noppes.npcs.api.wrapper.BlockPosWrapper;

public class RayTraceVec implements IRayTraceVec {

	public static RayTraceVec EMPTY = new RayTraceVec();

	private double yaw = 0.0d;
	private double distance = 0.0d;
	private double x = 0.0d;
	private double y = 0.0d;
	private double z = 0.0d;
	private double sx = 0.0d;
	private double sy = 0.0d;
	private double sz = 0.0d;

	public void calculatePos(double cx, double cy, double cz, double yaw, double pitch, double distance) {
		sx = cx;
		sy = cy;
		sz = cz;
		if (distance < 0.0d) { distance *= -1.0d; }
		x = cx + Math.sin(yaw * Math.PI / 180) * distance * -1;
		y = cy + Math.sin(pitch * Math.PI / 180) * distance;
		z = cz + Math.cos(yaw * Math.PI / 180) * distance;
		this.distance = distance;
		this.yaw = yaw;
	}

	public void calculateVec(double dx, double dy, double dz, double mx, double my, double mz) {
		sx = dx;
		sy = dy;
		sz = dz;
		double hVal = 1.5d + my - dy, rad0 = Math.PI / 180.0d, rad1 = 180.0d / Math.PI;
		this.distance = Math.sqrt(Math.pow(mx - dx, 2.0d) + Math.pow(my - dy, 2.0d) + Math.pow(mz - dz, 2.0d));
		if (hVal < 0.35d) {
			hVal = 0.35d;
		}
		double xVal = mx - dx, zVal = mz - dz;
		y = (-3.0d + Math.sqrt(9.0d - 16.0d * (-0.75d - hVal))) / 8.0d;
		double radiusXZ = Math.sqrt(Math.pow(xVal, 2.0d) + Math.pow(zVal, 2.0d));
		double impXZ = -4.5d + Math.sqrt(20.25d + 4.0d * radiusXZ);
		if (xVal == 0.0d) {
			yaw = dz > mz ? 180.0d : 0.0d;
		} else {
			double v = Math.atan(zVal / xVal) * rad1;
			if (xVal <= 0.0d) {
				yaw = 90.0d + v;
			} else {
				yaw = 270.0d + v;
			}
		}
		yaw %= 360.0d;
		if (yaw < 0.0d) {
			yaw += 360.0d;
		}
		x = Math.sin(yaw * rad0) * impXZ * -1;
		z = Math.cos(yaw * rad0) * impXZ;
	}

	@Override
	public double getYaw() { return yaw; }

	@Override
	public double getDistance() { return distance; }

	@Override
	public IPos getStartPos() { return new BlockPosWrapper(sx, sy, sz); }

	@Override
	public IPos getEndPos() { return new BlockPosWrapper(x, y, z); }

	@Override
	public double getX() { return x; }

	@Override
	public double getY() { return y; }

	@Override
	public double getZ() { return z; }

}
