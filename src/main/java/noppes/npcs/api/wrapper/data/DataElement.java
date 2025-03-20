package noppes.npcs.api.wrapper.data;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

import noppes.npcs.LogWriter;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.api.wrapper.DataObject;
import noppes.npcs.util.Util;

public class DataElement implements IDataElement {

	private final Object data; // parent Object
	private String name;
	private final Object object; // this
	private Class<?> parent; // parent Class

	public DataElement(Object object, Object clazz) {
		this.object = object;
		this.data = clazz;
		this.parent = null;
		if (object instanceof Method) {
			this.name = ((Method) object).getName();
			this.parent = ((Method) object).getDeclaringClass();
		} else if (object instanceof Field) {
			this.name = ((Field) object).getName();
			this.parent = ((Field) object).getDeclaringClass();
		} else if (object instanceof Constructor) {
			this.name = "";
			this.parent = ((Constructor<?>) object).getDeclaringClass();
		} else if (object instanceof Class) {
			this.name = object.getClass().getName();
			this.parent = clazz.getClass();
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (this.object instanceof Method) {
			if (!(object instanceof Method)) {
				return false;
			}
			Method m0 = (Method) this.object;
			Method m1 = (Method) object;
			Parameter[] p0 = m0.getParameters();
			Parameter[] p1 = m1.getParameters();
			if (p0.length != p1.length) {
				return false;
			}
			for (int p = 0; p < p0.length; p++) {
				if (p0[p].getType() != p1[p].getType()) {
					return false;
				}
			}
			return m0.getName().equals(m1.getName()) && m0.getReturnType() == m1.getReturnType()
					&& m0.getDeclaringClass() == m1.getDeclaringClass();
		} else if (this.object instanceof Field) {
			if (!(object instanceof Field)) {
				return false;
			}
			Field f0 = (Field) this.object;
			Field f1 = (Field) object;
			return f0.getName().equals(f1.getName()) && f0.getType() == f1.getType()
					&& f0.getDeclaringClass() == f1.getDeclaringClass();
		} else if (this.object instanceof Constructor) {
			if (!(object instanceof Constructor)) {
				return false;
			}
			Constructor<?> c0 = (Constructor<?>) this.object;
			Constructor<?> c1 = (Constructor<?>) object;
			Parameter[] p0 = c0.getParameters();
			Parameter[] p1 = c1.getParameters();
			if (p0.length != p1.length) {
				return false;
			}
			for (int p = 0; p < p0.length; p++) {
				if (p0[p].getType() != p1[p].getType()) {
					return false;
				}
			}
			return c0.getDeclaringClass() == c1.getDeclaringClass();
		}
		return this.equals(object);
	}

	@Override
	public String getData() {
		String key;
		if (this.object instanceof Method) {
			Method m = (Method) this.object;
			int md = m.getModifiers();
			if (Modifier.isPrivate(md)) { key = "private "; }
			else if (Modifier.isProtected(md)) { key = "protected "; }
			else if (Modifier.isPublic(md)) { key = "public "; }
			else { key = "default "; }
			if (Modifier.isStatic(md)) { key += "static "; }
			if (Modifier.isSynchronized(md)) { key += "synchronized "; }
			if (Modifier.isFinal(md)) { key += "final "; }
			StringBuilder body = new StringBuilder("(");
			for (Parameter p : m.getParameters()) {
				if (!body.toString().equals("(")) { body.append(", "); }
				body.append(p.getType().getName());
			}
			body.append(")");
			key += Util.getAgrName(m.getReturnType()) + " " + m.getName() + body;
			String obfName = DataObject.getObfuscatedName(m.getName());
			if (!obfName.isEmpty()) { key += " {obf_name=\""  + obfName + "\"}"; }
		}
		else if (this.object instanceof Field) {
			Field f = (Field) this.object;
			int md = f.getModifiers();
			if (Modifier.isPrivate(md)) { key = "private "; }
			else if (Modifier.isProtected(md)) { key = "protected "; }
			else if (Modifier.isPublic(md)) { key = "public "; }
			else { key = "default "; }
			if (Modifier.isStatic(md)) { key += "static "; }
			if (Modifier.isFinal(md)) { key += "final "; }
			key += Util.getAgrName(f.getType()) + " " + f.getName();
			String obfName = DataObject.getObfuscatedName(f.getName());
			if (!obfName.isEmpty()) { key += " {obf_name=\""  + obfName + "\"}"; }
			f.setAccessible(true);
		}
		else if (this.object instanceof Constructor) {
			StringBuilder body = new StringBuilder("(");
			int md = ((Constructor<?>) this.object).getModifiers();
			if (Modifier.isPrivate(md)) { key = "private "; }
			else if (Modifier.isProtected(md)) { key = "protected "; }
			else if (Modifier.isPublic(md)) { key = "public "; }
			else { key = "default "; }
			Class<?>[] params = ((Constructor<?>) this.object).getParameterTypes();
			for (Class<?> p : params) {
				if (!body.toString().equals("(")) { body.append(", "); }
				body.append(Util.getAgrName(p));
			}
			body.append(")");
			key += data.getClass().getSimpleName() + body;
		}
		else if (this.object instanceof Class) {
			int md = ((Class<?>) this.object).getModifiers();
			if (Modifier.isPrivate(md)) { key = "private "; }
			else if (Modifier.isProtected(md)) { key = "protected "; }
			else if (Modifier.isPublic(md)) { key = "public "; }
			else { key = "default "; }
			key += Util.getAgrName((Class<?>) this.object);
		}
		else { key = this.data.toString(); }
		return key;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object getObject() {
		return this.object;
	}

	@Override
	public Class<?> getParent() {
		try {
			return parent != null ? parent : Class.forName("java.lang.Object");
		} catch (ClassNotFoundException e) { LogWriter.error("Error:", e); }
		return null;
	}

	@Override
	public int getType() {
		if (this.object instanceof Constructor) {
			return 0;
		}
		if (this.object instanceof Class) {
			return 1;
		}
		if (this.object instanceof Field) {
			return 2;
		}
		if (this.object instanceof Method) {
			return 3;
		}
		return -1;
	}

	@Override
	public Object getValue() {
		if (this.object instanceof Method) {
			return ((Method) this.object).getReturnType();
		}
		else if (this.object instanceof Field) {
			if (data.getClass() == Class.class) { return null; }
			try {
				Field field = (Field) object;
				field.setAccessible(true);
				Object obj = field.get(data);
				if (obj == null) { return "null"; }
				String value = obj.toString();
				if (obj.getClass().isArray()) {
					Class<?> ct = obj.getClass().getComponentType();
					String key = Util.getAgrName(ct);
					return key + "[]" + value.substring(value.indexOf("@"));
				}
				else if (obj instanceof List || obj instanceof Map) {
					return Util.getAgrName(obj.getClass()) + value;
				}
				return value;
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		return this.object;
	}

	@Override
	public Object invoke(Object[] values) {
		if (this.object instanceof Method) {
			((Method) this.object).setAccessible(true);
			try {
				((Method) this.object).invoke(this.data, values);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		return null;
	}

	@Override
	public boolean isBelong(Class<?> cz) {
		if (parent == null) { return false; }
		return parent.isAssignableFrom(cz);
	}

	@Override
	public boolean setValue(Object value) {
		if (this.object instanceof Field) {
			Field f = ((Field) this.object);
			int mod = f.getModifiers();
			if (Modifier.isFinal(mod)) {
				try {
					Field modifiersField = Field.class.getDeclaredField("modifiers");
					modifiersField.setAccessible(true);
					modifiersField.setInt(f, mod - Modifier.FINAL - (Modifier.isPrivate(mod) ? Modifier.PRIVATE : 0));
					f.setAccessible(true);
					f.set(Modifier.isStatic(mod) ? null : data, value);
					modifiersField.setInt(f, mod);
					return true;
				}
				catch (Exception e) {
					LogWriter.error("Error:", e);
					return false;
				}
			}
			try {
				f.setAccessible(true);
				f.set(data, value);
				return true;
			}
			catch (Exception e) {
				throw new CustomNPCsException(e, "Error set value.");
			}
		}
		return false;
	}

}
