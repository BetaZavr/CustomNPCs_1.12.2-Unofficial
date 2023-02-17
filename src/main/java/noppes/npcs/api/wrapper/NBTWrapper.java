package noppes.npcs.api.wrapper;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.util.NBTJsonUtil;

public class NBTWrapper implements INbt {
	private NBTTagCompound compound;

	public NBTWrapper(NBTTagCompound compound) {
		this.compound = compound;
	}

	@Override
	public void clear() {
		for (String name : this.compound.getKeySet()) {
			this.compound.removeTag(name);
		}
	}

	@Override
	public boolean getBoolean(String key) {
		return this.compound.getBoolean(key);
	}

	@Override
	public byte getByte(String key) {
		return this.compound.getByte(key);
	}

	@Override
	public byte[] getByteArray(String key) {
		return this.compound.getByteArray(key);
	}

	@Override
	public INbt getCompound(String key) {
		return NpcAPI.Instance().getINbt(this.compound.getCompoundTag(key));
	}

	@Override
	public double getDouble(String key) {
		return this.compound.getDouble(key);
	}

	@Override
	public float getFloat(String key) {
		return this.compound.getFloat(key);
	}

	@Override
	public int getInteger(String key) {
		return this.compound.getInteger(key);
	}

	@Override
	public int[] getIntegerArray(String key) {
		return this.compound.getIntArray(key);
	}

	@Override
	public String[] getKeys() {
		return this.compound.getKeySet().toArray(new String[this.compound.getKeySet().size()]);
	}

	@Override
	public Object[] getList(String key, int type) {
		NBTTagList list = this.compound.getTagList(key, type);
		Object[] nbts = new Object[list.tagCount()];
		for (int i = 0; i < list.tagCount(); ++i) {
			if (list.getTagType() == 10) {
				nbts[i] = NpcAPI.Instance().getINbt(list.getCompoundTagAt(i));
			} else if (list.getTagType() == 8) {
				nbts[i] = list.getStringTagAt(i);
			} else if (list.getTagType() == 6) {
				nbts[i] = list.getDoubleAt(i);
			} else if (list.getTagType() == 5) {
				nbts[i] = list.getFloatAt(i);
			} else if (list.getTagType() == 3) {
				nbts[i] = list.getIntAt(i);
			} else if (list.getTagType() == 11) {
				nbts[i] = list.getIntArrayAt(i);
			}
		}
		return nbts;
	}

	@Override
	public int getListType(String key) {
		NBTBase b = this.compound.getTag(key);
		if (b == null) {
			return 0;
		}
		if (b.getId() != 9) {
			throw new CustomNPCsException("NBT tag " + key + " isn't a list", new Object[0]);
		}
		return ((NBTTagList) b).getTagType();
	}

	@Override
	public long getLong(String key) {
		return this.compound.getLong(key);
	}

	@Override
	public NBTTagCompound getMCNBT() {
		return this.compound;
	}

	@Override
	public short getShort(String key) {
		return this.compound.getShort(key);
	}

	@Override
	public String getString(String key) {
		return this.compound.getString(key);
	}

	@Override
	public int getType(String key) {
		return this.compound.getTagId(key);
	}

	@Override
	public boolean has(String key) {
		return this.compound.hasKey(key);
	}

	@Override
	public boolean isEqual(INbt nbt) {
		return nbt != null && this.compound.equals(nbt.getMCNBT());
	}

	@Override
	public void merge(INbt nbt) {
		this.compound.merge(nbt.getMCNBT());
	}

	@Override
	public void remove(String key) {
		this.compound.removeTag(key);
	}

	@Override
	public void setBoolean(String key, boolean value) {
		this.compound.setBoolean(key, value);
	}

	@Override
	public void setByte(String key, byte value) {
		this.compound.setByte(key, value);
	}

	@Override
	public void setByteArray(String key, byte[] value) {
		this.compound.setByteArray(key, value);
	}

	@Override
	public void setCompound(String key, INbt value) {
		if (value == null) {
			throw new CustomNPCsException("Value cant be null", new Object[0]);
		}
		this.compound.setTag(key, value.getMCNBT());
	}

	@Override
	public void setDouble(String key, double value) {
		this.compound.setDouble(key, value);
	}

	@Override
	public void setFloat(String key, float value) {
		this.compound.setFloat(key, value);
	}

	@Override
	public void setInteger(String key, int value) {
		this.compound.setInteger(key, value);
	}

	@Override
	public void setIntegerArray(String key, int[] value) {
		this.compound.setIntArray(key, value);
	}

	@Override
	public void setList(String key, Object[] value) {
		NBTTagList list = new NBTTagList();
		for (Object nbt : value) {
			if (nbt instanceof INbt) {
				list.appendTag(((INbt) nbt).getMCNBT());
			} else if (nbt instanceof String) {
				list.appendTag(new NBTTagString((String) nbt));
			} else if (nbt instanceof Double) {
				list.appendTag(new NBTTagDouble((double) nbt));
			} else if (nbt instanceof Float) {
				list.appendTag(new NBTTagFloat((float) nbt));
			} else if (nbt instanceof Integer) {
				list.appendTag(new NBTTagInt((int) nbt));
			} else if (nbt instanceof int[]) {
				list.appendTag(new NBTTagIntArray((int[]) nbt));
			}
		}
		this.compound.setTag(key, list);
	}

	@Override
	public void setLong(String key, long value) {
		this.compound.setLong(key, value);
	}

	@Override
	public void setShort(String key, short value) {
		this.compound.setShort(key, value);
	}

	@Override
	public void setString(String key, String value) {
		this.compound.setString(key, value);
	}

	@Override
	public String toJsonString() {
		return NBTJsonUtil.Convert(this.compound);
	}
}
