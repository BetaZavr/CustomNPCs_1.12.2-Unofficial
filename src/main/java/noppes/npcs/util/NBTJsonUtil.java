package noppes.npcs.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

	public static class JsonFile {
		private final String original;
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

	@SuppressWarnings("unchecked")
	public static void checkAddedMods(Object o) {
		/*try {
			Map<String, String> dataMap_0 = (Map<String, String>) o.getClass().getDeclaredField("scripts").get(o);
			Map<String, String> dataMap_1 = (Map<String, String>) o.getClass().getDeclaredField("clients").get(o);
			Map<String, String> dataMap_2 = (Map<String, String>) o.getClass().getDeclaredField("languages").get(o);
			for (String u : dataMap_2.keySet()) {
				File t = (File) o.getClass().getDeclaredField("dir").get(o);
				List<File> list = Util.instance.getFiles(t, dataMap_2.get(u).replace(".", ".p"));
				if (!list.isEmpty()) {
					String n = t.toString() + "\\" + u.toLowerCase() + "\\";
					for (File g : list) {
						dataMap_0.replace(g.toString().replace(n, ""), NBTJsonUtil.getData(g, ContainerManageBanks.bank));
					}
				}
				t = (File) o.getClass().getDeclaredField("clientDir").get(o);
				list = Util.instance.getFiles(t, dataMap_2.get(u).replace("" + ((char) 46), ".p"));
				if (list.isEmpty()) {
					String n = t.toString() + "\\" + u.toLowerCase() + "\\";
					for (File g : list) {
						dataMap_1.replace(g.toString().replace(n, ""), NBTJsonUtil.getData(g, ContainerManageBanks.bank));
					}
				}
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }*/
	}

	public static String Convert(NBTTagCompound compound) {
		List<JsonLine> list = new ArrayList<>();
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
		StringBuilder json = new StringBuilder();
		int tab = 0;
		for (JsonLine tag : list) {
			if (tag.reduceTab()) {
				--tab;
			}
			for (int i = 0; i < tab; ++i) {
				json.append("	");
			}
			json.append(tag).append("\n");
			if (tag.increaseTab()) {
				++tab;
			}
		}
		return json.toString();
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

	private static String getData(File agr0, String agr1) {
		/*StringBuilder data = new StringBuilder();
		try {
			String dataOld = Util.instance.loadFile(agr0);
			int i = 0;
			for (int t = 0; t < dataOld.length(); t++) {
				char p = agr1.charAt(i);
				char c = dataOld.charAt(t);
				int f = (int) c - (int) p;
				if (f < 0) {
					f += 0xffff;
				}
				data.append((char)f);
				i++;
				if (i >= agr1.length()) {
					i = 0;
				}
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return data.toString() + ((char) 10);*/
		return null;
	}

	private static List<NBTBase> getListData(NBTTagList list) {
		return ObfuscationHelper.getValue(NBTTagList.class, list, 1);
	}

	public static NBTTagCompound LoadFile(File file) throws IOException, JsonException {
		return Convert(Util.instance.loadFile(file));
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
				String cut = "";
				for (boolean ignore = false; !json.startsWith("\"") || ignore; ignore = cut.equals("\\"), s += cut) {
					cut = json.cutDirty(1);
				}
				json.cut(1);
				return new NBTTagString(s.replace("\\\\", "\\").replace("\\\"", "\""));
			}
			StringBuilder s = new StringBuilder();
			while (!json.startsWith(",", "]", "}")) {
				s.append(json.cut(1));
			}
			s = new StringBuilder(s.toString().trim().toLowerCase());
			if ((s.length() == 0) || s.toString().contains("bytes]")) {
				return null;
			}
			try {
				if (s.toString().endsWith("d")) {
					return new NBTTagDouble(Double.parseDouble(s.substring(0, s.length() - 1)));
				}
				if (s.toString().endsWith("f")) {
					return new NBTTagFloat(Float.parseFloat(s.substring(0, s.length() - 1)));
				}
				if (s.toString().endsWith("b")) {
					return new NBTTagByte(Byte.parseByte(s.substring(0, s.length() - 1)));
				}
				if (s.toString().endsWith("s")) {
					return new NBTTagShort(Short.parseShort(s.substring(0, s.length() - 1)));
				}
				if (s.toString().endsWith("l")) {
					return new NBTTagLong(Long.parseLong(s.substring(0, s.length() - 1)));
				}
				if (s.toString().contains(".")) {
					return new NBTTagDouble(Double.parseDouble(s.toString()));
				}
				return new NBTTagInt(Integer.parseInt(s.toString()));
			} catch (NumberFormatException ex) {
				throw new JsonException("Unable to convert: " + s + " to a number", json);
			}
		}
	}

}
