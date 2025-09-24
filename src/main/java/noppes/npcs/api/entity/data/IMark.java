package noppes.npcs.api.entity.data;

import noppes.npcs.api.handler.data.IAvailability;

public interface IMark {

	IAvailability getAvailability();

	int getColor();

	int getType();

    boolean is3D();

    boolean isRotate();

	void set3D(boolean bo);

	void setColor(int color);

	void setRotate(boolean rotate);

	void setType(int type);

	void update();

}
