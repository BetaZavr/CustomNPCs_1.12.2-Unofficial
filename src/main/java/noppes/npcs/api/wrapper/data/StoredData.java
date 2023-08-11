package noppes.npcs.api.wrapper.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
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
import noppes.npcs.api.wrapper.EntityWrapper;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.ObfuscationHelper;

public class StoredData
implements IData {
		
	private NBTTagCompound data;
	private BlockWrapper block;
	private EntityWrapper<?> entity;
	private ItemStackWrapper stack;
	private ScriptController controller;
	
	public StoredData() {
		this.data = new NBTTagCompound();
	}

	public StoredData(BlockWrapper wrapper) {
		this();
		this.block = wrapper;
	}

	public StoredData(EntityWrapper<?> wrapper) {
		this();
		this.entity = wrapper;
	}

	public StoredData(ItemStackWrapper stack) {
		this();
		stack.storedData = new NBTTagCompound();
		this.data = stack.storedData;
		this.stack = stack;
	}

	public StoredData(ScriptController controller) {
		this();
		this.controller = controller;
	}

	private void resetData() {
		if (this.block != null) {
			if (this.block.tile == null) { return; }
			if (!this.block.tile.getTileData().hasKey("CustomNPCsData", 10)) {
				this.block.tile.getTileData().setTag("CustomNPCsData", new NBTTagCompound());
			}
			this.data = this.block.tile.getTileData().getCompoundTag("CustomNPCsData");
			return;
		}
		else if (this.entity != null) {
			if (this.entity.getMCEntity() instanceof EntityPlayer) {
				this.data = PlayerData.get((EntityPlayer) this.entity.getMCEntity()).scriptStoreddata;
			} else {
				if (!this.entity.getMCEntity().getEntityData().hasKey("CNPCStoredData", 10)) {
					this.entity.getMCEntity().getEntityData().setTag("CNPCStoredData", new NBTTagCompound());
				}
				this.data = this.entity.getMCEntity().getEntityData().getCompoundTag("CNPCStoredData");
			}
		}
		else if (this.stack != null) {
			this.data = this.stack.storedData;
		}
		else if (this.controller != null) {
			this.data = this.controller.compound;
		}
	}

	@Override
	public void clear() {
		this.resetData();
		for (String key : this.data.getKeySet().toArray(new String[this.data.getKeySet().size()])) { this.data.removeTag(key); }
		if (this.controller != null) { this.controller.shouldSave = true; }
	}

	@Override
	public Object get(String key) {
		System.out.println("key: "+key);
		this.resetData();
		if (!this.data.hasKey(key)) { return null; }
		NBTBase base = this.data.getTag(key);
		if (base instanceof NBTTagByte) { return ObfuscationHelper.getValue(NBTTagByte.class, (NBTTagByte) base, 0); }
		else if (base instanceof NBTTagShort) { return ObfuscationHelper.getValue(NBTTagShort.class, (NBTTagShort) base, 0); }
		else if (base instanceof NBTTagInt) { return ObfuscationHelper.getValue(NBTTagInt.class, (NBTTagInt) base, 0); }
		else if (base instanceof NBTTagLong) { return ObfuscationHelper.getValue(NBTTagLong.class, (NBTTagLong) base, 0); }
		else if (base instanceof NBTTagFloat) { return ObfuscationHelper.getValue(NBTTagFloat.class, (NBTTagFloat) base, 0); }
		else if (base instanceof NBTTagDouble) { return ObfuscationHelper.getValue(NBTTagDouble.class, (NBTTagDouble) base, 0); }
		else if (base instanceof NBTTagString) { return ObfuscationHelper.getValue(NBTTagString.class, (NBTTagString) base, 0); }
		else if (base instanceof NBTTagByteArray) { return ((NBTTagByteArray) base).getByteArray(); }
		else if (base instanceof NBTTagIntArray) { return ((NBTTagIntArray) base).getIntArray(); }
		else if (base instanceof NBTTagLongArray) { return ObfuscationHelper.getValue(NBTTagLongArray.class, (NBTTagLongArray) base, 0); }
		else if (base instanceof NBTTagList) {
			Object[] arr = new Object[((NBTTagList) base).tagCount()];
			switch(((NBTTagList) base).getTagType()) {
				case 1: {
					List<NBTTagByte> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagByte.class, tagList.get(i), 0); }
					break;
				}
				case 2: {
					List<NBTTagShort> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagShort.class, tagList.get(i), 0); }
					break;
				}
				case 3: {
					List<NBTTagInt> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagInt.class, tagList.get(i), 0); }
					break;
				}
				case 4: {
					List<NBTTagLong> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagLong.class, tagList.get(i), 0); }
					break;
				}
				case 5: {
					List<NBTTagFloat> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagFloat.class, tagList.get(i), 0); }
					break;
				}
				case 6: {
					List<NBTTagDouble> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagDouble.class, tagList.get(i), 0); }
					break;
				}
				case 7: {
					List<NBTTagByteArray> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagByteArray.class, tagList.get(i), 0); }
					break;
				}
				case 8: {
					List<NBTTagString> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagString.class, tagList.get(i), String.class); }
					break;
				}
				case 11: {
					List<NBTTagIntArray> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagIntArray.class, tagList.get(i), 0); }
					break;
				}
				case 12: {
					List<NBTTagLongArray> tagList = ObfuscationHelper.getValue(NBTTagList.class, (NBTTagList) base, 1);
					for (int i = 0; i < arr.length; i++) { arr[i] = ObfuscationHelper.getValue(NBTTagLongArray.class, tagList.get(i), 0); }
					break;
				}
				default: { break; }
			}
			return arr;
		}
		else if (base instanceof NBTTagCompound) {
			System.out.println("base: "+(NBTTagCompound) base);
			boolean isArray = ((NBTTagCompound) base).getBoolean("IsArray");
			ScriptEngine engine = ScriptController.Instance.getEngineByName("ECMAScript");
			if (engine==null) { return null; }
			try {
				String str = "JSON.parse('"+(isArray ? "[" : "{");
				Set<String> sets = ((NBTTagCompound) base).getKeySet();
				Map<String, Object> map = Maps.<String, Object>newTreeMap();
				for (String k : sets) {
					if (k.equals("IsArray")) { continue; }
					map.put(k, ((NBTTagCompound) base).getTag(k).getId()==8 ? ((NBTTagCompound) base).getString(k) : ((NBTTagCompound) base).getDouble(k));
				}
				for (String k : map.keySet()) {
					String s = map.get(k) instanceof String ? "\""+map.get(k)+"\"" : map.get(k).toString();
					if (isArray) { str += s+", "; }
					else { str += "\""+k+"\":"+s+", "; }
				}
				str = str.substring(0, str.length()-2) + (isArray ? "]" : "}")+"')";
				return engine.eval(str);
			}
			catch (Exception e) {}
		}
		return null;
	}

	@Override
	public String[] getKeys() {
		this.resetData();
		Set<String> sets = this.data.getKeySet();
		return sets.toArray(new String[sets.size()]);
	}

	@Override
	public boolean has(String key) {
		this.resetData();
		return this.data.hasKey(key);
	}

	@Override
	public void put(String key, Object value) throws CommandException {
		this.resetData();
		if (value.getClass().isArray()) {
			Object[] vs = (Object[]) value;
			if (vs.length==0) {
				this.data.setTag(key, new NBTTagList());
				return;
			}
			if (vs[0] instanceof Byte) {
				List<Byte> l = Lists.<Byte>newArrayList();
				for (Object v : vs) { if (v instanceof Byte) { l.add((Byte) v); } }
				byte[] arr = new byte[l.size()];
				int i = 0;
				for (byte d : l) { arr[i] = d; i++; }
				this.data.setByteArray(key, arr);
			}
			else if (vs[0] instanceof Integer) {
				List<Integer> l = Lists.<Integer>newArrayList();
				for (Object v : vs) { if (v instanceof Integer) { l.add((Integer) v); } }
				int[] arr = new int[l.size()];
				int i = 0;
				for (int d : l) { arr[i] = d; i++; }
				this.data.setIntArray(key, arr);
			}
			else if (vs[0] instanceof Long) {
				List<Long> l = Lists.<Long>newArrayList();
				for (Object v : vs) { if (v instanceof Long) { l.add((Long) v); } }
				long[] arr = new long[l.size()];
				int i = 0;
				for (long d : l) { arr[i] = d; i++; }
				this.data.setTag(key, new NBTTagLongArray(arr));
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
				this.data.setTag(key, list);
			}
		}
		else if (value instanceof Byte) { this.data.setByte(key, (Byte) value); }
		else if (value instanceof Short) { this.data.setShort(key, (Short) value); }
		else if (value instanceof Integer) { this.data.setInteger(key, (Integer) value); }
		else if (value instanceof Long) { this.data.setLong(key, (Long) value); }
		else if (value instanceof Float) { this.data.setFloat(key, (Float) value); }
		else if (value instanceof Double) { this.data.setDouble(key, (Double) value); }
		else if (value instanceof String) { this.data.setString(key, (String) value); }
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
					else {
						throw new CommandException("Unsupported data type to put in StoredData. To "+(isArray ? "array; Pos" : "object; Key")+": "+scopeEntry.getKey()+"; Value: "+v.toString(), new Object[0]);
					}
				}
				this.data.setTag(key, nbt);
				return;
			}
			throw new CommandException("Unsupported data type to put in StoredData. Key: \""+key+"\"; Value: "+value.toString(), new Object[0]);
		}
		if (this.controller != null) { this.controller.shouldSave = true; }
	}

	@Override
	public boolean remove(String key) {
		this.resetData();
		if (!this.data.hasKey(key)) { return false; }
		this.data.removeTag(key);
		if (this.controller != null) { this.controller.shouldSave = true; }
		return true;
	}

	@Override
	public INbt getNbt() {
		this.resetData();
		return NpcAPI.Instance().getINbt(this.data);
	}
	
	@Override
	public void setNbt(INbt nbt) {
		NBTTagCompound compound = nbt.getMCNBT().copy();
		this.clear();
		for (String key : compound.getKeySet()) { this.data.setTag(key, compound.getTag(key)); }
		if (this.controller != null) { this.controller.shouldSave = true; }
	}
	
}