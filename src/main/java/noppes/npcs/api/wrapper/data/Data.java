package noppes.npcs.api.wrapper.data;

import java.util.*;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.util.Util;

public class Data implements IData {

	private static final NBTWrapper NBT_EMPTY = new NBTWrapper(new NBTTagCompound());
	protected final Map<String, Object> map = new TreeMap<>();
	protected NBTWrapper cacheNbt = NBT_EMPTY;

	@Override
	public void clear() { map.clear(); }

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
		CustomNpcs.debugData.start(this);
		if (cacheNbt == null) {
			cacheNbt = NBT_EMPTY;
			NBTBase tag = Util.instance.writeObjectToNbt(map);
			if (tag instanceof NBTTagCompound) { cacheNbt = new NBTWrapper((NBTTagCompound) tag); }
		}
		CustomNpcs.debugData.end(this);
		return cacheNbt;
	}

	@Override
	public boolean has(String key) {
		return map.containsKey(key);
	}

	@Override
	public void put(String key, Object value) {
		cacheNbt = null;
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
		CustomNpcs.debugData.start(this);
		Object obj = Util.instance.readObjectFromNbt(compound);
		if (obj instanceof TreeMap) {
			try {
				map.clear();
				map.putAll((TreeMap<String, Object>) obj);
				cacheNbt = null;
			}
			catch (Exception ignored) { }
		}
		CustomNpcs.debugData.end(this);
	}

	@Override
	public void setNbt(INbt nbt) { setNbt(nbt.getMCNBT()); }

}