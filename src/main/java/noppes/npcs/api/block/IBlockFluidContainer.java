package noppes.npcs.api.block;

public interface IBlockFluidContainer extends IBlock {
	String getFluidName();

	float getFluidPercentage();

	float getFluidValue();

	float getFuildDensity();

	float getFuildTemperature();
}
