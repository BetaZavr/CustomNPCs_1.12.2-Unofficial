package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;

public interface IDropNbtSet {

	double getChance();

	INbt getConstructorTag(INbt nbt);

	String getPath();

	int getType();

	int getTypeList();

	String[] getValues();

	void remove();

	void setChance(double chance);

	void setPath(String path);

	void setType(int type);

	void setTypeList(int type);

	void setValues(String values);

	void setValues(String[] values);

}
