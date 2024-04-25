package noppes.npcs.api.gui;

import noppes.npcs.api.IPos;

public interface ICompassData {

	int getDimensionID();

	String getName();

	String getNPCName();

	IPos getPos();

	int getRange();

	String getTitle();

	int getType();

	boolean isShow();

	void setDimensionID(int dimID);

	void setName(String name);

	void setNPCName(String npcName);

	void setPos(int x, int y, int z);

	void setPos(IPos pos);

	void setRange(int range);

	void setShow(boolean show);

	void setTitle(String title);

	void setType(int type);

}
