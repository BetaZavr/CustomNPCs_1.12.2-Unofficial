package noppes.npcs.schematics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

public interface ISchematic {
	IBlockState getBlockState(int p0);

	IBlockState getBlockState(int p0, int p1, int p2);

	short getHeight();

	short getLength();

	String getName();

	NBTTagCompound getNBT();

	NBTTagCompound getTileEntity(int p0);

	int getTileEntitySize();

	short getWidth();
}
