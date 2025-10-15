package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IAvailability;

public interface IMark {

	IAvailability getAvailability();

	int getColor();

	int getType();

    boolean is3D();

    boolean isRotate();

	void set3D(@ParamName("bo") boolean bo);

	void setColor(@ParamName("color") int color);

	void setRotate(@ParamName("rotate") boolean rotate);

	void setType(@ParamName("type") int type);

	void update();

}
