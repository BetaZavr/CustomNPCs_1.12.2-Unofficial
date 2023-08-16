package noppes.npcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.NBTJsonUtil.JsonException;

public class TempFile {
	
	private static int maxPart = 30000;
	
	public String name;
	public final Map<Integer, String> data = Maps.<Integer, String>newTreeMap();
	public int fileType; // 0 - simple text, 1 - nbt json, 2 - compressed nbt
	public int saveType; // 0 - temp file, 1 - client script, 2 - normal save
	public int tryLoads;
	public long size;
	public long lastLoad;
	
	public TempFile() {
		this.name = "";
		this.fileType = 0;
		this.saveType = 0;
		this.size = 0;
		this.tryLoads = 0;
		this.lastLoad = System.currentTimeMillis();
	}
	
	public TempFile(File file) {
		this.name = file.getAbsolutePath();
		this.fileType = 0;
		this.saveType = 0;
		this.size = file.length();
		this.tryLoads = 0;
		this.lastLoad = System.currentTimeMillis();
		this.reset(file);
	}
	
	public TempFile(String name, int filetype, int savetype, long size) {
		this.name = name;
		this.fileType = filetype;
		this.saveType = savetype;
		this.size = size;
		this.tryLoads = 0;
		this.lastLoad = System.currentTimeMillis();
	}

	public boolean isLoad() {
		if (this.data.isEmpty()) { return false; }
		for (String str : this.data.values()) { if (str==null || str.isEmpty()) { return false; } }
		return true;
	}

	public String getDataText() {
		String text = "";
		for (String str : this.data.values()) { text += str; }
		return text;
	}
	
	public NBTTagCompound getDataNbt() {
		if (this.fileType == 0) { return null; }
		try { return NBTJsonUtil.Convert(this.getDataText()); }
		catch (JsonException e) {}
		return null;
	}

	public void reset(File file) {
		if (file==null || !file.exists()) {
			this.data.clear();
			this.size = -1;
			return;
		}
		this.saveType = 0;
		this.size = file.length();
		try {
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
			this.fileType = 2;
			this.reset(NBTJsonUtil.Convert(nbt));
			return;
		} catch (IOException e) { }
		
		String text = "";
		try { text = Files.toString(file, Charset.forName("UTF-8")); } catch (IOException e) {}
		try {
			NBTJsonUtil.Convert(text);
			this.fileType = 1;
		} catch (JsonException e) {}
		this.reset(text);
	}
	
	public void reset(String text) {
		if (text==null || text.isEmpty()) {
			this.data.clear();
			this.size = -1;
			return;
		}
		if (this.size<0) { this.size = text.getBytes().length; }
		int part = 0;
		while (!text.isEmpty()) {
			int end = text.length() < TempFile.maxPart ? text.length() : TempFile.maxPart;
			if (end == 0) { break; }
			this.data.put(part, text.substring(0, end));
			if (end == text.length()) { break; }
			text = text.substring(end);
			part++;
		}
	}

	public NBTTagCompound getTitle() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("name", this.name);
		nbt.setInteger("filetype", this.fileType);
		nbt.setInteger("savetype", this.saveType);
		nbt.setInteger("parts", this.data.size());
		nbt.setLong("size", this.size);
		return nbt;
	}
	
	public void setTitle(NBTTagCompound nbt) {
		this.name = nbt.getString("name");
		this.fileType = nbt.getInteger("filetype");
		this.saveType = nbt.getInteger("savetype");
		this.size = nbt.getLong("size");
		this.data.clear();
		this.lastLoad = 0L;
		for (int i = 0; i < nbt.getInteger("parts"); i++) { this.data.put(i, ""); }
	}
	
	public int getNextPatr() {
		if (this.data.isEmpty()) { return -1; }
		int i = 0;
		for (String str : this.data.values()) {
			if (str==null || str.isEmpty()) { break; }
			i++;
		}
		return i;
	}

	public void save() {
		if (!this.isLoad()) { return; }
		File file = new File(this.name);
		if (this.saveType==0) {
			File dir = new File(CustomNpcs.Dir, "temp files");
			if (!dir.exists()) { dir.mkdir(); }
			file = new File(dir, file.getName());
		}
		else if (this.saveType==1) {
			File dir = new File(CustomNpcs.Dir, "client scripts/ecmascript");
			if (!dir.exists()) { dir.mkdir(); }
			file = new File(dir, file.getName());
		}
		if (!file.exists()) { try { file.createNewFile(); } catch (IOException e) { } }
		if (file.exists()) {
			this.saveTo(file);
			return;
		}
		LogWriter.error("Unable to create file: "+file.getAbsolutePath()+". Path is incorrect!");
	}

	private void saveTo(File file) {
		switch(this.fileType) {
			case 1: {
				try {
					NBTJsonUtil.SaveFile(file, this.getDataNbt());
					LogWriter.debug("Save nbt json to file: "+file.getAbsolutePath());
				}
				catch (IOException | JsonException e) { LogWriter.error("Error save nbt json to file: "+file.getAbsolutePath(), e); }
				break;
			}
			case 2: {
				try {
					CompressedStreamTools.writeCompressed(this.getDataNbt(), new FileOutputStream(file));
					LogWriter.debug("Save nbt compressed to file: "+file.getAbsolutePath());
				}
				catch (IOException e) { LogWriter.error("Error save nbt compressed to file: "+file.getAbsolutePath(), e); }
				break;
			}
			default: {
				try {
					Files.write(this.getDataText().getBytes(), file);
					LogWriter.debug("Save text to file: "+file.getAbsolutePath());
				}
				catch (IOException e) { LogWriter.error("Error save text to file: "+file.getAbsolutePath(), e); }
				break;
			}
		}
	}
	
}
