package noppes.npcs.api.wrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.util.Util;

public class DataObject implements IDataObject {

	public List<IDataElement> data;
	public Object object;

	public DataObject(Object obj) {
		this.object = obj;
		this.data = Util.instance.getClassData(obj, false, true);
	}

	@Override
	public String get() {
		StringBuilder builder = new StringBuilder();
		String enter = new String(Character.toChars(0xA));
		builder.append("Class \"").append(this.object).append("\":");
		IDataElement[] cs = this.getClasses();
		if (cs.length > 0) {
			List<String> list = Lists.newArrayList();
			for (IDataElement ide : cs) {
				list.add("" + ide.getValue());
			}
			Collections.sort(list);
			int i = 0;
			builder.append(enter).append("Classes:[");
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
			List<String> list = Lists.newArrayList();
			for (IDataElement ide : fs) {
				list.add(((Field) ide.getObject()).getName());
			}
			Collections.sort(list);
			int i = 0;
			builder.append(enter).append("Fields:[");
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
			List<String> list = Lists.newArrayList();
			for (IDataElement ide : ms) {
				list.add(((Method) ide.getObject()).getName());
			}
			Collections.sort(list);
			int i = 0;
			builder.append(enter).append("Methods:[");
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
		List<IDataElement> c = Lists.newArrayList();
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
		List<IDataElement> c = Lists.newArrayList();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Class) {
				c.add(de);
			}
		}
		int i = 0;
		if (!c.isEmpty()) {
			String enter = new String(Character.toChars(0xA));
			builder.append("Classes: [").append(enter);
			StringBuilder sp = new StringBuilder(" ");
			for (int j = 0; j < String.valueOf(c.size()).length() - String.valueOf(i).length(); j++) {
				sp.append(" ");
			}
			for (IDataElement de : c) {
				builder.append(i).append(sp).append(de.getData()).append(enter);
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
		List<IDataElement> c = Lists.newArrayList();
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
		List<IDataElement> c = Lists.newArrayList();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Constructor) {
				c.add(de);
			}
		}
		int i = 0;
		if (!c.isEmpty()) {
			String enter = new String(Character.toChars(0xA));
			builder.append("Constructors: [").append(enter);
			StringBuilder sp = new StringBuilder(" ");
			for (int j = 0; j < String.valueOf(c.size()).length() - String.valueOf(i).length(); j++) {
				sp.append(" ");
			}
			for (IDataElement de : c) {
				builder.append(i).append(sp).append(de.getData()).append(enter);
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
		List<IDataElement> f = Lists.newArrayList();
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
		Map<String, IDataElement> f = Maps.newHashMap();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Field) {
				f.put(de.getName(), de);
			}
		}
		int i = 0;
		if (!f.isEmpty()) {
			String enter = new String(Character.toChars(0xA));
			List<String> names = Lists.newArrayList(f.keySet());
			Collections.sort(names);
			String maxKey = "", maxValue = "";
			for (IDataElement de : f.values()) {
				if (de.getData().length() > maxKey.length()) {
					maxKey = de.getData();
				}
				if (("" + de.getValue()).length() > maxValue.length()) {
					maxValue = "" + de.getValue();
				}
			}
            builder.append("Fields: [").append(enter);
			for (String name : names) {
				IDataElement de = f.get(name);
				StringBuilder sp = new StringBuilder(" ");
                StringBuilder fx = new StringBuilder();
                StringBuilder sx = new StringBuilder();
                for (int j = 0; j < String.valueOf(names.size()).length() - String.valueOf(i).length(); j++) {
					sp.append(" ");
				}
				for (int j = 0; j < maxKey.length() - de.getData().length(); j++) {
					fx.append(" ");
				}
				for (int j = 0; j < maxValue.length() - ("" + de.getValue()).length(); j++) {
					sx.append(" ");
				}
				builder.append(i).append(sp).append(de.getData()).append(fx).append(" = ").append(de.getValue()).append(!de.isBelong(this.object.getClass()) ? sx + " [" + de.getParent().getName() + "]" : "").append(enter);
				i++;
			}
			builder.append("]");
		}
		return builder.toString();
	}

	@Override
	public String getInfo() {
		StringBuilder builder = new StringBuilder();
		int md = this.object.getClass().getModifiers();
		String key = "", enter = new String(Character.toChars(0xA));
		if (Modifier.isPrivate(md)) {
			key = "Private ";
		} else if (Modifier.isProtected(md)) {
			key = "Protected ";
		} else if (Modifier.isPublic(md)) {
			key = "Public ";
		}
		if (Modifier.isAbstract(md)) {
			key += "Abstract";
		}
		if (Modifier.isInterface(md)) {
			key += "Interface";
		}
		builder.append(key).append("Class: \"").append(this.object.getClass().getName()).append("\"; value = ").append(this.object).append(enter);
		// Constructors
		builder.append(this.getConstructorsInfo()).append(enter);
		// Classes
		builder.append(this.getClassesInfo()).append(enter);
		// Fields
		builder.append(this.getFieldsInfo()).append(enter);
		// Methods
		builder.append(this.getMethodsInfo());
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
		List<IDataElement> m = Lists.newArrayList();
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
		Map<String, IDataElement> m = Maps.newHashMap();
		for (IDataElement de : this.data) {
			if (de.getObject() instanceof Method) {
				m.put(de.getName(), de);
			}
		}
		int i;
		if (!m.isEmpty()) {
			String enter = new String(Character.toChars(0xA));
			List<String> names = Lists.newArrayList(m.keySet());
			Collections.sort(names);
			String maxKey = "", maxValue = "";
			for (IDataElement de : m.values()) {
				if (de.getData().length() > maxKey.length()) {
					maxKey = de.getData();
				}
				if (("" + de.getValue()).length() > maxValue.length()) {
					maxValue = "" + de.getValue();
				}
			}
			i = 0;
			builder.append("Methods: [").append(enter);
			for (String name : names) {
				IDataElement de = m.get(name);
				StringBuilder sp = new StringBuilder(" ");
                StringBuilder fx = new StringBuilder();
                StringBuilder sx = new StringBuilder();
                for (int j = 0; j < String.valueOf(names.size()).length() - String.valueOf(i).length(); j++) {
					sp.append(" ");
				}
				for (int j = 0; j < maxKey.length() - de.getData().length(); j++) {
					fx.append(" ");
				}
				for (int j = 0; j < maxValue.length() - ("" + de.getValue()).length(); j++) {
					sx.append(" ");
				}
				builder.append(i).append(sp).append(de.getData()).append(fx).append(" = ").append(de.getValue()).append(!de.isBelong(this.object.getClass()) ? sx + " [" + de.getParent().getName() + "]" : "").append(enter);
				i++;
			}
			builder.append("]");
		}
		return builder.toString();
	}

}
