package noppes.npcs.api.entity.data;

import net.minecraft.nbt.NBTTagCompound;

public interface IDropNbtSet {

	double getChance();

	NBTTagCompound getConstructoredTag(NBTTagCompound nbt);

	String getPath();

	int getType();

	int getTypeList();

	String[] getValues();

	void remove();

	void setChance(double chance);

	void setPath(String paht);

	void setType(int type);

	void setTypeList(int type);

	void setValues(String values);

	void setValues(String[] values);

}
