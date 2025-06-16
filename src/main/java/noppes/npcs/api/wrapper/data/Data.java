package noppes.npcs.api.wrapper.data;

import java.util.*;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.util.Util;

public class Data implements IData {

	protected final Map<String, Object> map = new TreeMap<>();

	public Data() { }

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Object get(String key) {
		return map.get(key);
	}

	@Override
	public String[] getKeys() {
		Set<String> sets = map.keySet();
		return sets.toArray(new String[0]);
	}

	@Override
	public INbt getNbt() {
		NBTBase tag = Util.instance.writeObjectToNbt(map);
		if (tag instanceof NBTTagCompound) { return new NBTWrapper((NBTTagCompound) tag); }
		return new NBTWrapper(new NBTTagCompound());
	}

	@Override
	public boolean has(String key) {
		return map.containsKey(key);
	}

	@Override
	public void put(String key, Object value) {
		if (value == null) {
			remove(key);
			return;
		}
		map.put(key, value);
	}

	@Override
	public void remove(String key) {
		map.remove(key);
	}

	@SuppressWarnings("unchecked")
	public void setNbt(NBTTagCompound compound) {
		Object obj = Util.instance.readObjectFromNbt(compound);
		if (obj instanceof TreeMap) {
			try {
				map.clear();
				map.putAll((TreeMap<String, Object>) obj);
			}
			catch (Exception ignored) { }
		}
	}

	@Override
	public void setNbt(INbt nbt) {
		setNbt(nbt.getMCNBT());
	}

}