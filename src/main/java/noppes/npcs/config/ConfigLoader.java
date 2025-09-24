package noppes.npcs.config;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.client.config.IConfigElement;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.Util;

public class ConfigLoader {

	private static final List<Field> data = new ArrayList<>();
	public Configuration config;

	public ConfigLoader(File directory) {
		if (!directory.exists() && !directory.mkdir()) { return; }
		File file = new File(directory, CustomNpcs.MODNAME + " Unofficial.cfg");
		List<String> lines = new ArrayList<>();
		boolean isOldVersion = false;
		boolean needSave = !file.exists();
		if (!needSave) {
			String text = Util.instance.loadFile(file);
			isOldVersion = text.indexOf("# Configuration file") != 0;
			if (isOldVersion) {
				for (String line : text.split("" + ((char) 10))) {
					if (!line.contains("=") || line.indexOf("#") == 0) { continue; }
					lines.add(line);
				}
				Util.instance.saveFile(file, "");
			}
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
							StringBuilder str = new StringBuilder(property.getString());
							while (str.length() < 6 ) { str.insert(0, "0"); }
							property.set(str.toString().toUpperCase());
						}
						needSave = true;
					} else if (property.getStringList().length > 0) {
						List<String> list = new ArrayList<>();
						boolean change = false;
						for (String c : property.getStringList()) {
							if (c.length() > 6) {
								change = true;
								Color color = new Color((int) Long.parseLong(c, 16));
								color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
								StringBuilder str = new StringBuilder(Integer.toHexString(color.getRGB()).toUpperCase());
								while (str.length() < 6 ) { str.insert(0, "0"); }
								list.add(str.toString().toUpperCase());
							} else if (c.length() < 6) {
								change = true;
								StringBuilder str = new StringBuilder(c);
								while (str.length() < 6 ) { str.insert(0, "0"); }
								list.add(str.toString().toUpperCase());
							}
						}
						if (change) {
							property.set(list.toArray(new String[0]));
							needSave = true;
						}
					}
				}
				if (!prop.info().isEmpty()) {
					property.setComment(prop.info());
				}
				if (!prop.def().isEmpty()) {
					if (field.getType().isArray()) {
						property.setValidValues(prop.def().split(","));
					} else {
						property.setDefaultValue(prop.def());
					}
				}
				boolean isContinue = true;
				if (!field.getType().isArray()) {
					if (!prop.min().isEmpty()) {
						if (classType.equals("int")) {
							property.setMinValue(Integer.parseInt(prop.min()));
						} else if (classType.equals("double")) {
							property.setMinValue(Double.parseDouble(prop.min()));
						}
					}
					if (!prop.max().isEmpty()) {
						if (classType.equals("int")) {
							property.setMaxValue(Integer.parseInt(prop.max()));
						} else if (classType.equals("double")) {
							property.setMaxValue(Double.parseDouble(prop.max()));
						}
					}
				} else {
					try {
						Object objArray = field.get(null);
						int size = 0;
						if (objArray instanceof int[]) { size = ((int[]) objArray).length; }
						else if (objArray instanceof Object[]) { size = ((Object[]) objArray).length; }
						else if (objArray instanceof double[]) { size = ((double[]) objArray).length; }
						isContinue = property.getStringList().length == size;
					}
					catch (Exception e) { LogWriter.error("Error Load: \""+field.getName()+"\"", e); }
				}
				if (isContinue) {
					continue;
				}
			}
			needSave = true;

			Object object = null;
			try {
				object = field.get(null);
			} catch (Exception e) { LogWriter.error(e); }

			Type type = Type.STRING;
			if (field.getType().isArray()) {
				String[] values = null;
				String[] validValues = prop.def().split(",");
                switch (classType) {
                    case "string":
                        values = (String[]) object;
                        break;
                    case "int":
                        type = Type.INTEGER;
                        if (object == null) {
                            values = validValues;
                        } else {
                            int[] vs = (int[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                values[i] = "" + vs[i];
                            }
                        }
                        break;
                    case "boolean":
                        type = Type.BOOLEAN;
                        if (object == null) {
                            values = validValues;
                        } else {
                            boolean[] vs = (boolean[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                values[i] = "" + vs[i];
                            }
                        }
                        break;
                    case "double":
                        type = Type.DOUBLE;
                        if (object == null) {
                            values = validValues;
                        } else {
                            double[] vs = (double[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                values[i] = "" + vs[i];
                            }
                        }
                        break;
                    case "color":
                        type = Type.COLOR;
                        if (object == null) {
                            values = validValues;
                        } else {
                            Color[] vs = (Color[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                Color color = new Color(vs[i].getRGB());
                                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
								StringBuilder str = new StringBuilder(Integer.toHexString(color.getRGB()).toUpperCase());
								while (str.length() < 6 ) { str.insert(0, "0"); }
								values[i] = str.toString();
								if (values[i].length() > 6) { values[i] = values[i].substring(values[i].length() - 6); }
                            }
                        }
                        break;
                }
				if (values != null) {
					property = new Property(name, values, type, "config." + name + ".key");
					property.setValidValues(validValues);
				}
			} else {
                if (object != null) {
					String value = object.toString();
                    switch (classType) {
                        case "int":
                            type = Type.INTEGER;
                            value = object.toString();
                            break;
                        case "boolean":
                            type = Type.BOOLEAN;
                            value = object.toString();
                            break;
                        case "double":
                            type = Type.DOUBLE;
                            value = object.toString();
                            break;
                        case "color":
                            type = Type.COLOR;
							StringBuilder str = new StringBuilder(Integer.toHexString(((Color) object).getRGB()).toUpperCase());
							while (str.length() < 6 ) { str.insert(0, "0"); }
							value = str.toString();
							if (value.length() > 6) { value = value.substring(value.length() - 6); }
                            break;
                    }
					property = new Property(name, value, type, "config." + name + ".key");
					property.setDefaultValue(prop.def());
					if (!prop.min().isEmpty()) {
						if (classType.equals("int")) {
							property.setMinValue(Integer.parseInt(prop.min()));
						} else if (classType.equals("double")) {
							property.setMinValue(Double.parseDouble(prop.min()));
						}
					}
					if (!prop.max().isEmpty()) {
						if (classType.equals("int")) {
							property.setMaxValue(Integer.parseInt(prop.max()));
						} else if (classType.equals("double")) {
							property.setMaxValue(Double.parseDouble(prop.max()));
						}
					}
				}
			}
			if (property != null) {
				if (!prop.info().isEmpty()) {
					property.setComment(prop.info());
				}
				cat.put(name, property);
			}
		}
		if (isOldVersion) {
			for (String line : lines) {
				String name = line.substring(0, line.indexOf("="));
				String value = line.substring(line.indexOf("=") + 1);
				Property property = null;
				if (config.getCategory(Configuration.CATEGORY_GENERAL).containsKey(name)) {
					for (String key : config.getCategory(Configuration.CATEGORY_GENERAL).keySet()) {
						if (key.equalsIgnoreCase(name)) {
							property = config.getCategory(Configuration.CATEGORY_GENERAL).get(key);
							break;
						}
					}
				} else if (config.getCategory(Configuration.CATEGORY_CLIENT).containsKey(name)) {
					for (String key : config.getCategory(Configuration.CATEGORY_CLIENT).keySet()) {
						if (key.equalsIgnoreCase(name)) {
							property = config.getCategory(Configuration.CATEGORY_CLIENT).get(key);
							break;
						}
					}
				}
				if (property == null) {
					continue;
				}
				if (value.indexOf("[") == 0) {
					value = value.replace("[", "").replace("]", "");
					property.setValidValues(value.split(", "));
				} else if (value.indexOf("{") == 0) {
					value = value.replace("{", "").replace("}", "");
					property.setValidValues(value.split(", "));
				} else {
					property.setValue(value);
				}
			}
			needSave = true;
		}
		if (needSave) {
			config.save();
		}
		resetData();
	}

	public static void sendTo(EntityPlayerMP player) {
		NBTTagCompound compound = new NBTTagCompound();
		for (Field field : data) {
			String key = field.getName();
			try {
				Object value = field.get(CustomNpcs.instance);
				if (value instanceof int[]) { compound.setIntArray(key, (int[]) value); }
				else if (value instanceof Color[]) {
					int[] colors = new int[((Color[]) value).length];
					for (int i = 0; i < colors.length; i++) { colors[i] = ((Color[]) value)[i].getRGB(); }
					compound.setIntArray(key, colors);
				}
				else if (value instanceof Boolean) { compound.setBoolean(key, (boolean) value); }
				else if (value instanceof Integer) { compound.setInteger(key, (int) value); }
				else if (value instanceof Color) { compound.setInteger(key, ((Color) value).getRGB()); }
				else if (value instanceof String) { compound.setString(key, (String) value); }
				else { LogWriter.warn("Custom object "+key+" = "+value.getClass()); }
			}
			catch (Exception ignored) { }
		}
		if (compound.getKeySet().isEmpty()) { return; }
		NBTTagList list = new NBTTagList();
		for (Class<?> cls : ScriptController.forgeEventNames.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Name", ScriptController.forgeEventNames.get(cls));
			nbt.setString("Class", cls.getName());
			list.appendTag(nbt);
		}
		compound.setTag("ForgeEventNames", list);
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.ModData, compound);
	}

	public static void load(NBTTagCompound compound) {
		ScriptController.forgeEventNames.clear();
		for (int i = 0; i < compound.getTagList("ForgeEventNames", 10).tagCount(); i++) {
			NBTTagCompound nbt = compound.getTagList("ForgeEventNames", 10).getCompoundTagAt(i);
			String name = nbt.getString("Name");
			Class<?> cls = null;
			try { cls = Class.forName(nbt.getString("Class")); }
			catch (Exception e) { LogWriter.error(e); }
			ScriptController.forgeEventNames.put(cls, name);
		}
		compound.removeTag("ForgeEventNames");
		for (String key : compound.getKeySet()) {
			Field field = null;
			for (Field f : data) {
				if (f.getName().equals(key)) {
					field = f;
					break;
				}
			}
			if (field == null) { continue; }
			int id = compound.getTag(key).getId();
			try {
				if (id == 1) { field.set(null, compound.getBoolean(key)); }
				else if (id == 3) {
					if (field.getType() == Color.class) { field.set(null, new Color(compound.getInteger(key))); }
					else { field.set(null, compound.getInteger(key)); }
				}
				else if (id == 8) { field.set(null, compound.getString(key)); }
				else if (id == 11) {
					if (field.getType() == Color[].class) {
						int[] arr = compound.getIntArray(key);
						Color[] colors = new Color[arr.length];
						for (int i = 0; i < arr.length; i++) { colors[i] = new Color(arr[i]); }
						field.set(null, colors);
					}
					else { field.set(null, compound.getIntArray(key)); }
				}
			}
			catch (Exception e) { LogWriter.error(e); }
		}
	}

	public List<IConfigElement> getChildElements() {
		Iterator<Property> pI = config.getCategory(Configuration.CATEGORY_GENERAL).getOrderedValues().iterator();
		Map<String, ConfigElement> map = new TreeMap<>();
		while (pI.hasNext()) {
			Property p = pI.next();
			map.put(p.getName(), new ConfigElement(p));
		}
		pI = config.getCategory(Configuration.CATEGORY_CLIENT).getOrderedValues().iterator();
		while (pI.hasNext()) {
			Property p = pI.next();
			map.put(p.getName(), new ConfigElement(p));
		}
		return new ArrayList<>(map.values());
	}

	public void resetConfig() {
		for (Field field : CustomNpcs.class.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ConfigProp.class)) {
				continue;
			}

			ConfigProp prop = field.getAnnotation(ConfigProp.class);
			String name = field.getName();
			ConfigCategory cat = config.getCategory(prop.type());
			Property property = null;
			String classType = field.getType().getTypeName().toLowerCase().replace("integer", "int").replace("[]", "");
			if (classType.lastIndexOf(".") != -1) {
				classType = classType.substring(classType.lastIndexOf(".") + 1);
			}
			Object object = null;
			try {
				object = field.get(null);
			} catch (Exception e) { LogWriter.error(e); }
			Type type = Type.STRING;
			if (field.getType().isArray()) {
				String[] values = null;
				String[] validValues = prop.def().split(",");
                switch (classType) {
                    case "string":
                        values = (String[]) object;
                        break;
                    case "int":
                        type = Type.INTEGER;
                        if (object == null) {
                            values = validValues;
                        } else {
                            int[] vs = (int[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                values[i] = "" + vs[i];
                            }
                        }
                        break;
                    case "boolean":
                        type = Type.BOOLEAN;
                        if (object == null) {
                            values = validValues;
                        } else {
                            boolean[] vs = (boolean[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                values[i] = "" + vs[i];
                            }
                        }
                        break;
                    case "double":
                        type = Type.DOUBLE;
                        if (object == null) {
                            values = validValues;
                        } else {
                            double[] vs = (double[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                values[i] = "" + vs[i];
                            }
                        }
                        break;
                    case "color":
                        type = Type.COLOR;
                        if (object == null) {
                            values = validValues;
                        } else {
                            Color[] vs = (Color[]) object;
                            values = new String[vs.length];
                            for (int i = 0; i < vs.length; i++) {
                                values[i] = Integer.toHexString((vs[i]).getRGB()).toUpperCase();
                                if (vs[i].getAlpha() == 0) {
                                    values[i] += 0xFF000000;
                                }
                            }
                        }
                        break;
                }
				if (values != null) {
					property = new Property(name, values, type, "config." + name + ".key");
					property.setValidValues(validValues);
					if (!prop.min().isEmpty()) {
						if (classType.equals("int")) {
							property.setMinValue(Integer.parseInt(prop.min()));
						} else if (classType.equals("double")) {
							property.setMinValue(Double.parseDouble(prop.min()));
						}
					}
					if (!prop.max().isEmpty()) {
						if (classType.equals("int")) {
							property.setMaxValue(Integer.parseInt(prop.max()));
						} else if (classType.equals("double")) {
							property.setMaxValue(Double.parseDouble(prop.max()));
						}
					}
				}
			} else {
                if (object != null) {
					String value = object.toString();
                    switch (classType) {
                        case "int":
                            type = Type.INTEGER;
                            value = object.toString();
                            break;
                        case "boolean":
                            type = Type.BOOLEAN;
                            value = object.toString();
                            break;
                        case "double":
                            type = Type.DOUBLE;
                            value = object.toString();
                            break;
                        case "color":
                            type = Type.COLOR;
                            value = Integer.toHexString(((Color) object).getRGB()).toUpperCase();
                            break;
                    }
					property = new Property(name, value, type, "config." + name + ".key");
					property.setDefaultValue(prop.def());
					if (!prop.min().isEmpty()) {
						if (classType.equals("int")) {
							property.setMinValue(Integer.parseInt(prop.min()));
						} else if (classType.equals("double")) {
							property.setMinValue(Double.parseDouble(prop.min()));
						}
					}
					if (!prop.max().isEmpty()) {
						if (classType.equals("int")) {
							property.setMaxValue(Integer.parseInt(prop.max()));
						} else if (classType.equals("double")) {
							property.setMaxValue(Double.parseDouble(prop.max()));
						}
					}
				}
			}
			if (property != null) {
				if (!prop.info().isEmpty()) {
					property.setComment(prop.info());
				}
				cat.put(name, property);
			}
		}
		config.save();
	}

	public void resetData() {
		boolean needResetConfig = false;
		for (Field field : CustomNpcs.class.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ConfigProp.class)) { continue; }
            ConfigProp prop = field.getAnnotation(ConfigProp.class);
			if (prop.type().equals(Configuration.CATEGORY_GENERAL)) {data.add(field); }
			String name = field.getName();
			ConfigCategory cat = config.getCategory(prop.type());
			if (!cat.containsKey(name)) {
				continue;
			}
			Property property = cat.get(name);
			Type type = property.getType();
			if (field.getType().isArray()) {
				String[] values = property.getValidValues();
				try {
					if (type == Type.STRING) { field.set(null, values); }
					else if (type == Type.INTEGER) {
						int[] base = (int[]) field.get(null);
						int[] def = null;
						int[] min = null;
						int[] max = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new int[bd.length];
							for (int i = 0; i < bd.length; i++) {
								def[i] = Integer.parseInt(bd[i]);
							}
						}
						if (!prop.min().isEmpty()) {
							String[] bd = prop.min().split(",");
							min = new int[bd.length];
							for (int i = 0; i < bd.length; i++) {
								min[i] = Integer.parseInt(bd[i]);
							}
						}
						if (!prop.max().isEmpty()) {
							String[] bd = prop.max().split(",");
							max = new int[bd.length];
							for (int i = 0; i < bd.length; i++) {
								max[i] = Integer.parseInt(bd[i]);
							}
						}
						if (def != null && base.length != def.length) {
							int[] newBase = new int[def.length];
							for (int i = 0; i < def.length; i++) {
								newBase[i] = i < base.length ? base[i] : def[i];
							}
							base = newBase;
							needResetConfig = true;
						}
						int[] vs = new int[base.length];
						for (int i = 0; i < values.length && i < base.length; i++) {
							vs[i] = Integer.parseInt(values[i]);
							if (min != null && i < min.length && vs[i] < min[i]) {
								vs[i] = min[i];
								needResetConfig = true;
							}
							if (max != null && i < max.length && vs[i] > max[i]) {
								vs[i] = max[i];
								needResetConfig = true;
							}
						}
						field.set(null, vs);
					}
					else if (type == Type.BOOLEAN) {
						boolean[] base = (boolean[]) field.get(null);
						boolean[] def = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new boolean[bd.length];
							for (int i = 0; i < bd.length; i++) {
								def[i] = Boolean.parseBoolean(bd[i]);
							}
						}
						if (def != null && base.length != def.length) {
							boolean[] newBase = new boolean[def.length];
							for (int i = 0; i < def.length; i++) {
								newBase[i] = i < base.length ? base[i] : def[i];
							}
							base = newBase;
							needResetConfig = true;
						}
						boolean[] vs = new boolean[values.length];
						for (int i = 0; i < values.length && i < base.length; i++) {
							vs[i] = Boolean.parseBoolean(values[i]);
						}
						field.set(null, vs);
					}
					else if (type == Type.DOUBLE) {
						double[] base = (double[]) field.get(null);
						double[] def = null;
						double[] min = null;
						double[] max = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new double[bd.length];
							for (int i = 0; i < bd.length; i++) {
								def[i] = Double.parseDouble(bd[i]);
							}
						}
						if (!prop.min().isEmpty()) {
							String[] bd = prop.min().split(",");
							min = new double[bd.length];
							for (int i = 0; i < bd.length; i++) {
								min[i] = Double.parseDouble(bd[i]);
							}
						}
						if (!prop.max().isEmpty()) {
							String[] bd = prop.max().split(",");
							max = new double[bd.length];
							for (int i = 0; i < bd.length; i++) {
								max[i] = Double.parseDouble(bd[i]);
							}
						}
						if (def != null && base.length != def.length) {
							double[] newBase = new double[def.length];
							for (int i = 0; i < def.length; i++) {
								newBase[i] = i < base.length ? base[i] : def[i];
							}
							base = newBase;
							needResetConfig = true;
						}
						double[] vs = new double[values.length];
						for (int i = 0; i < values.length && i < base.length; i++) {
							vs[i] = Double.parseDouble(values[i]);
							if (min != null && i < min.length && vs[i] < min[i]) {
								vs[i] = min[i];
								needResetConfig = true;
							}
							if (max != null && i < max.length && vs[i] > max[i]) {
								vs[i] = max[i];
								needResetConfig = true;
							}
						}
						field.set(null, vs);
					}
					else if (type == Type.COLOR) {
						Color[] base = (Color[]) field.get(null);
						Color[] def = null;
						if (!prop.def().isEmpty()) {
							String[] bd = prop.def().split(",");
							def = new Color[bd.length];
							for (int i = 0; i < bd.length; i++) {
								def[i] = new Color((int) Long.parseLong(bd[i], 16));
							}
						}
						if (def != null && base.length != def.length) {
							Color[] newBase = new Color[def.length];
							for (int i = 0; i < def.length; i++) {
								newBase[i] = i < base.length ? base[i] : def[i];
							}
							base = newBase;
							needResetConfig = true;
						}
						Color[] vs = new Color[values.length];
						for (int i = 0; i < values.length && i < base.length; i++) {
							try {
								vs[i] = new Color(Integer.parseInt(values[i]));
							} catch (Exception e) {
								vs[i] = new Color((int) Long.parseLong(values[i], 16));
								vs[i] = new Color(vs[i].getRed(), vs[i].getGreen(), vs[i].getBlue(), 0);
							}
						}
						field.set(null, vs);
					}
				} catch (Exception e) {
					LogWriter.error("Config Error field \"" + name + "\"; type: " + type, e);
				}
			} else {
				try {
					if (type == Type.STRING) {
						field.set(null, property.getString());
					}
					else if (type == Type.INTEGER) {
						int v = Integer.parseInt(property.getString());
						if (!prop.min().isEmpty()) {
							int m = Integer.parseInt(prop.min());
							if (v < m) {
								v = m;
							}
						}
						if (!prop.max().isEmpty()) {
							int n = Integer.parseInt(prop.max());
							if (v > n) {
								v = n;
							}
						}
						field.set(null, v);
					}
					else if (type == Type.BOOLEAN) {
						field.set(null, Boolean.valueOf(property.getString()));
					}
					else if (type == Type.DOUBLE) {
						double v = Double.parseDouble(property.getString());
						if (!prop.min().isEmpty()) {
							double m = Double.parseDouble(prop.min());
							if (v < m) {
								v = m;
							}
						}
						if (!prop.max().isEmpty()) {
							double n = Double.parseDouble(prop.max());
							if (v > n) {
								v = n;
							}
						}
						field.set(null, v);
					}
					else if (type == Type.COLOR) {
						Color color = new Color((int) Long.parseLong(property.getString(), 16));
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
						field.set(null, color);
					}
				} catch (Exception e) {
					LogWriter.error("Config Error field \"" + name + "\"; type: " + type, e);
				}
			}
		}
		if (needResetConfig) {
			resetConfig();
		}
	}

}
