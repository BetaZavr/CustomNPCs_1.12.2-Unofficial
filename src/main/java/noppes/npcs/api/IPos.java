package noppes.npcs.api;

import net.minecraft.util.math.BlockPos;

public interface IPos {

	IPos add(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	IPos add(@ParamName("pos") IPos pos);

	double distanceTo(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	double distanceTo(@ParamName("pos") IPos pos);

	IPos down();

	IPos down(@ParamName("n") double n);

	IPos east();

	IPos east(@ParamName("n") double n);

	BlockPos getMCBlockPos();

	double getX();

	double getY();

	double getZ();

	double[] normalize();

	IPos north();

	IPos north(@ParamName("n") double n);

	IPos offset(@ParamName("direction") int direction);

	IPos offset(@ParamName("direction") int direction, @ParamName("n") double n);

	IPos south();

	IPos south(@ParamName("n") double n);

	IPos subtract(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	IPos subtract(@ParamName("pos") IPos pos);

	IPos up();

	IPos up(@ParamName("n") double n);

	IPos west();

	IPos west(@ParamName("n") double n);

}
