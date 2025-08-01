package noppes.npcs.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class TempFile {

	private static final int maxPart = 30000;

	public String name;
	public final Map<Integer, String> data = new TreeMap<>();
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

	public TempFile(String name, int filetype, int savetype, long size) {
		this.name = name;
		this.fileType = filetype;
		this.saveType = savetype;
		this.size = size;
		this.tryLoads = 0;
		this.lastLoad = System.currentTimeMillis();
	}

	public NBTTagCompound getDataNbt() {
		if (this.fileType == 0) {
			return null;
		}
		try {
			return NBTJsonUtil.Convert(this.getDataText());
		} catch (Exception e) { LogWriter.error(e); }
		return null;
	}

	public String getDataText() {
		StringBuilder text = new StringBuilder();
		for (String str : this.data.values()) {
			text.append(str);
		}
		return text.toString();
	}

	public int getNextPart() {
		if (this.data.isEmpty()) {
			return -1;
		}
		int i = 0;
		for (String str : this.data.values()) {
			if (str == null || str.isEmpty()) {
				break;
			}
			i++;
		}
		return i;
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

	public boolean isLoad() {
		if (this.data.isEmpty()) {
			return false;
		}
		for (String str : this.data.values()) {
			if (str == null || str.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public void reset(File file) throws IOException {
		if (file == null || !file.exists()) {
			this.data.clear();
			this.size = -1;
			return;
		}
		this.saveType = 0;
		this.size = file.length();
		try {
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(java.nio.file.Files.newInputStream(file.toPath()));
			this.fileType = 2;
			this.reset(NBTJsonUtil.Convert(nbt));
			return;
		} catch (IOException e) { LogWriter.error(e); }

		String text = Util.instance.loadFile(file);

		try {
			NBTJsonUtil.Convert(text);
			this.fileType = 1;
		} catch (Exception e) { LogWriter.error(e); }
		this.reset(text);
	}

	public void reset(String text) {
		if (text == null || text.isEmpty()) {
			this.data.clear();
			this.size = -1;
			return;
		}
		if (this.size < 0) {
			this.size = text.getBytes().length;
		}
		int part = 0;
		while (!text.isEmpty()) {
			int end = Math.min(text.length(), TempFile.maxPart);
            this.data.put(part, text.substring(0, end));
			if (end == text.length()) {
				break;
			}
			text = text.substring(end);
			part++;
		}
	}

	public void save() throws IOException {
		if (!this.isLoad()) {
			return;
		}
		File file = new File(this.name);
		if (this.saveType == 0) {
			File dir = new File(CustomNpcs.Dir, "temp files");
			if (dir.exists() || dir.mkdir()) {
				file = new File(dir, file.getName());
			}
		} else if (this.saveType == 1) {
			File dir = new File(CustomNpcs.Dir, "client scripts/ecmascript");
			if (dir.exists() || dir.mkdir()) {
				file = new File(dir, file.getName());
			}
		}
		if (!file.exists() && !file.createNewFile()) {
			return;
		}
		if (file.exists()) {
			this.saveTo(file);
			return;
		}
		LogWriter.error("Unable to create file: " + file.getAbsolutePath() + ". Path is incorrect!");
	}

	private void saveTo(File file) {
		switch (this.fileType) {
		case 1: {
			try {
				Util.instance.saveFile(file, NBTJsonUtil.Convert(this.getDataNbt()));
				LogWriter.debug("Save nbt json to file: " + file.getAbsolutePath());
			} catch (Exception e) {
				LogWriter.error("Error save nbt json to file: " + file.getAbsolutePath(), e);
			}
			break;
		}
		case 2: {
			try {
				CompressedStreamTools.writeCompressed(this.getDataNbt(), java.nio.file.Files.newOutputStream(file.toPath()));
				LogWriter.debug("Save nbt compressed to file: " + file.getAbsolutePath());
			} catch (IOException e) {
				LogWriter.error("Error save nbt compressed to file: " + file.getAbsolutePath(), e);
			}
			break;
		}
		default: {
			if (Util.instance.saveFile(file, this.getDataText())) { LogWriter.debug("Save text to file: " + file.getAbsolutePath()); }
			break;
		}
		}
	}

	public void setTitle(NBTTagCompound nbt) {
		this.name = nbt.getString("name");
		this.fileType = nbt.getInteger("filetype");
		this.saveType = nbt.getInteger("savetype");
		this.size = nbt.getLong("size");
		this.data.clear();
		this.lastLoad = 0L;
		for (int i = 0; i < nbt.getInteger("parts"); i++) {
			this.data.put(i, "");
		}
	}

}
