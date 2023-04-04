package noppes.npcs.api.entity.data;

import noppes.npcs.api.handler.data.IAvailability;

public interface IMark {
	
	IAvailability getAvailability();

	int getColor();

	int getType();

	// New
	boolean isRotate();

	void setColor(int color);

	void setRotate(boolean rotate);

	void setType(int type);

	void update();

}
