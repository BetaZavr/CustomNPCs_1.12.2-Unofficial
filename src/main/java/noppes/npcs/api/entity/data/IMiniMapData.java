package noppes.npcs.api.entity.data;

import noppes.npcs.api.IPos;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IMiniMapData {

	int getColor();

	int[] getDimensions();

	String getIcon();

	int getId();

	String getName();

	IPos getPos();

	String[] getSpecificKeys();

	String getSpecificValue(@ParamName("key") String key);

	String getType();

	boolean isEnable();

	void setColor(@ParamName("color") int color);

	void setDimensions(@ParamName("dims") int[] dims);

	void setIcon(@ParamName("icon") String icon);

	void setName(@ParamName("name") String name);

	void setPos(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void setPos(@ParamName("pos") IPos pos);

	void setType(@ParamName("type") String type);

}
