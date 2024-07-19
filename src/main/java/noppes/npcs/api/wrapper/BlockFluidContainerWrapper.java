package noppes.npcs.api.wrapper;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import noppes.npcs.api.block.IBlockFluidContainer;

public class BlockFluidContainerWrapper extends BlockWrapper implements IBlockFluidContainer {

	private final BlockFluidBase block;

	public BlockFluidContainerWrapper(World world, Block block, BlockPos pos) {
		super(world, block, pos);
		this.block = (BlockFluidBase) block;
	}

	@Override
	public String getFluidName() {
		return this.block.getFluid().getName();
	}

	@Override
	public float getFluidPercentage() {
		return this.block.getFilledPercentage(this.world.getMCWorld(), this.pos);
	}

	@Override
	public float getFluidValue() {
		return this.block.getQuantaValue(this.world.getMCWorld(), this.pos);
	}

	@Override
	public float getFluidDensity() {
		return BlockFluidBase.getDensity(this.world.getMCWorld(), this.pos);
	}

	@Override
	public float getFluidTemperature() {
		return BlockFluidBase.getTemperature(this.world.getMCWorld(), this.pos);
	}
}
