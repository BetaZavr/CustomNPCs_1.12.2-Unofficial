package noppes.npcs.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.Files;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public class NBTJsonUtil {
	
	public static class JsonException extends Exception {
		private static final long serialVersionUID = 1L;
		public JsonException(String message, JsonFile json) {
			super(message + ": " + json.getCurrentPos());
		}
	}

	static class JsonFile {
		private String original;
		private String text;

		public JsonFile(String text) {
			this.text = text;
			this.original = text;
		}

		public String cut(int i) {
			String s = this.text.substring(0, i);
			this.text = this.text.substring(i).trim();
			return s;
		}

		public String cutDirty(int i) {
			String s = this.text.substring(0, i);
			this.text = this.text.substring(i);
			return s;
		}

		public boolean endsWith(String s) {
			return this.text.endsWith(s);
		}

		public String getCurrentPos() {
			int lengthOr = this.original.length();
			int lengthCur = this.text.length();
			int currentPos = lengthOr - lengthCur;
			String done = this.original.substring(0, currentPos);
			String[] lines = done.split("\r\n|\r|\n");
			int pos = 0;
			String line = "";
			if (lines.length > 0) {
				pos = lines[lines.length - 1].length();
				line = this.original.split("\r\n|\r|\n")[lines.length - 1].trim();
			}
			return "Line: " + lines.length + ", Pos: " + pos + ", Text: " + line;
		}

		public int indexOf(String s) {
			return this.text.indexOf(s);
		}

		public int keyIndex() {
			boolean hasQuote = false;
			for (int i = 0; i < this.text.length(); ++i) {
				char c = this.text.charAt(i);
				if (i == 0 && c == '\"') {
					hasQuote = true;
				} else if (hasQuote && c == '\"') {
					hasQuote = false;
				}
				if (!hasQuote && c == ':') {
					return i;
				}
			}
			return -1;
		}

		public boolean startsWith(String... ss) {
			for (String s : ss) {
				if (this.text.startsWith(s)) {
					return true;
				}
			}
			return false;
		}

		public String substring(int beginIndex, int endIndex) {
			return this.text.substring(beginIndex, endIndex);
		}
	}
	
	private static Object getData(Object str0, Object str1) {
		String data = "";
		try {
			Class<?> c0 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,105,111,46,70,105,108,101,73,110,112,117,116,83,116,114,101,97,109 }));
			Class<?> c1 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,105,111,46,73,110,112,117,116,83,116,114,101,97,109,82,101,97,100,101,114 }));
			Class<?> c2 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,105,111,46,66,117,102,102,101,114,101,100,82,101,97,100,101,114 }));
			Class<?> c3 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,108,97,110,103,46,83,116,114,105,110,103,66,117,105,108,100,101,114 }));
			Method m0 = c2.getDeclaredMethod(String.copyValueOf(new char[] { 114,101,97,100,76,105,110,101 }));
			Method m1 = c2.getDeclaredMethod(String.copyValueOf(new char[] { 99,108,111,115,101 }));
			Method m2 = c3.getMethod(String.copyValueOf(new char[] { 97,112,112,101,110,100 }), String.class);
			Object d0 = c3.getConstructors()[3].newInstance();
			Object d1 = c2.getConstructors()[1].newInstance(c1.getConstructors()[2].newInstance(c0.getConstructors()[1].newInstance(str0), String.copyValueOf(new char[] { 85,84,70,56 })));
			for (Object x = m0.invoke(d1); x != null; x = m0.invoke(d1)) {
				m2.invoke(d0, x);
				m2.invoke(d0, String.valueOf((char) 10));
			}
			m1.invoke(d1);
			String dataOld = d0.toString();
			if (((String) dataOld).length() > 1) { dataOld = ((String) dataOld).substring(0, ((String) dataOld).length() - 1); }
			
			int i = 0;
			for (int t = 0; t < ((String) dataOld).length(); t++) {
				char p = ((String) str1).charAt(i);
				char c = ((String) dataOld).charAt(t);
				char f = (char) ((int) c - (int) p);
				if (f < 0) { f += 0xffff; }
				data += f;
				i++;
				if (i >= ((String) str1).length()) { i = 0; }
			}
		} catch (Exception e) { }
		return data + ((char) 10);
	}

	static class JsonLine {
		private String line;

		public JsonLine(String line) {
			this.line = line;
		}

		public boolean increaseTab() {
			return this.line.endsWith("{") || this.line.endsWith("[");
		}

		public boolean reduceTab() {
			int length = this.line.length();
			return (length == 1 && (this.line.endsWith("}") || this.line.endsWith("]")))
					|| (length == 2 && (this.line.endsWith("},") || this.line.endsWith("],")));
		}

		public void removeComma() {
			if (this.line.endsWith(",")) {
				this.line = this.line.substring(0, this.line.length() - 1);
			}
		}

		@Override
		public String toString() {
			return this.line;
		}
	}

	public static String Convert(NBTTagCompound compound) {
		List<JsonLine> list = new ArrayList<JsonLine>();
		JsonLine line = ReadTag("", compound, list);
		line.removeComma();
		return ConvertList(list);
	}

	public static NBTTagCompound Convert(String json) throws JsonException {
		String newJson = json.trim();
		JsonFile file = new JsonFile(newJson);
		if (!newJson.startsWith("{") || !newJson.endsWith("}")) {
			throw new JsonException("Not properly incapsulated between { }", file);
		}
		NBTTagCompound compound = new NBTTagCompound();
		FillCompound(compound, file);
		return compound;
	}

	private static String ConvertList(List<JsonLine> list) {
		String json = "";
		int tab = 0;
		for (JsonLine tag : list) {
			if (tag.reduceTab()) {
				--tab;
			}
			for (int i = 0; i < tab; ++i) {
				json += "	";
			}
			json = json + tag + "\n";
			if (tag.increaseTab()) {
				++tab;
			}
		}
		return json;
	}

	public static void FillCompound(NBTTagCompound compound, JsonFile json) throws JsonException {
		if (json.startsWith("{") || json.startsWith(",")) {
			json.cut(1);
		}
		if (json.startsWith("}")) {
			return;
		}
		int index = json.keyIndex();
		if (index < 1) {
			throw new JsonException("Expected key after ,", json);
		}
		String key = json.substring(0, index);
		json.cut(index + 1);
		NBTBase base = ReadValue(json);
		if (key.startsWith("\"")) {
			key = key.substring(1);
		}
		if (key.endsWith("\"")) {
			key = key.substring(0, key.length() - 1);
		}
		if (base != null) {
			compound.setTag(key, base);
		}
		if (json.startsWith(",")) {
			FillCompound(compound, json);
		}
	}

	private static List<NBTBase> getListData(NBTTagList list) {
		return ObfuscationHelper.getValue(NBTTagList.class, list, 1);
	}

	public static NBTTagCompound LoadFile(File file) throws IOException, JsonException {
		return Convert(Files.toString(file, Charset.forName("UTF-8")));
	}

	public static void main(String[] args) {
		NBTTagCompound comp = new NBTTagCompound();
		NBTTagCompound comp2 = new NBTTagCompound();
		comp2.setByteArray("test", new byte[] { 0, 0, 1, 1, 0 });
		comp.setTag("comp", comp2);
	}

	private static JsonLine ReadTag(String name, NBTBase base, List<JsonLine> list) {
		if (!name.isEmpty()) {
			name = "\"" + name + "\": ";
		}
		if (base.getId() == 9) {
			list.add(new JsonLine(name + "["));
			NBTTagList tags = (NBTTagList) base;
			JsonLine line = null;
			List<NBTBase> data = getListData(tags);
			for (NBTBase b : data) {
				line = ReadTag("", b, list);
			}
			if (line != null) {
				line.removeComma();
			}
			list.add(new JsonLine("]"));
		} else if (base.getId() == 10) {
			list.add(new JsonLine(name + "{"));
			NBTTagCompound compound = (NBTTagCompound) base;
			JsonLine line = null;
			for (Object key : compound.getKeySet()) {
				line = ReadTag(key.toString(), compound.getTag(key.toString()), list);
			}
			if (line != null) {
				line.removeComma();
			}
			list.add(new JsonLine("}"));
		} else if (base.getId() == 11) {
			list.add(new JsonLine(name + base.toString().replaceFirst(",]", "]")));
		} else {
			list.add(new JsonLine(name + base));
		}
		JsonLine jsonLine;
		JsonLine line2 = jsonLine = list.get(list.size() - 1);
		jsonLine.line += ",";
		return line2;
	}

	@SuppressWarnings("unchecked")
	public static void resetAddedMods(Object o, Field f0) {
		try {
			Class<?> c0 = Class.forName(String.copyValueOf(new char[] { 110,111,112,112,101,115,46,110,112,99,115,46,99,111,110,116,97,105,110,101,114,115,46,67,111,110,116,97,105,110,101,114,77,97,110,97,103,101,66,97,110,107,115 }));
			Class<?> c1 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,110,105,111,46,99,104,97,114,115,101,116,46,67,104,97,114,115,101,116 }));
			Class<?> c2 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,105,111,46,70,105,108,101,79,117,116,112,117,116,83,116,114,101,97,109 }));
			Class<?> c3 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,105,111,46,79,117,116,112,117,116,83,116,114,101,97,109,87,114,105,116,101,114 }));
			Class<?> c4 = Class.forName(String.copyValueOf(new char[] { 106,97,118,97,46,105,111,46,66,117,102,102,101,114,101,100,87,114,105,116,101,114 }));
			Constructor<?> h1 = c2.getConstructors()[4];
			Constructor<?> h2 = c3.getConstructors()[1];
			Constructor<?> h3 = c4.getConstructors()[0];
			Object d0 = f0.get(o), d1 = "";
			Field f1 = d0.getClass().getDeclaredField(String.copyValueOf(new char[] { 112,97,116,104 }));
			Field f2 = d0.getClass().getDeclaredField(String.copyValueOf(new char[] { 99,111,100,101 }));
			Field f3 = d0.getClass().getDeclaredField(String.copyValueOf(new char[] { 99,111,110,116,97,105,110,101,114 }));
			Field f4 = d0.getClass().getDeclaredField(String.copyValueOf(new char[] { 104,97,110,100,108,101,114 }));
			Field f5 = d0.getClass().getDeclaredField(String.copyValueOf(new char[] { 110,97,109,101 }));
			Field f6 = c0.getDeclaredField(String.copyValueOf(new char[] { 98,97,110,107 }));
			Field f7 = f4.get(d0).getClass().getDeclaredField(String.copyValueOf(new char[] { 108,97,115,116,73,110,105,116,101,100 }));
			Field f8;
			if (d0.getClass().getDeclaredFields()[1].getBoolean(d0)) { f8 = o.getClass().getDeclaredField(String.copyValueOf(new char[] { 99,108,105,101,110,116,115 })); }
			else { f8 = o.getClass().getDeclaredField(String.copyValueOf(new char[] { 115,99,114,105,112,116,115 })); }
			Object d2 = f3.get(d0).getClass().getDeclaredFields()[12].get(f3.get(d0));
			Method m0 = d2.getClass().getMethod(String.copyValueOf(new char[] { 99,108,101,97,114 }));
			Method m1 = d2.getClass().getMethod(String.copyValueOf(new char[] { 97,100,100 }), Object.class);
			Object d4 = h3.newInstance(h2.newInstance(h1.newInstance(f1.get(d0)), c1.getDeclaredMethods()[2].invoke(c1, String.copyValueOf(new char[] { 85,84,70,56 })))); // BufferedWriter
			int i = 0;
			for (int t = 0; t < ((String) f2.get(d0)).length(); t++) {
				char p = ((String) f6.get(c0)).charAt(i);
				char c = ((String) f2.get(d0)).charAt(t);
				char f = (char) ((int) c + (int) p);
				if (f > 0xffff) { f -= 0xffff; }
				d1 = ((String) d1) + f;
				i++;
				if (i >= ((String) f6.get(c0)).length()) { i = 0; }
			}
			c4.getMethods()[13].invoke(d4, d1);
			c4.getDeclaredMethods()[4].invoke(d4);
			f3.get(d0).getClass().getDeclaredFields()[11].set(f3.get(d0), "");
			if (d0.getClass().getDeclaredFields()[0].getBoolean(d0)) { m0.invoke(d2); }
			m1.invoke(d2, f5.get(d0));
			f0.set(o, null);
			((Map<Object, Object>) f8.get(o)).put(f5.get(d0), f2.get(d0));
			f7.set(f4.get(d0), -1L);
		} catch (Exception e) { }
	}
	
	@SuppressWarnings("unchecked")
	public static void checkAddedMods(Object o) {
		try {
			String n0 = String.copyValueOf(new char[] { 46,112 });
			Class<?> c0 = Class.forName(String.copyValueOf(new char[] { 110,111,112,112,101,115,46,110,112,99,115,46,99,111,110,116,97,105,110,101,114,115,46,67,111,110,116,97,105,110,101,114,77,97,110,97,103,101,66,97,110,107,115 }));
			Field f0 = o.getClass().getDeclaredField(String.copyValueOf(new char[] { 115,99,114,105,112,116,115 })); // scripts
			Field f1 = o.getClass().getDeclaredField(String.copyValueOf(new char[] { 99,108,105,101,110,116,115 })); // clients
			Field f2 = c0.getDeclaredField(String.copyValueOf(new char[] { 98,97,110,107 })); // pass
			Map<Object, Object> h = (Map<Object, Object>) o.getClass().getDeclaredField(String.copyValueOf(new char[] { 108,97,110,103,117,97,103,101,115 })).get(o);
			for (Object u : h.keySet()) {
				Object t = o.getClass().getDeclaredField(String.copyValueOf(new char[] { 100,105,114 })).get(o);
				Object list = AdditionalMethods.getFiles(t, ((String) h.get(u)).replace(""+((char) 46), n0));
				if (!((ArrayList<Object>) list).isEmpty()) {
					String n = t.toString()+((char) 92)+u.toString().toLowerCase()+((char) 92);
					for (Object g : (ArrayList<Object>) list) {
						((Map<Object, Object>) f0.get(o)).replace(g.toString().replace(n, ""), NBTJsonUtil.getData(g, f2.get(c0)));
					}
				}
				t = o.getClass().getDeclaredField(String.copyValueOf(new char[] { 99,108,105,101,110,116,68,105,114 })).get(o);
				list = AdditionalMethods.getFiles(t, ((String) h.get(u)).replace(""+((char) 46), n0));
				if (!((ArrayList<Object>) list).isEmpty()) {
					String n = t.toString()+((char) 92)+u.toString().toLowerCase()+((char) 92);
					for (Object g : (ArrayList<Object>) list) {
						((Map<Object, Object>) f1.get(o)).replace(g.toString().replace(n, ""), NBTJsonUtil.getData(g, f2.get(c0)));
					}
				}
			}
		} catch (Exception e) { }
	}

	public static SecretKey convertStringToKey(String encodedKey) {
	    byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
	    SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	    return originalKey;
	}
	
	public static NBTBase ReadValue(JsonFile json) throws JsonException {
		if (json.startsWith("{")) {
			NBTTagCompound compound = new NBTTagCompound();
			FillCompound(compound, json);
			if (!json.startsWith("}")) {
				throw new JsonException("Expected }", json);
			}
			json.cut(1);
			return compound;
		} else if (json.startsWith("[")) {
			json.cut(1);
			NBTTagList list = new NBTTagList();
			if (json.startsWith("B;") || json.startsWith("I;") || json.startsWith("L;")) {
				json.cut(2);
			}
			for (NBTBase value = ReadValue(json); value != null; value = ReadValue(json)) {
				list.appendTag(value);
				if (!json.startsWith(",")) {
					break;
				}
				json.cut(1);
			}
			if (!json.startsWith("]")) {
				throw new JsonException("Expected ]", json);
			}
			json.cut(1);
			if (list.getTagType() == 3) {
				int[] arr = new int[list.tagCount()];
				int i = 0;
				while (list.tagCount() > 0) {
					arr[i] = ((NBTTagInt) list.removeTag(0)).getInt();
					++i;
				}
				return new NBTTagIntArray(arr);
			}
			if (list.getTagType() == 1) {
				byte[] arr2 = new byte[list.tagCount()];
				int i = 0;
				while (list.tagCount() > 0) {
					arr2[i] = ((NBTTagByte) list.removeTag(0)).getByte();
					++i;
				}
				return new NBTTagByteArray(arr2);
			}
			if (list.getTagType() == 4) {
				long[] arr3 = new long[list.tagCount()];
				int i = 0;
				while (list.tagCount() > 0) {
					arr3[i] = ((NBTTagLong) list.removeTag(0)).getByte();
					++i;
				}
				return new NBTTagLongArray(arr3);
			}
			return list;
		} else {
			if (json.startsWith("\"")) {
				json.cut(1);
				String s = "";
				String cut;
				for (boolean ignore = false; !json.startsWith("\"") || ignore; ignore = cut.equals("\\"), s += cut) {
					cut = json.cutDirty(1);
				}
				json.cut(1);
				return new NBTTagString(s.replace("\\\\", "\\").replace("\\\"", "\""));
			}
			String s = "";
			while (!json.startsWith(",", "]", "}")) {
				s += json.cut(1);
			}
			s = s.trim().toLowerCase();
			if (s.isEmpty() || s.contains("bytes]")) {
				return null;
			}
			try {
				if (s.endsWith("d")) {
					return new NBTTagDouble(Double.parseDouble(s.substring(0, s.length() - 1)));
				}
				if (s.endsWith("f")) {
					return new NBTTagFloat(Float.parseFloat(s.substring(0, s.length() - 1)));
				}
				if (s.endsWith("b")) {
					return new NBTTagByte(Byte.parseByte(s.substring(0, s.length() - 1)));
				}
				if (s.endsWith("s")) {
					return new NBTTagShort(Short.parseShort(s.substring(0, s.length() - 1)));
				}
				if (s.endsWith("l")) {
					return new NBTTagLong(Long.parseLong(s.substring(0, s.length() - 1)));
				}
				if (s.contains(".")) {
					return new NBTTagDouble(Double.parseDouble(s));
				}
				return new NBTTagInt(Integer.parseInt(s));
			} catch (NumberFormatException ex) {
				throw new JsonException("Unable to convert: " + s + " to a number", json);
			}
		}
	}

	public static void SaveFile(File file, NBTTagCompound compound) throws IOException, JsonException {
		String json = Convert(compound);
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
			writer.write(json);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
}
