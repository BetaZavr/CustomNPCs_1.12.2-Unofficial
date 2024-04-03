package noppes.npcs.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import net.minecraftforge.fml.common.FMLLog;

public class ObfuscationHelper {

	private static boolean checkMethod(Method method, Object[] args) {
		if (args == null || args.length <= 0) {
			return true;
		}
		if (args.length == method.getParameters().length) {
			Parameter[] ps = method.getParameters();
			for (int i = 0; i < ps.length; i++) {
				if (args[i] == null) {
					continue;
				}
				Class<?> clazz;
				if (args[i] instanceof Class) { clazz = (Class<?>) args[i]; }
				else { clazz = args[i].getClass(); }
				
				if (ps[i].getType() != clazz) {
					switch(ps[i].getType().toString().toLowerCase()) {
						case "boolean":
							if (clazz!=Boolean.class) { return false; }
							break;
						case "byte":
							if (clazz!=Byte.class) { return false; }
							break;
						case "short":
							if (clazz!=Short.class) { return false; }
							break;
						case "int":
							if (clazz!=Integer.class) { return false; }
							break;
						case "float":
							if (clazz!=Float.class) { return false; }
							break;
						case "double":
							if (clazz!=Double.class) { return false; }
							break;
						case "long":
							if (clazz!=Long.class) { return false; }
							break;
						default:
							return false;
					}
					
				}
			}
			return true;
		}
		return false;
	}

	public static Field getField(Class<?> clazz, Object type) {
		if (type instanceof Class) {
			Class<?> sc;
			Class<?> cl = (Class<?>) type;
			for (Field t : clazz.getDeclaredFields()) {
				if (t.getType() == cl) { return t; }
				sc = t.getType().getSuperclass();
				if (sc!=null && sc!=t.getType()) {
					while (sc!=null) {
						if (sc == cl) { return t; }
						sc = sc.getSuperclass();
					}
				}
			}
		} else if (type instanceof Integer) {
			int pos = (int) type;
			if (pos >= 0 && pos < clazz.getDeclaredFields().length) { return clazz.getDeclaredFields()[pos]; }
		} else if (type instanceof String) {
			String name = (String) type;
			if (name.isEmpty()) { return null; }
			for (Field t : clazz.getDeclaredFields()) {
				if (t.getName().equals(name)) { return t; }
			}
		}
		return null;
	}

	public static Method getMethod(Class<?> clazz, Object type, Object... args) {
		if (type instanceof Class) {
			Class<?> sc;
			Class<?> cl = (Class<?>) type;
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getReturnType() == cl && ObfuscationHelper.checkMethod(m, args)) { return m; }
				sc = m.getReturnType().getSuperclass();
				if (sc!=null && sc!=m.getReturnType() && ObfuscationHelper.checkMethod(m, args)) {
					while (sc!=null) {
						if (sc == cl) { return m; }
						sc = sc.getSuperclass();
					}
				}
			}
		} else if (type instanceof Integer) {
			int pos = (int) type;
			if (pos >= 0 && pos < clazz.getDeclaredMethods().length && ObfuscationHelper.checkMethod(clazz.getDeclaredMethods()[pos], args)) {
				return clazz.getDeclaredMethods()[pos];
			}
		} else if (type instanceof String) {
			String name = (String) type;
			if (name.isEmpty()) { return null; }
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals(name) && ObfuscationHelper.checkMethod(m, args)) { return m; }
			}
		}
		return null;
	}

	/**
	 * Get the first registered field by its content type Field access is irrelevant
	 * Use when the class has a large number of fields
	 * 
	 * @param clazz
	 *			- class in which the field is registered
	 * @param instance
	 *			- class from which to get the value
	 * @param fieldType
	 *			- class as field content (not interface)
	 * @return - field object
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> T getValue(Class<? super E> clazz, E instance, Class<? super T> fieldType) {
		Field f = null;
		if (fieldType == null) {
			return null;
		}
		try {
			f = ObfuscationHelper.getField(clazz, fieldType);
			if (f != null) {
				f.setAccessible(true);
				return (T) f.get(instance);
			}
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}", fieldType.getSimpleName(), f.getType().getSimpleName(), e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access type {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T, E> T getValue(Class<? super E> clazz, Class<? super E> instance, Class<? super T> fieldType) {
		Field f = null;
		if (fieldType == null) {
			return null;
		}
		try {
			f = ObfuscationHelper.getField(clazz, fieldType);
			if (f != null) {
				f.setAccessible(true);
				return (T) f.get(instance);
			}
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}", fieldType.getSimpleName(), f.getType().getSimpleName(),
						e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access type {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T, E> T getValue(Class<? super E> clazz, int index) {
		Field f = null;
		try {
			f = ObfuscationHelper.getField(clazz, index);
			if (f != null) {
				f.setAccessible(true);
				return (T) f.get(clazz);
			}
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}", "Index:" + index, f.getName(), e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access on {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
		return null;
	}

	/**
	 * Get field value by field registration number Field access is irrelevant Use
	 * if field name would be obfuscated
	 * 
	 * @param clazz
	 *			- class in which the field is registered
	 * @param instance
	 *			- class from which to get the value
	 * @param index
	 *			- field registration number
	 * @return - field object
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> T getValue(Class<? super E> clazz, E instance, int index) {
		Field f = null;
		try {
			f = ObfuscationHelper.getField(clazz, index);
			if (f != null) {
				f.setAccessible(true);
				return (T) f.get(instance);
			}
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}", "Index:" + index, f.getName(), e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access on {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
		return null;
	}

	/**
	 * Get field value by field name Field access is irrelevant Use if the field
	 * name will never be obfuscated
	 * 
	 * @param clazz
	 *			- class in which the field is registered
	 * @param instance
	 *			- class from which to get the value
	 * @param fieldName
	 *			- field name
	 * @return - field object
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> T getValue(Class<? super E> clazz, E instance, String fieldName) {
		Field f = null;
		if (fieldName == null || fieldName.isEmpty()) {
			return null;
		}
		try {
			f = ObfuscationHelper.getField(clazz, fieldName);
			if (f != null) {
				f.setAccessible(true);
				return (T) f.get(instance);
			}
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}", "Name:" + fieldName, f.getType().getSimpleName(), e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access on {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
		return null;
	}

	/**
	 * Tries to call a method by its content return type Method access is irrelevant
	 * Use if the method name will never be obfuscated
	 * 
	 * @param clazz
	 *			- class in which the method is registered
	 * @param instance
	 *			- object from which to call the method
	 * @param methodReturnType
	 *			- class returned by the method (not interface)
	 * @param args
	 *			- specify the parameters that the method accepts. If they are not,
	 *			then you can omit or NULL
	 * @return - object returned by the method
	 */
	public static Object invoke(Class<?> clazz, Object instance, Class<?> methodReturnType, Object... args) {
		Method m = ObfuscationHelper.getMethod(clazz, methodReturnType, args);
		if (m == null) {
			FMLLog.log.info("Unable to locate any metod {} on type {}", methodReturnType.getSimpleName(), clazz.getSimpleName());
			return null;
		}
		m.setAccessible(true);
		try {
			return m.invoke(instance, args);
		} catch (IllegalAccessException e) {
			FMLLog.log.error("Unable to access any metod {} on class {}", methodReturnType.getSimpleName(), clazz.getSimpleName(), e);
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		} catch (IllegalArgumentException e) {
			if (m != null) {
				FMLLog.log.error("Method type mismatch {} on {}", methodReturnType.getSimpleName(),
						m.getReturnType().getSimpleName(), e);
			}
			throw e;
		} catch (InvocationTargetException e) {
			if (m != null) {
				FMLLog.log.error("Type mismatch of one of the parameters of Method {}",
						methodReturnType.getSimpleName(), e);
			}
			try {
				throw e;
			} catch (InvocationTargetException e1) {
			}
		}
		return null;
	}

	/**
	 * Tries to call a method by method registration number Method access is
	 * irrelevant Use if the method name will never be obfuscated
	 * 
	 * @param clazz
	 *			- class in which the method is registered
	 * @param instance
	 *			- object from which to call the method
	 * @param index
	 *			- method registration number
	 * @param args
	 *			- specify the parameters that the method accepts. If they are not,
	 *			then you can omit or NULL
	 * @return - object returned by the method
	 */
	public static Object invoke(Class<?> clazz, Object instance, int index, Object... args) {
		Method m = ObfuscationHelper.getMethod(clazz, index, args);
		if (m == null) {
			FMLLog.log.info("Unable to locate any metod {} on type {}", "Index:" + index, clazz.getSimpleName());
			return null;
		}
		m.setAccessible(true);
		try {
			return m.invoke(instance, args);
		} catch (IllegalAccessException e) {
			FMLLog.log.error("Unable to access any metod {} on class {}", "Index:" + index, clazz.getSimpleName(), e);
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		} catch (IllegalArgumentException e) {
			if (m != null) {
				FMLLog.log.error("Method type mismatch {} on {}", "Index:" + index, m.getReturnType().getSimpleName(),
						e);
			}
			throw e;
		} catch (InvocationTargetException e) {
			if (m != null) {
				FMLLog.log.error("Type mismatch of one of the parameters of Method {}", "Index:" + index, e);
			}
			try {
				throw e;
			} catch (InvocationTargetException e1) {
			}
		}
		return null;
	}

	/**
	 * Tries to call a method by method name Method access is irrelevant Use if the
	 * method name will never be obfuscated
	 * 
	 * @param clazz
	 *			- class in which the method is registered
	 * @param instance
	 *			- object from which to call the method
	 * @param methodName
	 *			- method name
	 * @param args
	 *			- specify the parameters that the method accepts. If they are not,
	 *			then you can omit or NULL
	 * @return - object returned by the method
	 */
	public static Object invoke(Class<?> clazz, Object instance, String methodName, Object... args) {
		Method m = ObfuscationHelper.getMethod(clazz, methodName, args);
		if (m == null) {
			FMLLog.log.info("Unable to locate any metod {} on type {}", "Name:" + methodName, clazz.getSimpleName());
			return null;
		}
		m.setAccessible(true);
		try {
			return m.invoke(instance, args);
		} catch (IllegalAccessException e) {
			FMLLog.log.error("Unable to access any metod {} on class {}", "Name:" + methodName, clazz.getSimpleName(),
					e);
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		} catch (IllegalArgumentException e) {
			if (m != null) {
				FMLLog.log.error("Method type mismatch {} on {}", "Name:" + methodName,
						m.getReturnType().getSimpleName(), e);
			}
			throw e;
		} catch (InvocationTargetException e) {
			if (m != null) {
				FMLLog.log.error("Type mismatch of one of the parameters of Method {}", "Name:" + methodName, e);
			}
			try {
				throw e;
			} catch (InvocationTargetException e1) {
			}
		}
		return null;
	}

	/**
	 * Set the first registered field by its content type Field access is irrelevant
	 * Use when the class has a large number of fields
	 * 
	 * @param clazz
	 *			- class in which the field is registered
	 * @param instance
	 *			- class from which to get the value
	 * @param value
	 *			- set new value
	 * @param fieldType
	 *			- class as field content (not interface)
	 */
	public static <T, E> void setValue(Class<? super T> clazz, T instance, Object value, Class<?> fieldType) {
		if (fieldType == null) {
			return;
		}
		Field f = ObfuscationHelper.getField(clazz, fieldType);
		if (f == null) {
			FMLLog.log.info("Unable to locate any field {} on type {}", "Type:" + fieldType.getSimpleName(),
					clazz.getSimpleName());
			return;
		}
		f.setAccessible(true);
		if (Modifier.isFinal(f.getModifiers())) {
			Field modifiersField;
			try {
				modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(f, f.getModifiers() & ~ Modifier.FINAL );
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				FMLLog.log.error("Failed to change final field state {}", f.getName(), e);
			}
		}
		try {
			f.set(instance, value);
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}.class; value: {}", "Type:" + fieldType.getSimpleName(), f.getType().getSimpleName(), value, e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access on {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
	}

	public static <T, E> void setValue(Class<? super T> clazz, Object value, int index) {
		if (index < 0) { return; }
		Field f = ObfuscationHelper.getField(clazz, index);
		if (f == null) {
			FMLLog.log.info("Unable to locate any field {} on type {}", "Index:" + index, clazz.getSimpleName());
			return;
		}
		f.setAccessible(true);
		if (Modifier.isFinal(f.getModifiers())) {
			Field modifiersField;
			try {
				modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(f, f.getModifiers() & ~ Modifier.FINAL );
				f.set(null, value);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				FMLLog.log.error("Failed to change final field state {}", f.getName(), e);
			}
			return;
		}
		try {
			f.set(clazz, value);
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}.class; value: {}", "Index:" + index, f.getType().getSimpleName(), value, e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access on {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
	}

	/**
	 * Set field value by field registration number Field access is irrelevant Use
	 * if field name would be obfuscated
	 * 
	 * @param clazz
	 *			- class in which the field is registered
	 * @param instance
	 *			- class from which to get the value
	 * @param value
	 *			- set new value
	 * @param index
	 *			- field registration number
	 */
	public static <T, E> void setValue(Class<? super T> clazz, T instance, Object value, int index) {
		if (index < 0) {
			return;
		}
		Field f = ObfuscationHelper.getField(clazz, index);
		if (f == null) {
			FMLLog.log.info("Unable to locate any field {} on type {}", "Index:" + index, clazz.getSimpleName());
			return;
		}
		f.setAccessible(true);
		if (Modifier.isFinal(f.getModifiers())) {
			Field modifiersField;
			try {
				modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(f, f.getModifiers() & ~ Modifier.FINAL );
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				FMLLog.log.error("Failed to change final field state {}", f.getName(), e);
			}
		}
		try {
			f.set(instance, value);
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}.class; value: {}", "Index:" + index, f.getType().getSimpleName(), value, e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access on {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
	}

	/**
	 * Set field value by field name Field access is irrelevant Use if the field
	 * name will never be obfuscated
	 * 
	 * @param clazz
	 *			- class in which the field is registered
	 * @param instance
	 *			- class from which to get the value
	 * @param value
	 *			- set new value
	 * @param fieldName
	 *			- field name
	 */
	public static <T, E> void setValue(Class<? super T> clazz, T instance, Object value, String fieldName) {
		if (fieldName == null || fieldName.isEmpty()) {
			return;
		}
		Field f = ObfuscationHelper.getField(clazz, fieldName);
		if (f == null) {
			FMLLog.log.info("Unable to locate any field {} on type {}", "Name:" + fieldName, clazz.getSimpleName());
			return;
		}
		f.setAccessible(true);
		if (Modifier.isFinal(f.getModifiers())) {
			Field modifiersField;
			try {
				modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(f, f.getModifiers() & ~ Modifier.FINAL );
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				FMLLog.log.error("Failed to change final field state {}", f.getName(), e);
			}
		}
		try {
			f.set(instance, value);
		} catch (IllegalArgumentException e) {
			if (f != null) {
				FMLLog.log.error("Field type mismatch {} on {}.class; value: {}", "Name:" + fieldName, f.getType().getSimpleName(), value, e);
			}
			throw e;
		} catch (IllegalAccessException e) {
			if (f != null) {
				FMLLog.log.error("Mismatch change field access on {}", f.getName(), e);
			}
			try {
				throw e;
			} catch (IllegalAccessException e1) {
			}
		}
	}

	public static void setStaticValue(Field field, Object newValue) {
		if (field==null) { return; }
		field.setAccessible(true);
		try {
			if (Modifier.isFinal(field.getModifiers())) {
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers() & ~ Modifier.FINAL);
			}
			field.set(null, newValue);
		} catch (NoSuchFieldException | SecurityException e) {
			FMLLog.log.error("Unable to locate field {}", "Name:" + field.getName(), e);
		} catch (IllegalArgumentException e) {
			FMLLog.log.error("Field type mismatch {} on {}.class; value: {}", "Name:" + field.getName(), field.getType().getSimpleName(), newValue, e);
			throw e;
		} catch (IllegalAccessException e) {
			FMLLog.log.error("Mismatch change field access on {}", field.getName(), e);
		}
	}
	
}
