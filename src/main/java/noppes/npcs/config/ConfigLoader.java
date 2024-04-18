package noppes.npcs.config;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.client.config.IConfigElement;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class ConfigLoader {
	
	public Configuration config;
	
	public ConfigLoader(File directory) {
		if (!directory.exists()) { directory.mkdir(); }
		File file = new File(directory, CustomNpcs.MODNAME + ".cfg");
		List<String> lines = Lists.<String>newArrayList();
		boolean isOldVersion = false;
		boolean needSave = !file.exists();
		if (!needSave) {
			try {
				BufferedReader reader = Files.newReader(file,  Charsets.UTF_8);
				boolean start = true;
				String line;
				while((line = reader.readLine()) != null) {
					if (start && !line.equals("# Configuration file")) { isOldVersion = true; }
					else { break; }
					if (line.indexOf("=") == -1 || line.indexOf("#") == 0) { continue; }
					lines.add(line);
				}
				if (isOldVersion) { Files.write((new String()).getBytes(), file); }
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		config = new Configuration(file);
		for (Field field : CustomNpcs.class.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ConfigProp.class)) { continue; }
			
			ConfigProp prop = field.getAnnotation(ConfigProp.class);
			String name = field.getName();
			ConfigCategory cat = config.getCategory(prop.type());
			Property property = null;
			String classType = field.getType().getTypeName().toLowerCase().replace("integer", "int").replace("[]", "");
			if (classType.lastIndexOf(".") != -1) { classType = classType.substring(classType.lastIndexOf(".") + 1); }
			
			if (cat.containsKey(name)) {
				property = cat.get(name);
				if (property.getType() == Type.COLOR) {
					if (!property.getString().isEmpty() && property.getString().length() != 6) {
						if (property.getString().length() > 6) {
							Color color = new Color((int) Long.parseLong(property.getString(), 16));
							property.set(Integer.toHexString(color.getRGB()).toUpperCase());
						} else {
							String str;
							for (str = property.getString(); str.length() < 6; str = "0" + str) { }
							property.set(str.toUpperCase());
						}
						needSave = true;
					}
					else if (property.getStringList().length > 0) {
						List<String> list = Lists.<String>newArrayList();
						boolean change = false;
						for (String c : property.getStringList()) {
							if (c.length() > 6) {
								change = true;
								Color color = new Color((int) Long.parseLong(c, 16));
								color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
								String str;
								for (str = Integer.toHexString(color.getRGB()).toUpperCase(); str.length() < 6; str = "0" + str) { }
								list.add(str);
							}
							else if (c.length() < 6) {
								change = true;
								String str;
								for (str = c; str.length() < 6; str = "0" + str) { }
								list.add(str.toUpperCase());
							}
						}
						if (change) {
							property.set(list.toArray(new String[list.size()]));
							needSave = true;
						}
					}
				}
				if (!prop.info().isEmpty()) { property.setComment(prop.info()); }
				if (!prop.def().isEmpty()) {
					if (field.getType().isArray()) { property.setValidValues(prop.def().split(",")); }
					else { property.setDefaultValue(prop.def()); }
				}
				boolean isContinue = true;
				if (!field.getType().isArray()) {
					if (!prop.min().isEmpty()) {
						if (classType.equals("int")) { property.setMinValue(Integer.valueOf(prop.min())); }
						else if (classType.equals("double")) { property.setMinValue(Double.valueOf(prop.min())); }
					}
					if (!prop.max().isEmpty()) {
						if (classType.equals("int")) { property.setMaxValue(Integer.valueOf(prop.min())); }
						else if (classType.equals("double")) { property.setMaxValue(Double.valueOf(prop.min())); }
					}
				} else {
					try { isContinue = property.getStringList().length == ((Object[]) field.get(null)).length; }
					catch (Exception e) {}
				}
				if (isContinue) { continue; }
			}
			needSave = true;

			Object object = null;
			try { object = field.get(null); } catch (Exception e) { }
			
			Type type = Type.STRING;
			if (field.getType().isArray()) {
				String[] values = null;
				String[] validValues = prop.def().split(",");
				if (classType.equals("string")) { values = (String[]) object; }
				else if (classType.equals("int")) {
					type = Type.INTEGER;
					if (object == null) { values = validValues; }
					else {
						int[] vs = (int[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) { values[i] = "" + vs[i]; }
					}
				}
				else if (classType.equals("boolean")) {
					type = Type.BOOLEAN;
					if (object == null) { values = validValues; }
					else {
						boolean[] vs = (boolean[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) { values[i] = "" + vs[i]; }
					}
				}
				else if (classType.equals("double")) {
					type = Type.DOUBLE;
					if (object == null) { values = validValues; }
					else {
						double[] vs = (double[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) { values[i] = "" + vs[i]; }
					}
				}
				else if (classType.equals("color")) {
					type = Type.COLOR;
					if (object == null) { values = validValues; }
					else {
						Color[] vs = (Color[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) {
							Color color = new Color(vs[i].getRGB());
							color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
							for (values[i] = Integer.toHexString(color.getRGB()).toUpperCase(); values[i].length() < 6; values[i] = "0" + values[i]) { }
						}
					}
				}
				if (values != null) {
					property = new Property(name, values, type, "config." + name + ".key");
					property.setValidValues(validValues);
				}
			} else {
				String value = object.toString();
				if (classType.equals("int")) { type = Type.INTEGER; value = object.toString(); }
				else if (classType.equals("boolean")) { type = Type.BOOLEAN; value = object.toString(); }
				else if (classType.equals("double")) { type = Type.DOUBLE; value = object.toString(); }
				else if (classType.equals("color")) { type = Type.COLOR; value = Integer.toHexString(((Color) object).getRGB()).toUpperCase(); }
				property = new Property(name, value, type, "config." + name + ".key");
				property.setDefaultValue(prop.def());
				if (!prop.min().isEmpty()) {
					if (classType.equals("int")) { property.setMinValue(Integer.valueOf(prop.min())); }
					else if (classType.equals("double")) { property.setMinValue(Double.valueOf(prop.min())); }
				}
				if (!prop.max().isEmpty()) {
					if (classType.equals("int")) { property.setMaxValue(Integer.valueOf(prop.min())); }
					else if (classType.equals("double")) { property.setMaxValue(Double.valueOf(prop.min())); }
				}
			}
			if (property != null) {
				if (!prop.info().isEmpty()) { property.setComment(prop.info()); }
				cat.put(name, property);
			}
		}
		if (isOldVersion) {
			for (String line : lines) {
				String name = line.substring(0, line.indexOf("="));
				String value = line.substring(line.indexOf("=")+1);
				Property property = null;
				if (config.getCategory(Configuration.CATEGORY_GENERAL).containsKey(name)) {
					for (String key : config.getCategory(Configuration.CATEGORY_GENERAL).keySet()) {
						if (key.equalsIgnoreCase(name)) {
							property = config.getCategory(Configuration.CATEGORY_GENERAL).get(key);
							break;
						}
					}
				}
				else if (config.getCategory(Configuration.CATEGORY_CLIENT).containsKey(name)) {
					for (String key : config.getCategory(Configuration.CATEGORY_CLIENT).keySet()) {
						if (key.equalsIgnoreCase(name)) {
							property = config.getCategory(Configuration.CATEGORY_CLIENT).get(key);
							break;
						}
					}
				}
				if (property == null) { continue; }
				if (value.indexOf("[") == 0) {
					value = value.replace("[", "").replace("]", "");
					property.setValidValues(value.split(", "));
				}
				else if (value.indexOf("{") == 0) {
					value = value.replace("{", "").replace("}", "");
					property.setValidValues(value.split(", "));
				}
				else { property.setValue(value); }
			}
			needSave = true;
		}
		if (needSave) { config.save(); }
		resetData();
	}

	public void resetData() {
		boolean needResetConfig = false;
		for (Field field : CustomNpcs.class.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ConfigProp.class)) { continue; }
			ConfigProp prop = field.getAnnotation(ConfigProp.class);
			String name = field.getName();
			ConfigCategory cat = config.getCategory(prop.type());
			if (!cat.containsKey(name)) { continue; }
			Property property = cat.get(name);
			Type type = property.getType();
			if (field.getType().isArray()) {
				String[] values = property.getValidValues();
				try {
					if (type == Type.STRING) { field.set(null, values); }
					else if (type == Type.INTEGER) {
						int[] base = (int[]) field.get(null);
						int[] def = null; int[] min = null; int[] max = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new int[bd.length];
							for (int i = 0; i < bd.length; i++) { def[i] = Integer.valueOf(bd[i]); }
						}
						if (!prop.min().isEmpty()) {
							String[] bd = prop.min().split(",");
							min = new int[bd.length];
							for (int i = 0; i < bd.length; i++) { min[i] = Integer.valueOf(bd[i]); }
						}
						if (!prop.max().isEmpty()) {
							String[] bd = prop.max().split(",");
							max = new int[bd.length];
							for (int i = 0; i < bd.length; i++) { max[i] = Integer.valueOf(bd[i]); }
						}
						if (def != null && base.length != def.length) {
							int[] newBase = new int[def.length];;
							for (int i = 0; i < def.length; i++) { newBase[i] = i < base.length ? base[i] : def[i]; }
							base = newBase;
							needResetConfig = true;
						}
						int[] vs = new int[base.length];
						for (int i = 0; i < values.length && i < base.length; i++) {
							vs[i] = Integer.valueOf(values[i]);
							if (min != null && i < min.length && vs[i] < min[i]) { vs[i] = min[i]; needResetConfig = true; needResetConfig = true; }
							if (max != null && i < max.length && vs[i] > max[i]) { vs[i] = max[i]; needResetConfig = true; needResetConfig = true; }
						}
						field.set(null, vs);
					}
					else if (type == Type.BOOLEAN) {
						boolean[] base = (boolean[]) field.get(null);
						boolean[] def = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new boolean[bd.length];
							for (int i = 0; i < bd.length; i++) { def[i] = Boolean.valueOf(bd[i]); }
						}
						if (def != null && base.length != def.length) {
							boolean[] newBase = new boolean[def.length];;
							for (int i = 0; i < def.length; i++) { newBase[i] = i < base.length ? base[i] : def[i]; }
							base = newBase;
							needResetConfig = true;
						}
						boolean[] vs = new boolean[values.length];
						for (int i = 0; i < values.length && i < base.length; i++) { vs[i] = Boolean.valueOf(values[i]); }
						field.set(null, vs);
					}
					else if (type == Type.DOUBLE) {
						double[] base = (double[]) field.get(null);
						double[] def = null; double[] min = null; double[] max = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new double[bd.length];
							for (int i = 0; i < bd.length; i++) { def[i] = Double.valueOf(bd[i]); }
						}
						if (!prop.min().isEmpty()) {
							String[] bd = prop.min().split(",");
							min = new double[bd.length];
							for (int i = 0; i < bd.length; i++) { min[i] = Double.valueOf(bd[i]); }
						}
						if (!prop.max().isEmpty()) {
							String[] bd = prop.max().split(",");
							max = new double[bd.length];
							for (int i = 0; i < bd.length; i++) { max[i] = Double.valueOf(bd[i]); }
						}
						if (def != null && base.length != def.length) {
							double[] newBase = new double[def.length];;
							for (int i = 0; i < def.length; i++) { newBase[i] = i < base.length ? base[i] : def[i]; }
							base = newBase;
							needResetConfig = true;
						}
						double[] vs = new double[values.length];
						for (int i = 0; i < values.length && i < base.length; i++) {
							vs[i] = Double.valueOf(values[i]);
							if (min != null && i < min.length && vs[i] < min[i]) { vs[i] = min[i]; needResetConfig = true; }
							if (max != null && i < max.length && vs[i] > max[i]) { vs[i] = max[i]; needResetConfig = true; }
						}
						field.set(null, vs);
					}
					else if (type == Type.COLOR) {
						Color[] base = (Color[]) field.get(null);
						Color[] def = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new Color[bd.length];
							for (int i = 0; i < bd.length; i++) { def[i] = new Color((int) Long.parseLong(bd[i], 16)); }
						}
						if (def != null && base.length != def.length) {
							Color[] newBase = new Color[def.length];;
							for (int i = 0; i < def.length; i++) { newBase[i] = i < base.length ? base[i] : def[i]; }
							base = newBase;
							needResetConfig = true;
						}
						Color[] vs = new Color[values.length];
						for (int i = 0; i < values.length && i < base.length; i++) {
							try { vs[i] = new Color(Integer.valueOf(values[i])); }
							catch (Exception e) {
								vs[i] = new Color((int) Long.parseLong(values[i], 16));
								vs[i] = new Color(vs[i].getRed(), vs[i].getGreen(), vs[i].getBlue(), 0);
							}
						}
						field.set(null, vs);
					}
				} catch (Exception e) { LogWriter.error("Config Error field \"" + name + "\"; type: "+type, e); }
			}
			else {
				try {
					if (type == Type.STRING) { field.set(null, property.getString()); }
					else if (type == Type.INTEGER) {
						int v = Integer.valueOf(property.getString());
						if (!prop.min().isEmpty()) {
							int m = Integer.valueOf(prop.min());
							if (v < m) { v = m; }
						}
						if (!prop.max().isEmpty()) {
							int n = Integer.valueOf(prop.max());
							if (v > n) { v = n; }
						}
						field.set(null, v);
					}
					else if (type == Type.BOOLEAN) { field.set(null, Boolean.valueOf(property.getString())); }
					else if (type == Type.DOUBLE) {
						double v = Double.valueOf(property.getString());
						if (!prop.min().isEmpty()) {
							double m = Double.valueOf(prop.min());
							if (v < m) { v = m; }
						}
						if (!prop.max().isEmpty()) {
							double n = Double.valueOf(prop.max());
							if (v > n) { v = n; }
						}
						field.set(null, v);
					}
					else if (type == Type.COLOR) {
						Color color = new Color((int) Long.parseLong(property.getString(), 16));
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
						field.set(null, color);
					}
				} catch (Exception e) { LogWriter.error("Config Error field \"" + name + "\"; type: "+type, e); }
			}
		}
		if (needResetConfig) { resetConfig(); }
	}

	public void resetConfig() {
		for (Field field : CustomNpcs.class.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ConfigProp.class)) { continue; }
			
			ConfigProp prop = field.getAnnotation(ConfigProp.class);
			String name = field.getName();
			ConfigCategory cat = config.getCategory(prop.type());
			Property property = null;
			String classType = field.getType().getTypeName().toLowerCase().replace("integer", "int").replace("[]", "");
			if (classType.lastIndexOf(".") != -1) { classType = classType.substring(classType.lastIndexOf(".") + 1); }
			Object object = null;
			try { object = field.get(null); } catch (Exception e) { }
			Type type = Type.STRING;
			if (field.getType().isArray()) {
				String[] values = null;
				String[] validValues = prop.def().split(",");
				if (classType.equals("string")) { values = (String[]) object; }
				else if (classType.equals("int")) {
					type = Type.INTEGER;
					if (object == null) { values = validValues; }
					else {
						int[] vs = (int[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) { values[i] = "" + vs[i]; }
					}
				}
				else if (classType.equals("boolean")) {
					type = Type.BOOLEAN;
					if (object == null) { values = validValues; }
					else {
						boolean[] vs = (boolean[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) { values[i] = "" + vs[i]; }
					}
				}
				else if (classType.equals("double")) {
					type = Type.DOUBLE;
					if (object == null) { values = validValues; }
					else {
						double[] vs = (double[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) { values[i] = "" + vs[i]; }
					}
				}
				else if (classType.equals("color")) {
					type = Type.COLOR;
					if (object == null) { values = validValues; }
					else {
						Color[] vs = (Color[]) object;
						values = new String[vs.length];
						for (int i = 0; i < vs.length; i++) {
							values[i] = Integer.toHexString((vs[i]).getRGB()).toUpperCase();
							if (vs[i].getAlpha() == 0) { values[i] += 0xFF000000; }
						}
					}
				}
				if (values != null) {
					property = new Property(name, values, type, "config." + name + ".key");
					property.setValidValues(validValues);
					if (!prop.min().isEmpty()) {
						if (classType.equals("int")) { property.setMinValue(Integer.valueOf(prop.min())); }
						else if (classType.equals("double")) { property.setMinValue(Double.valueOf(prop.min())); }
					}
					if (!prop.max().isEmpty()) {
						if (classType.equals("int")) { property.setMaxValue(Integer.valueOf(prop.min())); }
						else if (classType.equals("double")) { property.setMaxValue(Double.valueOf(prop.min())); }
					}
				}
			} else {
				String value = object.toString();
				if (classType.equals("int")) { type = Type.INTEGER; value = object.toString(); }
				else if (classType.equals("boolean")) { type = Type.BOOLEAN; value = object.toString(); }
				else if (classType.equals("double")) { type = Type.DOUBLE; value = object.toString(); }
				else if (classType.equals("color")) { type = Type.COLOR; value = Integer.toHexString(((Color) object).getRGB()).toUpperCase(); }
				property = new Property(name, value, type, "config." + name + ".key");
				property.setDefaultValue(prop.def());
				if (!prop.min().isEmpty()) {
					if (classType.equals("int")) { property.setMinValue(Integer.valueOf(prop.min())); }
					else if (classType.equals("double")) { property.setMinValue(Double.valueOf(prop.min())); }
				}
				if (!prop.max().isEmpty()) {
					if (classType.equals("int")) { property.setMaxValue(Integer.valueOf(prop.min())); }
					else if (classType.equals("double")) { property.setMaxValue(Double.valueOf(prop.min())); }
				}
			}
			if (property != null) {
				if (!prop.info().isEmpty()) { property.setComment(prop.info()); }
				cat.put(name, property);
			}
		}
		config.save();
	}

	public List<IConfigElement> getChildElements() {
		Iterator<Property> pI = config.getCategory(Configuration.CATEGORY_GENERAL).getOrderedValues().iterator();
		Map<String, ConfigElement> map = Maps.<String, ConfigElement>newTreeMap();
		while (pI.hasNext()) {
			Property p = pI.next();
			map.put(p.getName(), new ConfigElement(p));
		}
		pI = config.getCategory(Configuration.CATEGORY_CLIENT).getOrderedValues().iterator();
		while (pI.hasNext()) {
			Property p = pI.next();
			map.put(p.getName(), new ConfigElement(p));
		}
		return Lists.newArrayList(map.values());
	}

}
