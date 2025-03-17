package noppes.npcs.api.wrapper;

import java.lang.reflect.*;
import java.util.*;

import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class DataObject implements IDataObject {

	private static final Map<String, String> obfuscated = new HashMap<>();

	public static void load() {
		CustomNPCsScheduler.runTack(() -> {
			obfuscated.clear();
			String data = Util.instance.getDataFile("obf.dat");
			if (data.isEmpty()) { return; }
			for (String line : data.split("\n")) {
				if (line.contains("=")) {
					String[] d = line.split("=");
					obfuscated.put(d[0], d[1]);
				}
			}
		});
	}

	public static @Nonnull String getObfuscatedName(String name) {
		if (obfuscated.containsKey(name)) { return obfuscated.get(name); }
		return "";
	}

	public List<IDataElement> data;
	public Object object;

	public DataObject(Object obj) {
		this.object = obj;
		this.data = Util.instance.getClassData(obj, false, true);
	}

	@Override
	public String get() {
		StringBuilder builder = new StringBuilder();
		builder.append("Class \"").append(this.object).append("\":");
		IDataElement[] cs = this.getClasses();
		if (cs.length > 0) {
			List<String> list = new ArrayList<>();
			for (IDataElement ide : cs) {
				list.add("" + ide.getValue());
			}
			Collections.sort(list);
			int i = 0;
			builder.append((char) 10).append("Classes:[");
			for (String str : list) {
				builder.append(str);
				if (i < list.size() - 1) {
					builder.append(", ");
				}
				i++;
			}
			builder.append("]");
		}
		IDataElement[] fs = this.getFields();
		if (fs.length > 0) {
			List<String> list = new ArrayList<>();
			for (IDataElement ide : fs) {
				list.add(((Field) ide.getObject()).getName());
			}
			Collections.sort(list);
			int i = 0;
			builder.append((char) 10).append("Fields:[");
			for (String str : list) {
				builder.append(str);
				if (i < list.size() - 1) {
					builder.append(", ");
				}
				i++;
			}
			builder.append("]");
		}
		IDataElement[] ms = this.getMethods();
		if (ms.length > 0) {
			List<String> list = new ArrayList<>();
			for (IDataElement ide : ms) {
				list.add(((Method) ide.getObject()).getName());
			}
			Collections.sort(list);
			int i = 0;
			builder.append((char) 10).append("Methods:[");
			for (String str : list) {
				builder.append(str);
				if (i < list.size() - 1) {
					builder.append(", ");
				}
				i++;
			}
			builder.append("]");
		}
		return builder.toString();
	}

	@Override
	public IDataElement[] getClasses() {
		List<IDataElement> c = new ArrayList<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Class) {
				c.add(de);
			}
		}
		return c.toArray(new IDataElement[0]);
	}

	@Override
	public String getClassesInfo() {
		StringBuilder builder = new StringBuilder();
		List<IDataElement> c = new ArrayList<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Class) {
				c.add(de);
			}
		}
		int i = 0;
		if (!c.isEmpty()) {
			builder.append("Sub-Classes: [").append((char) 10);
			StringBuilder sp = new StringBuilder(" ");
			for (int j = 0; j < String.valueOf(c.size()).length() - String.valueOf(i).length(); j++) {
				sp.append(" ");
			}
			for (IDataElement de : c) {
				builder.append(" ").append(i).append(sp).append(de.getData()).append((char) 10);
				i++;
			}
			builder.append("]");
		}
		return builder.toString();
	}

	@Override
	public IDataElement getClazz(String name) {
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Class && de.getData().equals(name)) {
				return de;
			}
		}
		return null;
	}

	@Override
	public IDataElement[] getConstructors() {
		List<IDataElement> c = new ArrayList<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Constructor) {
				c.add(de);
			}
		}
		return c.toArray(new IDataElement[0]);
	}

	@Override
	public String getConstructorsInfo() {
		StringBuilder builder = new StringBuilder();
		List<IDataElement> c = new ArrayList<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Constructor) {
				c.add(de);
			}
		}
		int i = 0;
		if (!c.isEmpty()) {
			builder.append("Constructors: [").append((char) 10);
			StringBuilder sp = new StringBuilder(" ");
			for (int j = 0; j < String.valueOf(c.size()).length() - String.valueOf(i).length(); j++) {
				sp.append(" ");
			}
			for (IDataElement de : c) {
				builder.append(" ").append(i).append(sp).append(de.getData()).append((char) 10);
				i++;
			}
			builder.append("]");
		}
		return builder.toString();
	}

	@Override
	public IDataElement getField(String name) {
		int pos = -1;
		try {
			pos = Integer.parseInt(name);
		} catch (Exception e) { LogWriter.error("Error:", e); }
		int i = 0;
		for (IDataElement de : this.data) {
			if (i == pos || de.getObject() instanceof Field && de.getName().equals(name)) {
				return de;
			}
			i++;
		}
		return null;
	}

	@Override
	public IDataElement[] getFields() {
		List<IDataElement> f = new ArrayList<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Field) {
				f.add(de);
			}
		}
		return f.toArray(new IDataElement[0]);
	}

	@Override
	public String getFieldsInfo() {
		StringBuilder builder = new StringBuilder();
		Map<String, IDataElement> f = new HashMap<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Field) {
				f.put(de.getName(), de);
			}
		}
		if (!f.isEmpty()) {
			List<String> names = new ArrayList<>(f.keySet());
			Collections.sort(names);
			Map<Integer, String> dataMap = new LinkedHashMap<>();
			Map<Integer, String> ptMap = new LinkedHashMap<>();
			Map<Integer, String> prMap = new LinkedHashMap<>();
			Map<Integer, String> dMap = new LinkedHashMap<>();
			Map<Integer, Object> valueMap = new LinkedHashMap<>();
			String maxKey = "", maxValue = "";
			int i = 0;
			for (IDataElement de : f.values()) {
				String data = de.getData();
				Object value = de.getValue();
				if (data.length() > maxKey.length()) { maxKey = data; }
				if (("" + value).length() > maxValue.length()) { maxValue = "" + value; }
				if (data.startsWith("public")) { dataMap.put(i, data); }
				else if (data.startsWith("protected")) { ptMap.put(i, data); }
				else if (data.startsWith("private")) { prMap.put(i, data); }
				else { dMap.put(i, data); }
				valueMap.put(i, value);
				i++;
			}
			dataMap.putAll(ptMap);
			dataMap.putAll(prMap);
			dataMap.putAll(dMap);
            builder.append("Fields: [").append((char) 10);
			for (int pos : dataMap.keySet()) {
				StringBuilder sp = new StringBuilder(" ");
                StringBuilder fx = new StringBuilder();
                for (int j = 0; j < String.valueOf(names.size()).length() - String.valueOf(pos).length(); j++) { sp.append(" "); }
				for (int j = 0; j < maxKey.length() - dataMap.get(pos).length(); j++) { fx.append(" "); }
				builder.append(" ").append(pos).append(sp).append(dataMap.get(pos)).append(fx).append(" = ").append(valueMap.get(pos)).append((char) 10);
			}
			builder.append("]");
		}
		return builder.toString();
	}

	@Override
	public String getInfo() {
		StringBuilder builder = new StringBuilder();
		int md = this.object.getClass().getModifiers();
		String key = "";
		if (Modifier.isPrivate(md)) { key = "Private "; }
		else if (Modifier.isPublic(md)) { key = "Public "; }
		else if (Modifier.isProtected(md)) { key = "Protected "; }
		if (Modifier.isAbstract(md)) { key += "Abstract"; }
		if (Modifier.isInterface(md)) { key += "Interface"; }
		builder.append(key).append("Class ").append(object.getClass().getName());
		if (object.getClass().getSuperclass() != null && object.getClass().getSuperclass() != Object.class) {
			builder.append(" extends ").append(Util.getAgrName(object.getClass().getSuperclass()));
		}
		Class<?>[] implementers = object.getClass().getInterfaces();
		if (implementers.length > 0) {
			builder.append(" implements ");
			for (int i = 0; i < implementers.length; i++) {
				builder.append(Util.getAgrName(implementers[i]));
				if (i < implementers.length - 1) { builder.append(", "); }
			}
		}
		builder.append(":").append((char) 10).append("As an object: ").append(object).append((char) 10);
		// Constructors
		String values = getConstructorsInfo();
		if (!values.isEmpty()) { builder.append(values).append((char) 10); }
		// Classes
		values = getClassesInfo();
		if (!values.isEmpty()) { builder.append(values).append((char) 10); }
		// Fields
		values = getFieldsInfo();
		if (!values.isEmpty()) { builder.append(values).append((char) 10); }
		// Methods
		values = getMethodsInfo();
		if (!values.isEmpty()) { builder.append(values); }
		return builder.toString();
	}

	@Override
	public IDataElement getMethod(String name) {
		int pos = -1;
		try {
			pos = Integer.parseInt(name);
		} catch (Exception e) { LogWriter.error("Error:", e); }
		int i = 0;
		for (IDataElement de : this.data) {
			if (i == pos || de.getObject() instanceof Method && de.getName().equals(name)) {
				return de;
			}
			i++;
		}
		return null;
	}

	@Override
	public IDataElement[] getMethods() {
		List<IDataElement> m = new ArrayList<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Method) {
				m.add(de);
			}
		}
		return m.toArray(new IDataElement[0]);
	}

	@Override
	public String getMethodsInfo() {
		StringBuilder builder = new StringBuilder();
		Map<String, IDataElement> m = new HashMap<>();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Method) {
				m.put(de.getName(), de);
			}
		}
		if (!m.isEmpty()) {
			List<String> names = new ArrayList<>(m.keySet());
			Collections.sort(names);
			Map<Integer, String> dataMap = new LinkedHashMap<>();
			Map<Integer, String> ptMap = new LinkedHashMap<>();
			Map<Integer, String> prMap = new LinkedHashMap<>();
			Map<Integer, String> dMap = new LinkedHashMap<>();
			String maxKey = "";
			int i = 0;
			for (IDataElement de : m.values()) {
				String data = de.getData();
				if (data.length() > maxKey.length()) { maxKey = data; }
				if (data.startsWith("public")) { dataMap.put(i++, data); }
				else if (data.startsWith("protected")) { ptMap.put(i++, data); }
				else if (data.startsWith("private")) { prMap.put(i++, data); }
				else { dMap.put(i++, data); }
			}
			dataMap.putAll(ptMap);
			dataMap.putAll(prMap);
			dataMap.putAll(dMap);
			builder.append("Methods: [").append((char) 10);
			for (int pos : dataMap.keySet()) {
				StringBuilder sp = new StringBuilder(" ");
                for (int j = 0; j < String.valueOf(names.size()).length() - String.valueOf(pos).length(); j++) { sp.append(" "); }
				builder.append(" ").append(pos).append(sp).append(dataMap.get(pos)).append((char) 10);
			}
			builder.append("]");
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return getInfo().replaceAll("\\n", System.lineSeparator());
	}

}
