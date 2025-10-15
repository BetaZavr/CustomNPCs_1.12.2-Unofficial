package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;

public interface IDropNbtSet {

	double getChance();

	INbt getConstructorTag(@ParamName("nbt") INbt nbt);

	String getPath();

	int getType();

	int getTypeList();

	String[] getValues();

	void remove();

	void setChance(@ParamName("chance") double chance);

	void setPath(@ParamName("path") String path);

	void setType(@ParamName("type") int type);

	void setTypeList(@ParamName("type") int type);

	void setValues(@ParamName("values") String values);

	void setValues(@ParamName("values") String[] values);

}
