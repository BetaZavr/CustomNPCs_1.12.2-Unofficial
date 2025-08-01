package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.LogWriter;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.util.ValueUtil;

public class DropNbtSet implements IDropNbtSet {

	public double chance;
	private final DropSet parent;
	public String path;
	public int type;
	public int typeList;
	String[] values;

	public DropNbtSet(DropSet ds) {
		this.parent = ds;
		this.path = "";
		this.values = new String[0];
		this.type = 0;
		this.typeList = 0;
		this.chance = 100.0d;
	}

	public String checkValue(String value, int type) {
		switch (type) {
		case 0: { // boolean
			try {
				boolean b = Boolean.parseBoolean(value);
				return String.valueOf(b);
			}
			catch (Exception e) { LogWriter.error(e); }
			break;
		}
		case 1: { // byte
			try {
				byte b = Byte.parseByte(value);
				return String.valueOf(b);
			} catch (Exception e) { LogWriter.error(e); }
			break;
		}
		case 2: { // short
			try {
				short s = Short.parseShort(value);
				return String.valueOf(s);
			} catch (Exception e) { LogWriter.error(e); }
			break;
		}
		case 3: { // integer
			try {
				int b = Integer.parseInt(value);
				return String.valueOf(b);
			} catch (Exception e) { LogWriter.error(e); }
			break;
		}
		case 4: { // long
			try {
				long l = Long.parseLong(value);
				return String.valueOf(l);
			} catch (Exception e) { LogWriter.error(e); }
			break;
		}
		case 5: { // float
			try {
				float f = Float.parseFloat(value);
				return String.valueOf(f);
			} catch (Exception e) { LogWriter.error(e); }
			break;
		}
		case 6: { // double
			try {
				double d = Double.parseDouble(value);
				return String.valueOf(d);
			} catch (Exception e) { LogWriter.error(e); }
			break;
		}
		case 7: { // byte array
			String[] br = value.split(",");
			StringBuilder text = new StringBuilder();
			for (String str : br) {
				try {
					byte b = Byte.parseByte(str);
					if (text.length() > 0) {
						text.append(",");
					}
					text.append(String.valueOf(b));
				} catch (Exception e) { LogWriter.error(e); }
			}
			if (text.length() > 0) {
				return text.toString();
			}
			break;
		}
		case 8: { // string
			return value;
		}
		case 9: { // list
			String[] br = value.split(",");
			StringBuilder text = new StringBuilder();
			for (String str : br) {
				try {
					String sc = checkValue(str, this.typeList);
					if (sc != null) {
						if (text.length() > 0) {
							text.append(",");
						}
						text.append(sc);
					}
				} catch (Exception e) { LogWriter.error(e); }
			}
			if (text.length() > 0) {
				return text.toString();
			}
			break;
		}
		case 11: { // integer array
			String[] br = value.split(",");
			StringBuilder text = new StringBuilder();
			for (String str : br) {
				try {
					int i = Integer.parseInt(str);
					if (text.length() > 0) {
						if (type == this.type) {
							text.append(",");
						}
						{
							text.append(";");
						}
					}
					text.append(i);
				} catch (Exception e) { LogWriter.error(e); }
			}
			if (text.length() > 0) {
				return text.toString();
			}
			break;
		}
		}
		return null;
	}

	@Override
	public double getChance() {
		return Math.round(this.chance * 10000.0d) / 10000.0d;
	}

	@Override
	public INbt getConstructoredTag(INbt nbt) {
		NBTTagCompound pos = nbt.getMCNBT();
		String key = this.path;
		if (this.path.contains(".")) {
			String keyName;
			while (key.contains(".")) {
				keyName = key.substring(0, key.indexOf("."));
				if (!pos.hasKey(keyName, 10)) {
					pos.setTag(keyName, new NBTTagCompound());
				}
				pos = pos.getCompoundTag(keyName);
				key = key.substring(key.indexOf(".") + 1);
			}
		}
		int idx = (int) ((double) this.values.length * Math.random());
		if (idx >= this.values.length) {
			idx = this.values.length - 1;
		}
		String value = this.values[idx];
		switch (this.type) {
		case 0: { // boolean
			pos.setBoolean(key, Boolean.parseBoolean(value));
			break;
		}
		case 1: { // byte
			pos.setByte(key, Byte.parseByte(value));
			break;
		}
		case 2: { // short
			pos.setShort(key, Short.parseShort(value));
			break;
		}
		case 3: { // integer
			pos.setInteger(key, Integer.parseInt(value));
			break;
		}
		case 4: { // long
			pos.setLong(key, Long.parseLong(value));
			break;
		}
		case 5: { // float
			pos.setFloat(key, Float.parseFloat(value));
			break;
		}
		case 6: { // double
			pos.setDouble(key, Double.parseDouble(value));
			break;
		}
		case 7: { // byte array
			String[] brs = value.split(",");
			byte[] br = new byte[brs.length];
			for (int i = 0; i < brs.length; i++) {
				br[i] = Byte.parseByte(brs[i]);
			}
			pos.setByteArray(key, br);
			break;
		}
		case 8: { // string
			pos.setString(key, value);
			break;
		}
		case 9: { // list
			String[] brs = value.split(",");
			NBTTagList list = new NBTTagList();
            for (String br : brs) {
                if (this.typeList == 3) {
                    list.appendTag(new NBTTagInt(Integer.parseInt(br)));
                } else if (this.typeList == 5) {
                    list.appendTag(new NBTTagFloat(Float.parseFloat(br)));
                } else if (this.typeList == 6) {
                    list.appendTag(new NBTTagDouble(Double.parseDouble(br)));
                } else if (this.typeList == 8) {
                    list.appendTag(new NBTTagString(br));
                } else if (this.typeList == 11) {
                    String[] ints = br.split(";");
                    int[] is = new int[ints.length];
                    for (int j = 0; j < ints.length; j++) {
                        is[j] = Integer.parseInt(ints[j]);
                    }
                    list.appendTag(new NBTTagIntArray(is));
                }
            }
			pos.setTag(key, list);
			break;
		}
		case 11: { // integer array
			String[] ints = value.split(",");
			int[] is = new int[ints.length];
			for (int i = 0; i < ints.length; i++) {
				is[i] = Integer.parseInt(ints[i]);
			}
			pos.setIntArray(key, is);
			break;
		}
		}
		return new NBTWrapper(pos);
	}

	public String getKey() {
		String keyName = "";
		char c = ((char) 167);
		double ch = Math.round(this.chance * 10.0d) / 10.d;
		String chance = String.valueOf(ch).replace(".", ",");
		if (ch == (int) ch) {
			chance = String.valueOf((int) ch);
		}
		chance += "%";
		keyName += c + "e" + chance;

		String key = this.path;
		if (this.path.contains(".")) {
			List<String> keys = new ArrayList<>();
			String preKey = "";
			while (key.contains(".")) {
				preKey = key.substring(0, key.indexOf("."));
				keys.add(preKey);
				key = key.substring(key.indexOf(".") + 1);
			}
			String lastKey = key;
			keys.add(key);
			key = preKey + "." + lastKey;
			if (keys.size() > 2) {
				key = "..." + key;
			}
		}
		keyName += c + "r " + key;
		if (this.values.length == 0) {
			keyName += c + "b=" + c + "7|" + c + "cNULL" + c + "7|";
		} else if (this.values.length == 1) {
			keyName += c + "b=" + c + "7|" + c + "r" + this.values[0] + c + "7|";
		} else {
			keyName += c + "b=" + c + "7|" + c + "6" + this.values.length + c + "7|";
		}
		keyName += c + "8 #" + this.toString().substring(this.toString().indexOf("@") + 1);
		return keyName;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtDS = new NBTTagCompound();
		nbtDS.setInteger("Type", this.type);
		nbtDS.setInteger("TypeList", this.typeList);
		nbtDS.setString("Path", this.path);
		nbtDS.setDouble("Chance", this.chance);
		NBTTagList vs = new NBTTagList();
		for (String s : this.values) {
			if (s != null) {
				vs.appendTag(new NBTTagString(s));
			}
		}
		nbtDS.setTag("Values", vs);
		return nbtDS;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public int getTypeList() {
		return this.typeList;
	}

	@Override
	public String[] getValues() {
		return this.values;
	}

	public void load(NBTTagCompound nbtDS) {
		this.type = nbtDS.getInteger("Type");
		this.typeList = nbtDS.getInteger("TypeList");
		this.path = nbtDS.getString("Path");
		this.chance = nbtDS.getDouble("Chance");
		String[] vs = new String[nbtDS.getTagList("Values", 8).tagCount()];
		for (int i = 0; i < nbtDS.getTagList("Values", 8).tagCount(); i++) {
			String ch = checkValue(nbtDS.getTagList("Values", 8).getStringTagAt(i), this.type);
			if (ch != null) {
				vs[i] = ch;
			}
		}
		this.values = vs;
	}

	@Override
	public void remove() {
		this.parent.removeDropNbt(this);
	}

	@Override
	public void setChance(double chance) {
		double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void setType(int type) {
		if (type == 0 || type == 1 || type == 2 || type == 3 || type == 4 || type == 5 || type == 6 || type == 7
				|| type == 8 || type == 9 || type == 11) {
			this.type = type;
		}
	}

	@Override
	public void setTypeList(int type) {
		if (type == 3 || type == 5 || type == 6 || type == 8 || type == 11) {
			this.typeList = type;
		}
	}

	@Override
	public void setValues(String values) {
		if (values.contains("|")) {
			List<String> nal = new ArrayList<>();
			while (values.contains("|")) {
				String key = checkValue(values.substring(0, values.indexOf("|")), this.type);
				if (key != null) {
					nal.add(key);
				}
				values = values.substring(values.indexOf("|") + 1);
			}
			nal.add(values);
			String[] svs = new String[nal.size()];
			for (int i = 0; i < nal.size(); i++) {
				svs[i] = nal.get(i);
			}
			this.values = svs;
		} else {
			String ch = checkValue(values, this.type);
			if (ch != null) {
				this.values = new String[] { ch };
			}
		}
	}

	@Override
	public void setValues(String[] values) {
		List<String> nal = new ArrayList<>();
		for (String str : values) {
			String key = checkValue(str, this.type);
			if (key != null) {
				nal.add(key);
			}
		}
		String[] svs = new String[nal.size()];
		for (int i = 0; i < nal.size(); i++) {
			svs[i] = nal.get(i);
		}
		this.values = svs;
	}
}
