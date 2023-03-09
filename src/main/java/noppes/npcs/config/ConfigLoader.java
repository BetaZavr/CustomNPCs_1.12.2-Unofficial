package noppes.npcs.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.Maps;

import noppes.npcs.LogWriter;

public class ConfigLoader {
	private Class<?> configClass;
	private LinkedList<Field> configFields;
	private File dir;
	private String fileName;
	private boolean updateFile;

	public ConfigLoader(Class<?> clss, File dir, String fileName) {
		this.updateFile = false;
		if (!dir.exists()) {
			dir.mkdir();
		}
		this.dir = dir;
		this.configClass = clss;
		this.configFields = new LinkedList<Field>();
		this.fileName = fileName + ".cfg";
		for (Field field : this.configClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(ConfigProp.class)) {
				this.configFields.add(field);
			}
		}
	}

	public void loadConfig() {
		try {
			File configFile = new File(this.dir, this.fileName);
			HashMap<String, Field> types = new HashMap<String, Field>();
			for (Field field : this.configFields) {
				ConfigProp prop = field.getAnnotation(ConfigProp.class);
				types.put(prop.name().isEmpty() ? field.getName() : prop.name(), field);
			}
			if (configFile.exists()) {
				HashMap<String, Object> properties = this.parseConfig(configFile, types);
				for (String prop2 : properties.keySet()) {
					Field field2 = types.get(prop2);
					Object obj = properties.get(prop2);
					if (!obj.equals(field2.get(null))) { field2.set(null, obj); }
				}
				for (String type : types.keySet()) {
					if (!properties.containsKey(type)) {
						this.updateFile = true;
					}
				}
			} else {
				this.updateFile = true;
			}
		} catch (Exception e) {
			this.updateFile = true;
			LogWriter.except(e);
		}
		if (this.updateFile) {
			this.updateConfig();
		}
		this.updateFile = false;
	}

	private HashMap<String, Object> parseConfig(File file, HashMap<String, Field> types) throws Exception {
		HashMap<String, Object> config = new HashMap<String, Object>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
		String strLine;
		while ((strLine = reader.readLine()) != null) {
			if (!strLine.startsWith("#")) {
				if (strLine.length() == 0) {
					continue;
				}
				int index = strLine.indexOf("=");
				if (index <= 0 || index == strLine.length()) {
					this.updateFile = true;
				} else {
					String name = strLine.substring(0, index);
					String prop = strLine.substring(index + 1);
					if (!types.containsKey(name)) {
						this.updateFile = true;
					} else {
						Object obj = null;
						Class<?> class2 = types.get(name).getType();
						if (class2.isAssignableFrom(String.class)) {
							obj = prop;
						} else if (class2.isAssignableFrom(Integer.TYPE)) {
							obj = Integer.parseInt(prop);
						} else if (class2.isAssignableFrom(Short.TYPE)) {
							obj = Short.parseShort(prop);
						} else if (class2.isAssignableFrom(Byte.TYPE)) {
							obj = Byte.parseByte(prop);
						} else if (class2.isAssignableFrom(Boolean.TYPE)) {
							obj = Boolean.parseBoolean(prop);
						} else if (class2.isAssignableFrom(Float.TYPE)) {
							obj = Float.parseFloat(prop);
						} else if (class2.isAssignableFrom(Double.TYPE)) {
							obj = Double.parseDouble(prop);
						} else if (class2.isArray()) { // New
							String text = prop.replace("[", "").replace("]", "");
							try {
								while (text.indexOf(" ") != -1) { text = text.replace(" ", ""); }
								String[] strArr = text.split(",");
								boolean isDouble = false;
								for (int i = 0; i < strArr.length; i++) {
									if (strArr[i].indexOf('.')!=-1) {
										isDouble = true;
										break;
									}
								}
								if (isDouble) {
									double[] intArr = new double[strArr.length];
									for (int i = 0; i < strArr.length; i++) { intArr[i] = Double.parseDouble(strArr[i]); }
									obj = intArr; // double
								}
								else {
									int[] intArr = new int[strArr.length];
									for (int i = 0; i < strArr.length; i++) { intArr[i] = Integer.parseInt(strArr[i]); }
									obj = intArr; // int
								}
							} catch (NumberFormatException ex) {
								String[] textArr = (prop.replace("{", "").replace("}", "")).split(",");
								for (int i=0; i<textArr.length; i++) {
									String str = textArr[i];
									while(str.charAt(0)==' ') { str = str.substring(1); }
									textArr[i] = str;
								}
								obj = textArr;
							}
						}
						if (obj == null) {
							continue;
						}
						config.put(name, obj);
					}
				}
			}
		}
		reader.close();
		return config;
	}

	public void updateConfig() {
		File file = new File(this.dir, this.fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			Map<String, String> map = Maps.<String, String>newTreeMap();
			for (Field field : this.configFields) {
				ConfigProp prop = field.getAnnotation(ConfigProp.class);
				String key = prop.name().isEmpty() ? field.getName() : prop.name();
				String value = "";
				if (prop.info().length() != 0) { value = "#" + prop.info() + System.getProperty("line.separator"); }
				
				if (field.getType().isArray()) { // New
					String text = "[";
					boolean nextTry = false;
					try {
						double[] doubls = (double[]) field.get(null);
						for (int i = 0; i < doubls.length; i++) {
							text += "" + doubls[i];
							if (i < doubls.length - 1) { text += ", "; }
						}
						text += "]";
					}
					catch (ClassCastException | IllegalArgumentException | IllegalAccessException e) { nextTry = true; }
					if (nextTry) {
						nextTry = false;
						try {
							int[] ints = (int[]) field.get(null);
							for (int i = 0; i < ints.length; i++) {
								text += "" + ints[i];
								if (i < ints.length - 1) { text += ", "; }
							}
							text += "]";
						}
						catch (ClassCastException | IllegalArgumentException | IllegalAccessException e) { nextTry = true; }
					}
					if (nextTry) {
						nextTry = false;
						try {
							text = "{";
							String[] strings = (String[]) field.get(null);
							for (int i = 0; i < strings.length; i++) {
								text += "" + strings[i];
								if (i < strings.length - 1) {
									text += ", ";
								}
							}
							text += "}";
						}
						catch (ClassCastException | IllegalArgumentException | IllegalAccessException e) { e.printStackTrace(); continue; }
					}
					value += key + "=" + text + System.getProperty("line.separator");
				} else {
					try { value += key + "=" + field.get(null).toString() + System.getProperty("line.separator"); }
					catch (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace(); }
				}
				map.put(key, value);
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (String value : map.values()) {
				out.write(value);
				out.write(System.getProperty("line.separator"));
			}
			out.close();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}
}
