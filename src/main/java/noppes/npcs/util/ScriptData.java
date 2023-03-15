package noppes.npcs.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.handler.data.IScriptData;
import noppes.npcs.api.wrapper.EntityLivingBaseWrapper;
import noppes.npcs.api.wrapper.EntityLivingWrapper;
import noppes.npcs.api.wrapper.EntityWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class ScriptData
implements IScriptData {

	public boolean isConstant = false;
	private String keyName = "null", value = "null", language = "";
	private Object object = null;
	private final Map<String, Class<?>> parameters = Maps.newHashMap();
	private final List<IScriptData> subData = Lists.newArrayList();
	private int type = 0; // 0-any;1-boolean;2-byte;3-short;4-integer;5-long;6-float;7-double;8-string;9-class;10-class[];11-array;12-function;13-undefined

	public ScriptData(NBTTagCompound nbt) {
		this.load(nbt);
	}

	public ScriptData(String keyName, Object object, String language) {
		this.keyName = keyName;
		this.object = object;
		this.type = 0;
		this.value = "null";
		this.language = language;
		if (object == null) {
			return;
		}

		this.value = object.toString();
		if (object instanceof Boolean) {
			this.type = 1;
		} else if (object instanceof Byte) {
			this.type = 2;
		} else if (object instanceof Short) {
			this.type = 3;
		} else if (object instanceof Integer) {
			this.type = 4;
		} else if (object instanceof Long) {
			this.type = 5;
		} else if (object instanceof Float) {
			this.type = 6;
		} else if (object instanceof Double) {
			this.type = 7;
		} else if (object instanceof String) {
			this.type = 8;
		} else if (object instanceof ScriptContainer.Log || object instanceof ScriptContainer.Dump) {
			this.type = 12;
		} else if (object instanceof Class) {
			this.type = 9;
			this.value = ((Class<?>) object).getName();
		} else if (object.getClass().getSimpleName().indexOf("StaticClass") != -1) {
			this.type = 10;
			this.value = this.value.substring(this.value.indexOf('[') + 1, this.value.indexOf(']'));
		} else {
			this.value = object.toString();
			Class<?> clazz = null;
			try {
				clazz = Class.forName("jdk.nashorn.api.scripting.ScriptObjectMirror");
				Method m;
				try {
					m = clazz.getMethod("isArray");
					try {
						if ((boolean) m.invoke(object)) {
							this.type = 11;
							m = clazz.getMethod("keySet");

							@SuppressWarnings("unchecked")
							Set<Object> sets = (Set<Object>) m.invoke(object);
							m = clazz.getMethod("get", Object.class);

							for (Object obj : sets) {
								this.subData.add(new ScriptData(obj.toString(), m.invoke(object, obj), language));
							}
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					}
				} catch (NoSuchMethodException | SecurityException e) {
				}

				try {
					m = clazz.getMethod("isFunction");
					try {
						if ((boolean) m.invoke(object)) {
							this.type = 12;
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					}
				} catch (NoSuchMethodException | SecurityException e) {
				}
				try {
					m = clazz.getMethod("isUndefined");
					try {
						if ((boolean) m.invoke(object)) {
							this.type = 13;
							this.value = "undefined";
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					}
				} catch (NoSuchMethodException | SecurityException e) {
				}
			} catch (ClassNotFoundException e1) {
			}
		}
		if (this.object == null) {
			this.type = 13;
		}
	}

	public Map<String, String[]> getFuncVariables() {
		if (this.type != 12) {
			return null;
		}
		char chr = Character.toChars(0x00A7)[0];
		Map<String, String[]> map = Maps.newHashMap();
		for (String par : this.parameters.keySet()) {
			map.put(par, new String[] { chr + "r" + this.parameters.get(par).getName() });
		}
		if (this.subData.size() > 0) {
			List<String> conss = new ArrayList<String>();
			List<String> arrs = new ArrayList<String>();
			List<String> vars = new ArrayList<String>();
			List<String> funcs = new ArrayList<String>();
			Map<String, String[]> ht = new HashMap<String, String[]>();
			for (IScriptData isd : this.subData) {
				ScriptData sd = (ScriptData) isd;
				String key = sd.getGUIName();
				if (sd.isConstant) {
					conss.add(key);
				} else if (sd.getType() == 11) {
					arrs.add(key);
				} else if (sd.getType() == 12) {
					funcs.add(key);
				} else {
					vars.add(key);
				}
				ht.put(key, sd.getGUIDescription());
			}
			Collections.sort(conss);
			Collections.sort(arrs);
			Collections.sort(vars);
			Collections.sort(funcs);
			for (String key : conss) {
				map.put(key, ht.get(key));
			}
			for (String key : arrs) {
				map.put(key, ht.get(key));
			}
			for (String key : vars) {
				map.put(key, ht.get(key));
			}
			for (String key : funcs) {
				map.put(key, ht.get(key));
			}
		}
		return map;
	}

	public String[] getGUIDescription() {
		char chr = Character.toChars(0x00A7)[0];
		String des = chr + "7" + this.getSubName() + chr + "r = ";
		if (this.type == 8) {
			des += "\"" + this.value + "\"";
		} else if (this.value.equalsIgnoreCase("null")) {
			des += chr + "4null";
		} else {
			des += this.value;
		}
		if (this.type == 11) {
			des = chr + "5Is Array" + chr + "7;";
			if (this.subData.size() > 0) {
				des += "<br>" + chr + "8Body objects:";
				int i = 0;
				for (IScriptData sd : this.subData) {
					if (i > 5) {
						des += "<br>" + chr + "7....";
						break;
					}
					des += "<br>" + chr + "7[" + sd.getName() + "] " + ((ScriptData) sd).getSubName() + chr + "7 = "
							+ chr + "r" + sd.getValue();
					i++;
				}
			}
		} else if (this.type == 12) {
			String pars = "";
			if (this.parameters.size() > 0) {
				for (String key : this.parameters.keySet()) {
					pars += key + ", ";
				}
				pars = pars.substring(0, pars.length() - 2);
			}
			des = chr + "2Function" + chr + "r " + this.keyName + "(" + pars + ")";
			if (this.parameters.size() > 0) {
				des += "<br>" + chr + "8Parameters:";
				for (String key : this.parameters.keySet()) {
					des += "<br>" + chr + "2" + key + chr + "7 = " + chr + "r" + this.parameters.get(key).getName();
					if (key.equals("key") && (this.keyName.equals("getField") || this.keyName.equals("setField") || this.keyName.equals("invoke"))) {
						des += chr + "8 or "+chr+"rjava.lang.Integer";
					}
				}
			}
			if (this.subData.size() > 0) {
				des += "<br>" + chr + "8Body objects:";
				for (IScriptData sd : this.subData) {
					des += "<br>" + chr + "7" + ((ScriptData) sd).getSubName() + " " + ((ScriptData) sd).getGUIName()
							+ chr + "7 = " + chr + "r";
					if (sd.getType() == 8) {
						des += "\"" + sd.getValue() + "\"";
					} else {
						des += sd.getValue();
					}
				}
			}
			if (this.keyName.equals("dump") || this.keyName.equals("log") || this.keyName.equals("getField")
					|| this.keyName.equals("setField") || this.keyName.equals("invoke")) {
				des += "<br>" + chr + "8Description:";
				switch (this.keyName) {
				case "dump": {
					des += "<br>" + chr + "7Returns the value of the variable named [" + chr + "fkey" + chr
							+ "7] from the object (class instance) [" + chr + "fobject" + chr + "7]";
					des += "<br>" + chr + "cReturn" + chr + "7: " + chr + "fIDataObject";
					break;
				}
				case "log": {
					des += "<br>" + chr + "7Writes the string [" + chr + "ftext" + chr + "7] to this object's logs";
					break;
				}
				case "getField": {
					des += "<br>" + chr + "7returns the value of the variable named/pos [" + chr + "fkey" + chr
							+ "7] from the object (class instance) [" + chr + "fobject" + chr + "7]";
					des += "<br>" + chr + "cReturn" + chr + "7: " + chr + "fObject" + chr + "7 or " + chr + "4null";
					break;
				}
				case "setField": {
					des += "<br>" + chr + "7returns " + chr + "2true" + chr
							+ "7 if it was possible to change the variable with the name/pos [" + chr + "fkey" + chr
							+ "7] from the object (instance of the class) [" + chr + "fobject" + chr
							+ "7] to the value [" + chr + "fvalue" + chr + "7]";
					des += "<br>" + chr + "cReturn" + chr + "7: " + chr + "fboolean";
					break;
				}
				case "invoke": {
					des += "<br>" + chr + "7if it was possible to call a method with a [" + chr + "fkey" + chr
							+ "7] from an object (class instance) [" + chr + "fobject" + chr
							+ "7] with the passed values [" + chr + "fvalue" + chr
							+ "7] (value - as a variable or array), then returns the result of this method";
					des += "<br>" + chr + "cReturn" + chr + "7: " + chr + "fObject" + chr + "7 or " + chr + "4null";
					break;
				}
				}
			}
		} else if (this.type == 13) {
			des = chr + "7" + this.getSubName() + " = " + chr + "cUndefined";
		}
		return des.split("<br>");
	}

	public String getGUIName() {
		char chr = Character.toChars(0x00A7)[0];
		String key = chr + (this.isConstant ? "3" : this.type == 11 ? "5" : this.type == 12 ? "2" : "6") + this.keyName;
		if (this.type == 12
				&& (this.keyName.equals("dump") || this.keyName.equals("log") || this.keyName.equals("getField")
						|| this.keyName.equals("setField") || this.keyName.equals("invoke"))) {
			key = chr + "a" + this.keyName;
		}
		return key;
	}

	public String getGUIScrollName() {
		String key = this.getGUIName();
		if (this.type == 12) {
			key += "(";
			for (String par : this.parameters.keySet()) {
				key += par + ",";
			}
			key = key.substring(0, key.length() - 1) + ")";
		}
		return key;
	}

	@Override
	public String getName() {
		return this.keyName;
	}

	@Override
	public NBTTagCompound getNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("ObjectType", this.type);
		nbt.setString("Value", this.value);
		nbt.setString("Language", this.language);
		nbt.setBoolean("IsConstant", this.isConstant);
		if (this.type == 9) {
			nbt.setString("Class", this.value);
		} else if (this.type == 0) {
			nbt.setString("Class", this.object.getClass().getName());
		} else {
			nbt.setString("Class", this.object.getClass().getName());
		}

		NBTTagList subList = new NBTTagList();
		if (this.subData.size() > 0) {
			for (IScriptData sd : this.subData) {
				subList.appendTag(sd.getNBT());
			}
		}
		nbt.setTag("SubData", subList);

		NBTTagList parList = new NBTTagList();
		if (this.parameters.size() > 0) {
			for (String key : this.parameters.keySet()) {
				parList.appendTag(new NBTTagString(key));
				parList.appendTag(new NBTTagString(this.parameters.get(key).getName()));
			}
		}
		nbt.setTag("Parametrs", parList);

		return nbt;
	}

	@Override
	public Object getObject() {
		return this.object;
	}

	public String[] getParametrs() {
		if (this.type != 12) {
			return new String[0];
		}
		ArrayList<String> list = Lists.newArrayList(this.parameters.keySet());
		return list.toArray(new String[list.size()]);
	}

	private String getSubName() {
		String sub = "";
		switch (this.type) {
		case 1: {
			sub = "[boolean]";
			break;
		}
		case 2: {
			sub = "[byte]";
			break;
		}
		case 3: {
			sub = "[short]";
			break;
		}
		case 4: {
			sub = "[int]";
			break;
		}
		case 5: {
			sub = "[long]";
			break;
		}
		case 6: {
			sub = "[float]";
			break;
		}
		case 7: {
			sub = "[double]";
			break;
		}
		case 8: {
			sub = "[string]";
			break;
		}
		case 13: {
			sub = "[undefined]";
			break;
		}
		default: {
			sub = "[object]";
			break;
		}
		}
		return sub;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void load(NBTTagCompound nbt) {
		this.type = nbt.getInteger("ObjectType");
		this.value = nbt.getString("Value");
		this.language = nbt.getString("Language");
		this.isConstant = nbt.getBoolean("IsConstant");

		this.object = new Object();
		try {
			this.object = Class.forName(nbt.getString("Class"));
		} catch (ClassNotFoundException e) {
		}
		switch (this.type) {
		case 1: {
			this.object = Boolean.parseBoolean(this.value);
			break;
		}
		case 2: {
			this.object = Byte.parseByte(this.value);
			break;
		}
		case 3: {
			this.object = Short.parseShort(this.value);
			break;
		}
		case 4: {
			this.object = Integer.parseInt(this.value);
			break;
		}
		case 5: {
			this.object = Long.parseLong(this.value);
			break;
		}
		case 6: {
			this.object = Float.parseFloat(this.value);
			break;
		}
		case 7: {
			this.object = Double.parseDouble(this.value);
			break;
		}
		case 8: {
			this.object = (String) this.value.toString();
			break;
		}
		case 13: {
			this.object = null;
			break;
		}
		}
		this.subData.clear();
		for (int i = 0; i < nbt.getTagList("SubData", 10).tagCount(); i++) {
			this.subData.add(new ScriptData(nbt.getTagList("SubData", 10).getCompoundTagAt(i)));
		}
	}

	public void setVarToFunction(List<ScriptData> vars, Map<String, Class<?>> baseFuncNames) {
		if (this.type != 12) {
			return;
		}
		if (this.object instanceof ScriptContainer.Log) {
			this.parameters.put("text", String.class);
		} else if (this.object instanceof ScriptContainer.Dump) {
			this.parameters.put("object", Object.class);
		}
		if (this.object.toString().indexOf('{') == -1) {
			return;
		}
		ScriptEngine engine = ScriptController.Instance.getEngineByName(this.language);
		String body = this.object.toString();
		body = body.substring(body.indexOf('{') + 1, body.lastIndexOf('}'));
		try {
			engine.eval(body);
		} catch (ScriptException e) {
		}
		List<String> base = Lists.newArrayList();
		for (ScriptData sd : vars) {
			base.add(sd.getName());
			engine.put(sd.getName(), sd.getObject());
		}
		Bindings scriptObjectMirror = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		for (Map.Entry<String, Object> scopeEntry : scriptObjectMirror.entrySet()) {
			if (base.contains(scopeEntry.getKey())) {
				continue;
			}
			this.subData.add(new ScriptData(scopeEntry.getKey(), scopeEntry.getValue(), language));
		}
		String pars = this.value.substring(this.value.indexOf("(") + 1,
				this.value.indexOf(")", this.value.indexOf("(") + 1));
		if (baseFuncNames.containsKey(this.keyName)) {
			this.parameters.put(pars, baseFuncNames.get(this.keyName));
		} else {
			if (this.keyName.equals("getField") && pars.equals("object,key")) {
				this.parameters.put("object", Object.class);
				this.parameters.put("key", String.class);
			} else if ((this.keyName.equals("setField") || this.keyName.equals("invoke"))
					&& pars.equals("object,key,value")) {
				this.parameters.put("object", Object.class);
				this.parameters.put("key", String.class);
				this.parameters.put("value", Object.class);
			} else {
				while (pars.indexOf(" ") != -1) {
					pars = pars.replace(" ", "");
				}
				for (String par : pars.split(",")) {
					this.parameters.put(par, Object.class);
				}
			}
		}
	}

	public Map<String, Class<?>> getVariables(Map<String, Map<Integer, ScriptData>> data) {
		Map<String, Class<?>> map = Maps.newHashMap();
		if (this.type!=12) { return map; }
		for (String key : this.parameters.keySet()) { map.put(key, this.parameters.get(key)); }
		String body = this.object.toString();
		body = body.substring(body.indexOf("{")+1, body.indexOf("}"));
		Map<String, Class<?>> addedMap = AdditionalMethods.getVariablesInBody(body, data, map);
		for (String key : addedMap.keySet()) { map.put(key, addedMap.get(key)); }
		return map;
	}

}
