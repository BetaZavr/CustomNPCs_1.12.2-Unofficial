package noppes.npcs.api;

import net.minecraft.util.math.BlockPos;

public interface IPos {

	IPos add(double x, double y, double z);

	IPos add(IPos pos);

	double distanceTo(double x, double y, double z);

	double distanceTo(IPos pos);

	IPos down();

	IPos down(double n);

	IPos east();

	IPos east(double n);

	BlockPos getMCBlockPos();

	double getX();

	double getY();

	double getZ();

	double[] normalize();

	IPos north();

	IPos north(double n);

	IPos offset(int direction);

	IPos offset(int direction, double n);

	IPos south();

	IPos south(double n);

	IPos subtract(double x, double y, double z);

	IPos subtract(IPos pos);

	IPos up();

	IPos up(double n);

	IPos west();

	IPos west(double n);

}
