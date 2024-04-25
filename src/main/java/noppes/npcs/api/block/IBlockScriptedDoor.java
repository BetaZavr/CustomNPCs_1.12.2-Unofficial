package noppes.npcs.api.block;

import noppes.npcs.api.ITimers;

public interface IBlockScriptedDoor extends IBlock {

	String getBlockModel();

	float getHardness();

	boolean getOpen();

	float getResistance();

	String getSoung(boolean isOpen);

	ITimers getTimers();

	void setBlockModel(String name);

	void setHardness(float hardness);

	void setOpen(boolean open);

	void setResistance(float resistance);

	void setSound(boolean isOpen, String song);

}
