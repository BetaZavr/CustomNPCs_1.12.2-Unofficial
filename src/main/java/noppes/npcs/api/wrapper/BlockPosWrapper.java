package noppes.npcs.api.wrapper;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import noppes.npcs.api.IPos;

public class BlockPosWrapper
implements IPos {
	
	private BlockPos blockPos;

	public BlockPosWrapper(BlockPos pos) { this.blockPos = pos; }

	@Override
	public IPos add(int x, int y, int z) {
		return new BlockPosWrapper(this.blockPos.add(x, y, z));
	}

	@Override
	public IPos add(IPos pos) {
		return new BlockPosWrapper(this.blockPos.add((Vec3i) pos.getMCBlockPos()));
	}

	@Override
	public double distanceTo(IPos pos) {
		double d0 = this.getX() - pos.getX();
		double d2 = this.getY() - pos.getY();
		double d3 = this.getZ() - pos.getZ();
		return Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
	}

	@Override
	public IPos down() {
		return new BlockPosWrapper(this.blockPos.down());
	}

	@Override
	public IPos down(int n) {
		return new BlockPosWrapper(this.blockPos.down(n));
	}

	@Override
	public IPos east() {
		return new BlockPosWrapper(this.blockPos.east());
	}

	@Override
	public IPos east(int n) {
		return new BlockPosWrapper(this.blockPos.east(n));
	}

	@Override
	public BlockPos getMCBlockPos() {
		return this.blockPos;
	}

	@Override
	public int getX() {
		return this.blockPos.getX();
	}

	@Override
	public int getY() {
		return this.blockPos.getY();
	}

	@Override
	public int getZ() {
		return this.blockPos.getZ();
	}

	@Override
	public double[] normalize() {
		double d = Math.sqrt(this.blockPos.getX() * this.blockPos.getX() + this.blockPos.getY() * this.blockPos.getY()
				+ this.blockPos.getZ() * this.blockPos.getZ());
		return new double[] { this.getX() / d, this.getY() / d, this.getZ() / d };
	}

	@Override
	public IPos north() {
		return new BlockPosWrapper(this.blockPos.north());
	}

	@Override
	public IPos north(int n) {
		return new BlockPosWrapper(this.blockPos.north(n));
	}

	@Override
	public IPos offset(int direction) {
		return new BlockPosWrapper(this.blockPos.offset(EnumFacing.VALUES[direction]));
	}

	@Override
	public IPos offset(int direction, int n) {
		return new BlockPosWrapper(this.blockPos.offset(EnumFacing.VALUES[direction], n));
	}

	@Override
	public IPos south() {
		return new BlockPosWrapper(this.blockPos.south());
	}

	@Override
	public IPos south(int n) {
		return new BlockPosWrapper(this.blockPos.south(n));
	}

	@Override
	public IPos subtract(int x, int y, int z) {
		return new BlockPosWrapper(this.blockPos.add(-x, -y, -z));
	}

	@Override
	public IPos subtract(IPos pos) {
		return new BlockPosWrapper(this.blockPos.add(-pos.getX(), -pos.getY(), -pos.getZ()));
	}

	@Override
	public IPos up() {
		return new BlockPosWrapper(this.blockPos.up());
	}

	@Override
	public IPos up(int n) {
		return new BlockPosWrapper(this.blockPos.up(n));
	}

	@Override
	public IPos west() {
		return new BlockPosWrapper(this.blockPos.west());
	}

	@Override
	public IPos west(int n) {
		return new BlockPosWrapper(this.blockPos.west(n));
	}
	
}
