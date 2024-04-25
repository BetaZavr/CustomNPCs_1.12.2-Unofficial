package noppes.npcs.schematics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

public interface ISchematic {

	IBlockState getBlockState(int state);

	IBlockState getBlockState(int x, int y, int z);

	NBTTagList getEntitys();

	short getHeight();

	short getLength();

	String getName();

	NBTTagCompound getNBT();

	BlockPos getOffset();

	NBTTagCompound getTileEntity(int pos);

	int getTileEntitySize();

	short getWidth();

	boolean hasEntitys();

}
