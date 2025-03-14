package noppes.npcs.api.wrapper.data;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

import noppes.npcs.LogWriter;
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
			key += Util.getAgrName(m.getReturnType(), m.getGenericReturnType(), null) + " " + getNameOfForm(m.getName()) + body;
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
			key += Util.getAgrName(f.getType(), f.getGenericType(), null) + " " + getNameOfForm(f.getName());
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
				body.append(Util.getAgrName(p, p.getGenericSuperclass(), null));
			}
			body.append(")");
			key += data.getClass().getSimpleName() + body;
		} else if (this.object instanceof Class) {
			int md = ((Class<?>) this.object).getModifiers();
			if (Modifier.isPrivate(md)) { key = "private "; }
			else if (Modifier.isProtected(md)) { key = "protected "; }
			else if (Modifier.isPublic(md)) { key = "public "; }
			else { key = "default "; }
			key += Util.getAgrName((Class<?>) this.object, ((Class<?>) this.object).getGenericSuperclass(), null);
		} else {
			key = this.data.toString();
		}
		return key;
	}

	private String getNameOfForm(String name) {
		String obfName = DataObject.getObfuscatedName(name);
		return name + (obfName.isEmpty() ? "" : "["  + obfName + "]" );
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
			return this.parent != null ? this.parent : Class.forName("java.lang.Object");
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
		} else if (this.object instanceof Field) {
			((Field) this.object).setAccessible(true);
			try {
				Object obj = ((Field) this.object).get(this.data);
				if (obj == null) { return "null"; }
				String value = obj.toString();
				if (obj.getClass().isArray()) {
					Class<?> ct = obj.getClass().getComponentType();
					String key = Util.getAgrName(ct, ct.getGenericSuperclass(), obj);
					return key + "[]" + value.substring(value.indexOf("@"));
				}
				else if (obj instanceof List || obj instanceof Map) {
					return Util.getAgrName(obj.getClass(), obj.getClass().getGenericSuperclass(), obj) + value;
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
		if (this.parent == null) { return false; }
		Class<?> sc = this.parent;
		while (sc.getSuperclass() != null) {
			if (sc == cz) {
				return true;
			}
			sc = sc.getSuperclass();
		}
		return false;
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
					f.set(Modifier.isStatic(mod) ? null : this.data, value);
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
				if (f.getType() == int.class) {
					int v = (int) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == Integer.class) {
					Integer v = (int) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == float.class) {
					float v = (float) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == Float.class) {
					Float v = (float) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == long.class) {
					long v = (long) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == Long.class) {
					Long v = (long) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == byte.class) {
					byte v = (byte) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == Byte.class) {
					Byte v = (byte) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == short.class) {
					short v = (short) ((double) value);
					f.set(this.data, v);
				}
				else if (f.getType() == Short.class) {
					Short v = (short) ((double) value);
					f.set(this.data, v);
				}
				else { f.set(this.data, value); }
				return true;
			}
			catch (Exception e) {  LogWriter.error("Error:", e); }
		}
		return false;
	}

}
