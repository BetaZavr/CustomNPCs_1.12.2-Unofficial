package noppes.npcs.api;

import net.minecraft.util.math.BlockPos;

public interface IPos {
	IPos add(int x, int y, int z);

	IPos add(IPos pos);

	double distanceTo(IPos pos);

	IPos down();

	IPos down(int n);

	IPos east();

	IPos east(int n);

	BlockPos getMCBlockPos();

	int getX();

	int getY();

	int getZ();

	double[] normalize();

	IPos north();

	IPos north(int n);

	IPos offset(int direction);

	IPos offset(int direction, int n);

	IPos south();

	IPos south(int n);

	IPos subtract(int x, int y, int z);

	IPos subtract(IPos pos);

	IPos up();

	IPos up(int n);

	IPos west();

	IPos west(int n);
}
