package noppes.npcs.api.block;

import noppes.npcs.api.ITimers;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IBlockScriptedDoor extends IBlock {

	String getBlockModel();

	float getHardness();

	boolean getOpen();

	float getResistance();

	String getSound(@ParamName("isOpen") boolean isOpen);

	ITimers getTimers();

	void setBlockModel(@ParamName("name") String name);

	void setHardness(@ParamName("hardness") float hardness);

	void setOpen(@ParamName("open") boolean open);

	void setResistance(@ParamName("resistance") float resistance);

	void setSound(@ParamName("isOpen") boolean isOpen, @ParamName("song") String song);

}
