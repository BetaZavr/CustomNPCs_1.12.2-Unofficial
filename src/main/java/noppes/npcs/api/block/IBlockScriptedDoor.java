package noppes.npcs.api.block;

import noppes.npcs.api.ITimers;

public interface IBlockScriptedDoor extends IBlock {
	String getBlockModel();

	float getHardness();

	boolean getOpen();

	float getResistance();

	ITimers getTimers();

	void setBlockModel(String p0);

	void setHardness(float p0);

	void setOpen(boolean p0);

	void setResistance(float p0);
}
