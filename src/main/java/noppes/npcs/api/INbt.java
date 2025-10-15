package noppes.npcs.api;

import net.minecraft.nbt.NBTTagCompound;

@SuppressWarnings("all")
public interface INbt {

	void clear();

	boolean getBoolean(@ParamName("key") String key);

	byte getByte(@ParamName("key") String key);

	byte[] getByteArray(@ParamName("key") String key);

	INbt getCompound(@ParamName("key") String key);

	double getDouble(@ParamName("key") String key);

	float getFloat(@ParamName("key") String key);

	int getInteger(@ParamName("key") String key);

	int[] getIntegerArray(@ParamName("key") String key);

	String[] getKeys();

	Object[] getList(@ParamName("key") String key, @ParamName("type") int type);

	int getListType(@ParamName("key") String key);

	long getLong(@ParamName("key") String key);

	NBTTagCompound getMCNBT();

	short getShort(@ParamName("key") String key);

	String getString(@ParamName("key") String key);

	int getType(@ParamName("key") String key);

	boolean has(@ParamName("key") String key);

	boolean isEqual(@ParamName("nbt") INbt nbt);

	void merge(@ParamName("nbt") INbt nbt);

	void remove(@ParamName("key") String key);

	void setBoolean(@ParamName("key") String key, @ParamName("value") boolean value);

	void setByte(@ParamName("key") String key, @ParamName("value") byte value);

	void setByteArray(@ParamName("key") String key, @ParamName("value") byte[] value);

	void setCompound(@ParamName("key") String key, @ParamName("value") INbt value);

	void setDouble(@ParamName("key") String key, @ParamName("value") double value);

	void setFloat(@ParamName("key") String key, @ParamName("value") float value);

	void setInteger(@ParamName("key") String key, @ParamName("value") int value);

	void setIntegerArray(@ParamName("key") String key, @ParamName("value") int[] value);

	void setList(@ParamName("key") String key, @ParamName("value") Object[] value);

	void setLong(@ParamName("key") String key, @ParamName("value") long value);

	void setShort(@ParamName("key") String key, @ParamName("value") short value);

	void setString(@ParamName("key") String key, @ParamName("value") String value);

	String toJsonString();

}
