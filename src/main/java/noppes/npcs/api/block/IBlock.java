package noppes.npcs.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.data.IData;

public interface IBlock {
	
	void blockEvent(int type, int data);

	IContainer getContainer();

	String getDisplayName();

	Block getMCBlock();

	IBlockState getMCBlockState();

	TileEntity getMCTileEntity();

	int getMetadata();

	String getName();

	IPos getPos();

	IData getStoreddata();

	IData getTempdata();

	INbt getTileEntityNBT();

	IWorld getWorld();

	int getX();

	int getY();

	int getZ();

	boolean hasTileEntity();

	void interact(int side);

	boolean isAir();

	boolean isContainer();

	boolean isRemoved();

	void remove();

	IBlock setBlock(IBlock block);

	IBlock setBlock(String name);

	void setMetadata(int i);

	void setTileEntityNBT(INbt nbt);

}
