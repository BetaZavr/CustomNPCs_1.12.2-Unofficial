package noppes.npcs.api.entity.data;

import noppes.npcs.api.IPos;

public interface IMiniMapData {

	int getColor();

	int[] getDimentions();

	String getIcon();

	int getId();

	String getName();

	IPos getPos();

	String[] getSpecificKeys();

	String getSpecificValue(String key);

	String getType();

	boolean isEnable();

	void setColor(int color);

	void setDimentions(int[] dims);

	void setIcon(String icon);

	void setName(String name);

	void setPos(int x, int y, int z);

	void setPos(IPos pos);

	void setType(String type);

}
