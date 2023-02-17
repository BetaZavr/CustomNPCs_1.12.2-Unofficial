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
					if (!obj.equals(field2.get(null))) {
						field2.set(null, obj);
					}
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
							while (text.indexOf(" ") != -1) {
								text = text.replace(" ", "");
							}
							String[] strArr = text.split(",");
							int[] intArr = new int[strArr.length];
							for (int i = 0; i < strArr.length; i++) {
								try {
									intArr[i] = Integer.parseInt(strArr[i]);
								} catch (NumberFormatException ex) {
									intArr[i] = 0;
								}
							}
							obj = intArr;
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
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (Field field : this.configFields) {
				ConfigProp prop = field.getAnnotation(ConfigProp.class);
				if (prop.info().length() != 0) {
					out.write("#" + prop.info() + System.getProperty("line.separator"));
				}
				String name = prop.name().isEmpty() ? field.getName() : prop.name();
				try {
					if (field.getType().isArray()) { // New
						int[] intArr = (int[]) field.get(null);
						String text = "[";
						for (int i = 0; i < intArr.length; i++) {
							text += "" + intArr[i];
							if (i < intArr.length - 1) {
								text += ", ";
							}
						}
						text += "]";
						out.write(name + "=" + text + System.getProperty("line.separator"));
					} else {
						out.write(name + "=" + field.get(null).toString() + System.getProperty("line.separator"));
					}
					out.write(System.getProperty("line.separator"));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e2) {
					e2.printStackTrace();
				}
			}
			out.close();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}
}
