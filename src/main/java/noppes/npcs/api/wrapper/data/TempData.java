package noppes.npcs.api.wrapper.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.ObfuscationHelper;

public class TempData
implements IData {
	
	private Map<String, Object> map;
	private BlockWrapper block;

	public TempData() {
		this.map = Maps.<String, Object>newTreeMap();
	}
	
	public TempData(BlockWrapper wrapper) {
		this();
		this.block = wrapper;
	}

	private void resetData() {
		if (this.block != null) {
			if (this.block.storage == null) { return; }
			this.map = this.block.storage.tempData;
			return;
		}
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
		return sets.toArray(new String[sets.size()]);
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

	@Override
	public INbt getNbt() {
		this.resetData();
		NBTTagCompound compound = new NBTTagCompound();
		for (String key : this.map.keySet()) {
			Object value = this.map.get(key);
			if (value.getClass().isArray()) {
				Object[] vs = (Object[]) value;
				if (vs.length==0) {
					compound.setTag(key, new NBTTagList());
					continue;
				}
				if (vs[0] instanceof Byte) {
					List<Byte> l = Lists.<Byte>newArrayList();
					for (Object v : vs) { if (v instanceof Byte) { l.add((Byte) v); } }
					byte[] arr = new byte[l.size()];
					int i = 0;
					for (byte d : l) { arr[i] = d; i++; }
					compound.setByteArray(key, arr);
				}
				else if (vs[0] instanceof Integer) {
					List<Integer> l = Lists.<Integer>newArrayList();
					for (Object v : vs) { if (v instanceof Integer) { l.add((Integer) v); } }
					int[] arr = new int[l.size()];
					int i = 0;
					for (int d : l) { arr[i] = d; i++; }
					compound.setIntArray(key, arr);
				}
				else if (vs[0] instanceof Long) {
					List<Long> l = Lists.<Long>newArrayList();
					for (Object v : vs) { if (v instanceof Long) { l.add((Long) v); } }
					long[] arr = new long[l.size()];
					int i = 0;
					for (long d : l) { arr[i] = d; i++; }
					compound.setTag(key, new NBTTagLongArray(arr));
				}
				else if (vs[0] instanceof Short || vs[0] instanceof Float || vs[0] instanceof Double) {
					NBTTagList list = new NBTTagList();
					for (Object v : vs) {
						double d;
						if (v instanceof Short) { d = (double) (Short) v; }
						else if (v instanceof Float) { d = (double) (Float) v; }
						else if (v instanceof Double) { d = (Double) v; }
						else { continue; }
						list.appendTag(new NBTTagDouble(d));
					}
					compound.setTag(key, list);
				}
			}
			else if (value instanceof Byte) { compound.setByte(key, (Byte) value); }
			else if (value instanceof Short) { compound.setShort(key, (Short) value); }
			else if (value instanceof Integer) { compound.setInteger(key, (Integer) value); }
			else if (value instanceof Long) { compound.setLong(key, (Long) value); }
			else if (value instanceof Float) { compound.setFloat(key, (Float) value); }
			else if (value instanceof Double) { compound.setDouble(key, (Double) value); }
			else if (value instanceof String) { compound.setString(key, (String) value); }
			else {
				String clazz = value.toString();
				if ((clazz.equals("[object Array]") || clazz.equals("[object Object]")) && value instanceof Bindings) {
					boolean isArray = clazz.equals("[object Array]");
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setBoolean("IsArray", isArray);
					for (Map.Entry<String, Object> scopeEntry : ((Bindings) value).entrySet()) {
						Object v = scopeEntry.getValue();
						if (v.getClass().isArray()) {
							Object[] vs = (Object[]) v;
							if (vs.length==0) {
								nbt.setTag(scopeEntry.getKey(), new NBTTagList());
								continue;
							}
							if (vs[0] instanceof Byte) {
								List<Byte> l = Lists.<Byte>newArrayList();
								for (Object va : vs) { if (va instanceof Byte) { l.add((Byte) va); } }
								byte[] arr = new byte[l.size()];
								int i = 0;
								for (byte d : l) { arr[i] = d; i++; }
								nbt.setByteArray(scopeEntry.getKey(), arr);
							}
							else if (vs[0] instanceof Integer) {
								List<Integer> l = Lists.<Integer>newArrayList();
								for (Object va : vs) { if (va instanceof Integer) { l.add((Integer) va); } }
								int[] arr = new int[l.size()];
								int i = 0;
								for (int d : l) { arr[i] = d; i++; }
								nbt.setIntArray(scopeEntry.getKey(), arr);
							}
							else if (vs[0] instanceof Long) {
								List<Long> l = Lists.<Long>newArrayList();
								for (Object va : vs) { if (va instanceof Long) { l.add((Long) va); } }
								long[] arr = new long[l.size()];
								int i = 0;
								for (long d : l) { arr[i] = d; i++; }
								nbt.setTag(scopeEntry.getKey(), new NBTTagLongArray(arr));
							}
							else if (vs[0] instanceof Short || vs[0] instanceof Float || vs[0] instanceof Double) {
								NBTTagList list = new NBTTagList();
								for (Object va : vs) {
									double d;
									if (va instanceof Short) { d = (double) (Short) va; }
									else if (va instanceof Float) { d = (double) (Float) va; }
									else if (va instanceof Double) { d = (Double) va; }
									else { continue; }
									list.appendTag(new NBTTagDouble(d));
								}
								nbt.setTag(key, list);
							}
						}
						else if (v instanceof Byte) { nbt.setByte(scopeEntry.getKey(), (Byte) v); }
						else if (v instanceof Short) { nbt.setShort(scopeEntry.getKey(), (Short) v); }
						else if (v instanceof Integer) { nbt.setInteger(scopeEntry.getKey(), (Integer) v); }
						else if (v instanceof Long) { nbt.setLong(scopeEntry.getKey(), (Long) v); }
						else if (v instanceof Float) { nbt.setFloat(scopeEntry.getKey(), (Float) v); }
						else if (v instanceof Double) { nbt.setDouble(scopeEntry.getKey(), (Double) v); }
						else if (v instanceof String) { nbt.setString(scopeEntry.getKey(), (String) v); }
					}
					compound.setTag(key, nbt);
				}
			}
		}
		return NpcAPI.Instance().getINbt(compound); 
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
				Object[] vs = (Object[]) value;
				if (vs.length>0 && vs[0] instanceof Byte || vs[0] instanceof Short || vs[0] instanceof Integer || vs[0] instanceof Long || vs[0] instanceof Float || vs[0] instanceof Double || vs[0] instanceof String) {
					del.add(key);
				}
			}
		}
		
		for (String key : nbt.getMCNBT().getKeySet()) {
			NBTBase tag = nbt.getMCNBT().getTag(key);
			Object value = null;
			if (tag instanceof NBTTagByte) { value = ((NBTTagByte) tag).getByte(); }
			else if (tag instanceof NBTTagShort) { value = ((NBTTagShort) tag).getShort(); }
			else if (tag instanceof NBTTagInt) { value = ((NBTTagInt) tag).getInt(); }
			else if (tag instanceof NBTTagLong) { value = ((NBTTagLong) tag).getLong(); }
			else if (tag instanceof NBTTagFloat) { value = ((NBTTagFloat) tag).getFloat(); }
			else if (tag instanceof NBTTagDouble) { value = ((NBTTagDouble) tag).getDouble(); }
			else if (tag instanceof NBTTagByteArray) { value = ((NBTTagByteArray) tag).getByteArray();}
			else if (tag instanceof NBTTagIntArray) { value = ((NBTTagIntArray) tag).getIntArray();}
			else if (tag instanceof NBTTagLongArray) { value = ObfuscationHelper.getValue(NBTTagLongArray.class, (NBTTagLongArray) tag, 0); }
			else if (tag instanceof NBTTagList) {
				Object[] arr = new Object[((NBTTagList) tag).tagCount()];
				for (int i = 0; i < arr.length; i++) {
					NBTBase base = ((NBTTagList) tag).get(i);
					switch(base.getId()) {
						case 1: { arr[i] = ((NBTTagByte) base).getByte(); break; }
						case 2: { arr[i] = ((NBTTagShort) base).getShort(); break; }
						case 3: { arr[i] = ((NBTTagInt) base).getInt(); break; }
						case 4: { arr[i] = ((NBTTagLong) base).getLong(); break; }
						case 5: { arr[i] = ((NBTTagFloat) base).getFloat(); break; }
						case 6: { arr[i] = ((NBTTagDouble) base).getDouble(); break; }
						case 7: { arr[i] = ((NBTTagByteArray) base).getByteArray(); break; }
						case 8: { arr[i] = ((NBTTagString) base).getString(); break; }
						case 11: { arr[i] = ((NBTTagIntArray) base).getIntArray(); break; }
						case 12: { arr[i] = ObfuscationHelper.getValue(NBTTagLongArray.class, (NBTTagLongArray) base, 0); break; }
					}
				}
				value = arr;
			}
			else if (tag instanceof NBTTagCompound) {
				boolean isArray = ((NBTTagCompound) tag).getBoolean("IsArray");
				ScriptEngine engine = ScriptController.Instance.getEngineByName("ECMAScript");
				if (engine==null) { continue; }
				try {
					String str = "JSON.parse('"+(isArray ? "[" : "{");
					Set<String> sets = ((NBTTagCompound) tag).getKeySet();
					Map<String, Object> map = Maps.<String, Object>newTreeMap();
					for (String k : sets) {
						if (k.equals("IsArray")) { continue; }
						map.put(k, ((NBTTagCompound) tag).getTag(k).getId()==8 ? ((NBTTagCompound) tag).getString(k) : ((NBTTagCompound) tag).getDouble(k));
					}
					for (String k : map.keySet()) {
						String s = map.get(k) instanceof String ? "\""+map.get(k)+"\"" : map.get(k).toString();
						if (isArray) { str += s+", "; }
						else { str += "\""+k+"\":"+s+", "; }
					}
					str = str.substring(0, str.length()-2) + (isArray ? "]" : "}")+"')";
					value = engine.eval(str);
				}
				catch (Exception e) {}
			}
			if (value == null) { continue; }
			this.map.put(key, value);
		}
	}
	
}