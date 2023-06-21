package noppes.npcs.api.wrapper.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import noppes.npcs.api.handler.data.IDataElement;

public class DataElement
implements IDataElement {

	private Object data; // parent Object
	private String name;
	private Object object; // this
	private Class<?> parent = null; // parent Class

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
		String key = "";
		if (this.object instanceof Method) {
			Method m = (Method) this.object;
			String body = "(";
			for (Parameter p : m.getParameters()) {
				if (!body.equals("(")) {
					body += ", ";
				}
				body += p;
			}
			int md = m.getModifiers();
			key = m.getName() + body + ") ";
			if (Modifier.isPrivate(md)) {
				key += "private ";
			} else if (Modifier.isProtected(md)) {
				key += "protected ";
			} else if (Modifier.isPublic(md)) {
				key += "public ";
			} else {
				key += "default ";
			}
			if (Modifier.isStatic(md)) {
				key += "static ";
			}
			if (Modifier.isSynchronized(md)) {
				key += "synchronized ";
			}
			if (Modifier.isFinal(md)) {
				key += "final ";
			}
		} else if (this.object instanceof Field) {
			Field f = (Field) this.object;
			int md = f.getModifiers();
			key = f.getName() + " (" + f.getType().getName() + ") ";
			if (Modifier.isPrivate(md)) {
				key += "private ";
			} else if (Modifier.isProtected(md)) {
				key += "protected ";
			} else if (Modifier.isPublic(md)) {
				key += "public ";
			} else {
				key += "default ";
			}
			f.setAccessible(true);
			if (Modifier.isStatic(md)) {
				key += "static ";
			}
			if (Modifier.isFinal(md)) {
				key += "final ";
			}
		} else if (this.object instanceof Constructor) {
			String body = "(";
			for (Parameter p : ((Constructor<?>) this.object).getParameters()) {
				if (!body.equals("(")) {
					body += ", ";
				}
				body += p;
			}
			key = this.data.getClass().getSimpleName() + body + ")";
		} else if (this.object instanceof Class) {
			key = ((Class<?>) this.object).getName();
		} else {
			key = this.data.toString();
		}
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
			return this.parent != null ? this.parent : Class.forName("java.lang.Object");
		} catch (ClassNotFoundException e) {
		}
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
				return ((Field) this.object).get(this.data);
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		} else if (this.object instanceof Class) {
			return ((Class<?>) this.object).getSimpleName();
		}
		return this.object;
	}

	@Override
	public Object invoke(Object[] values) {
		if (this.object instanceof Method) {
			((Method) this.object).setAccessible(true);
			try {
				((Method) this.object).invoke(this.data, values);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}
		return null;
	}

	@Override
	public boolean isBelong(Class<?> cz) {
		if (this.parent==null) { return false; }
		Class<?> sc = this.parent;
		while(sc.getSuperclass()!=null) {
			if (sc==cz) { return true; }
			sc = sc.getSuperclass();
		}
		return false;
	}

	@Override
	public boolean setValue(Object value) {
		if (this.object instanceof Field) {
			((Field) this.object).setAccessible(true);
			try {
				((Field) this.object).set(this.data, value);
				return true;
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
		return false;
	}
}
