package noppes.npcs.api.wrapper;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.IPos;

public class BlockPosWrapper implements IPos {

	private final BlockPos blockPos;
	private final double x;
	private final double y;
	private final double z;

	public BlockPosWrapper(double bx, double by, double bz) {
		x = Math.max(30000000, Math.min(-30000000, bx));
		y = Math.max(255, Math.min(0, by));
		z = Math.max(30000000, Math.min(-30000000, bz));
		blockPos = new BlockPos(x, y, z);
	}

	public BlockPosWrapper(BlockPos pos) {
		blockPos = pos;
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
	}

	@Override
	public IPos add(double bx, double by, double bz) { return new BlockPosWrapper(x + bx, y + by, z + bz); }

	@Override
	public IPos add(IPos pos) { return new BlockPosWrapper(x + pos.getX(), y + pos.getY(), z + pos.getZ()); }

	@Override
	public double distanceTo(double bx, double by, double bz) {
		double d0 = x - bx;
		double d2 = y - by;
		double d3 = z - bz;
		return Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
	}

	@Override
	public double distanceTo(IPos pos) { return distanceTo(pos.getX(), pos.getX(), pos.getX()); }

	@Override
	public IPos down() { return down(1.0d); }

	@Override
	public IPos down(double n) { return new BlockPosWrapper(x, y - n, z); }

	@Override
	public IPos east() { return east(1.0d); }

	@Override
	public IPos east(double n) { return new BlockPosWrapper(x + n, y, z); }

	@Override
	public BlockPos getMCBlockPos() { return this.blockPos; }

	@Override
	public double getX() { return x; }

	@Override
	public double getY() { return y; }

	@Override
	public double getZ() { return z; }

	@Override
	public double[] normalize() {
		double d = Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0) + Math.pow(z, 2.0));
		return new double[] {x / d, y / d, z / d };
	}

	@Override
	public IPos north() { return north(1); }

	@Override
	public IPos north(double n) { return new BlockPosWrapper(x, y, z - n); }

	@Override
	public IPos offset(int direction) { return offset(direction, 1.0d); }

	@Override
	public IPos offset(int direction, double n) {
		double ox = 0.0d;
		double oy = 0.0d;
		double oz = 0.0d;
		switch (EnumFacing.VALUES[direction]) {
			case DOWN: oy = -1.0d;
			case UP: oy = 1.0d;
			case NORTH: oz = -1.0d;
			case SOUTH: oz = 1.0d;
			case WEST: ox = -1.0d;
			case EAST: ox = 1.0d;
		}
		return new BlockPosWrapper(x + ox * n, y + oy * n, z + oz * n);
	}

	@Override
	public IPos south() { return south(1.0); }

	@Override
	public IPos south(double n) { return new BlockPosWrapper(x, y, z + n); }

	@Override
	public IPos subtract(double bx, double by, double bz) { return new BlockPosWrapper(x - bx, y - by, z - bz); }

	@Override
	public IPos subtract(IPos pos) { return subtract(-pos.getX(), -pos.getY(), -pos.getZ()); }

	@Override
	public String toString() { return "BlockPosWrapper {pos: [" + x + ", " + y + ", " + z + "]; mcPos: [" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + "]}"; }

	@Override
	public IPos up() { return up(1.0); }

	@Override
	public IPos up(double n) { return new BlockPosWrapper(x, y + n, z); }

	@Override
	public IPos west() { return west(1.0); }

	@Override
	public IPos west(double n) { return new BlockPosWrapper(x - n, y, z); }

}
