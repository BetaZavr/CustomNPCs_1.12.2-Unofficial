package noppes.npcs.api;

import net.minecraft.nbt.NBTTagCompound;

public interface INbt {

	void clear();

	boolean getBoolean(String key);

	byte getByte(String key);

	byte[] getByteArray(String key);

	INbt getCompound(String key);

	double getDouble(String key);

	float getFloat(String key);

	int getInteger(String key);

	int[] getIntegerArray(String key);

	String[] getKeys();

	Object[] getList(String key, int type);

	int getListType(String key);

	long getLong(String key);

	NBTTagCompound getMCNBT();

	short getShort(String key);

	String getString(String key);

	int getType(String key);

	boolean has(String key);

	boolean isEqual(INbt nbt);

	void merge(INbt nbt);

	void remove(String key);

	void setBoolean(String key, boolean value);

	void setByte(String key, byte value);

	void setByteArray(String key, byte[] value);

	void setCompound(String key, INbt value);

	void setDouble(String key, double value);

	void setFloat(String key, float value);

	void setInteger(String key, int value);

	void setIntegerArray(String key, int[] value);

	void setList(String key, Object[] value);

	void setLong(String key, long value);

	void setShort(String key, short value);

	void setString(String key, String value);

	String toJsonString();
}
