package noppes.npcs.api.wrapper.data;

import java.util.*;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.LogWriter;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.util.Util;

public class TempData implements IData {

	protected final Map<String, Object> map = new TreeMap<>();
	protected BlockWrapper block;

	public TempData() { }

	public TempData(BlockWrapper wrapper) {
		this();
		block = wrapper;
	}

	@Override
	public void clear() {
		map.clear();
		if (block != null && block.storage != null) { block.storage.tempData.clear(); }
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
		if (block != null && block.storage != null) { block.storage.tempData.put(key, value); }
	}

	@Override
	public void remove(String key) {
		map.remove(key);
		if (block != null && block.storage != null) { block.storage.tempData.remove(key); }
	}

	@SuppressWarnings("unchecked")
	public void setNbt(NBTTagCompound compound) {
		Object obj = Util.instance.readObjectFromNbt(compound);
		if (obj instanceof TreeMap) {
			try {
				TreeMap<String, Object> newMap = (TreeMap<String, Object>) obj;
				map.clear();
				map.putAll(newMap);
			}
			catch (Exception ignored) { }
		}
		if (block != null && block.storage != null) {
			block.storage.tempData.clear();
			block.storage.tempData.putAll(map);
		}
	}

	@Override
	public void setNbt(INbt nbt) {
		setNbt(nbt.getMCNBT());
	}

}