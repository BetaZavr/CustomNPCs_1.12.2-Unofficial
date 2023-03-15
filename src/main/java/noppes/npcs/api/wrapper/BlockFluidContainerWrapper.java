package noppes.npcs.api.wrapper;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import noppes.npcs.api.block.IBlockFluidContainer;

public class BlockFluidContainerWrapper
extends BlockWrapper
implements IBlockFluidContainer {
	
	private BlockFluidBase block;

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
		return this.block.getQuantaValue((IBlockAccess) this.world.getMCWorld(), this.pos);
	}

	@Override
	public float getFuildDensity() {
		return BlockFluidBase.getDensity((IBlockAccess) this.world.getMCWorld(), this.pos);
	}

	@Override
	public float getFuildTemperature() {
		return BlockFluidBase.getTemperature((IBlockAccess) this.world.getMCWorld(), this.pos);
	}
}
