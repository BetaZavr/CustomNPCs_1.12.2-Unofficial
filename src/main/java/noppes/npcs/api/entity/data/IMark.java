package noppes.npcs.api.entity.data;

import noppes.npcs.api.handler.data.IAvailability;

public interface IMark {
	IAvailability getAvailability();

	int getColor();

	int getType();

	// New
	boolean isRotate();

	void setColor(int p0);

	void setRotate(boolean rotate);

	void setType(int p0);

	void update();

}
