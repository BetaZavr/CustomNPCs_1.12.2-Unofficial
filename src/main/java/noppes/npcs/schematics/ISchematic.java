package noppes.npcs.schematics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

public interface ISchematic {
	
	IBlockState getBlockState(int state);

	IBlockState getBlockState(int x, int y, int z);

	short getHeight();

	short getLength();

	String getName();

	NBTTagCompound getNBT();

	NBTTagCompound getTileEntity(int pos);

	int getTileEntitySize();

	short getWidth();
	
}
