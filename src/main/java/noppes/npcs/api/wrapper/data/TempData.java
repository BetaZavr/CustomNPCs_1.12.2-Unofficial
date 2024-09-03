package noppes.npcs.api.wrapper.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.util.Util;

public class TempData implements IData {

	private Map<String, Object> map;
	private BlockWrapper block;

	public TempData() {
		this.map = Maps.newTreeMap();
	}

	public TempData(BlockWrapper wrapper) {
		this();
		this.block = wrapper;
	}

	@Override
	public void clear() {
		this.resetData();
		this.map.clear();
	}

	@Override
	public Object get(String key) {
		this.resetData();
		return this.map.get(key);
	}

	@Override
	public String[] getKeys() {
		this.resetData();
		Set<String> sets = this.map.keySet();
		return sets.toArray(new String[0]);
	}

	@Override
	public INbt getNbt() {
		this.resetData();
		NBTTagCompound compound = new NBTTagCompound();
		for (String key : this.map.keySet()) {
			NBTBase nbt = Util.instance.writeObjectToNbt(this.map.get(key));
			if (nbt != null) {
				compound.setTag(key, nbt);
			}
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(compound);
	}

	@Override
	public boolean has(String key) {
		this.resetData();
		return this.map.containsKey(key);
	}

	@Override
	public void put(String key, Object value) {
		this.resetData();
		this.map.put(key, value);
	}

	@Override
	public boolean remove(String key) {
		this.resetData();
		if (this.map.containsKey(key)) {
			this.map.remove(key);
			return true;
		}
		return false;
	}

	private void resetData() {
		if (this.block != null) {
			if (this.block.storage == null) {
				return;
			}
			this.map = this.block.storage.tempData;
        }
	}

	@Override
	public void setNbt(INbt nbt) {
		this.resetData();
		List<String> del = Lists.newArrayList();
		for (String key : this.map.keySet()) {
			Object value = this.map.get(key);
			if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double || value instanceof String) {
				del.add(key);
			}
			if (value.getClass().isArray()) {
                assert value instanceof Object[];
                Object[] vs = (Object[]) value;
				if (vs.length > 0 && vs[0] instanceof Byte || vs[0] instanceof Short || vs[0] instanceof Integer
						|| vs[0] instanceof Long || vs[0] instanceof Float || vs[0] instanceof Double
						|| vs[0] instanceof String) {
					del.add(key);
				}
			}
		}
		for (String key : del) {
			this.map.remove(key);
		}
		for (String key : nbt.getMCNBT().getKeySet()) {
			Object value = Util.instance.readObjectFromNbt(nbt.getMCNBT().getTag(key));
			if (value == null) {
				continue;
			}
			this.map.put(key, value);
		}
	}

}