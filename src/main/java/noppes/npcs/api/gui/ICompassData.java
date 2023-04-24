package noppes.npcs.api.gui;

import noppes.npcs.api.IPos;

public interface ICompassData {

	int getDimensionID();

	void setDimensionID(int dimID);

	int getType();
	
	void setType(int type);

	int getRange();

	void setRange(int range);
	
	IPos getPos();
	
	void setPos(IPos pos);
	
	void setPos(int x, int y, int z);
	
	String getName();
	
	void setName(String name);
	
	String getTitle();
	
	void setTitle(String title);

	void setShow(boolean show);

	boolean isShow();

}
