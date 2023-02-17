package noppes.npcs.client.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.LogWriter;

public class PresetController {
	public static PresetController instance;
	private File dir;
	public HashMap<String, Preset> presets;

	public PresetController(File dir) {
		this.presets = new HashMap<String, Preset>();
		PresetController.instance = this;
		this.dir = dir;
		this.load();
	}

	public void addPreset(Preset preset) {
		while (this.presets.containsKey(preset.name.toLowerCase())) {
			preset.name += "_";
		}
		this.presets.put(preset.name.toLowerCase(), preset);
		this.save();
	}

	public Preset getPreset(String username) {
		if (this.presets.isEmpty()) {
			this.load();
		}
		return this.presets.get(username.toLowerCase());
	}

	public void load() {
		NBTTagCompound compound = this.loadPreset();
		HashMap<String, Preset> presets = new HashMap<String, Preset>();
		if (compound != null) {
			NBTTagList list = compound.getTagList("Presets", 10);
			for (int i = 0; i < list.tagCount(); ++i) {
				NBTTagCompound comp = list.getCompoundTagAt(i);
				Preset preset = new Preset();
				preset.readFromNBT(comp);
				presets.put(preset.name.toLowerCase(), preset);
			}
		}
		Preset.FillDefault(presets);
		this.presets = presets;
	}

	private NBTTagCompound loadPreset() {
		String filename = "presets.dat";
		try {
			File file = new File(this.dir, filename);
			if (!file.exists()) {
				return null;
			}
			return CompressedStreamTools.readCompressed(new FileInputStream(file));
		} catch (Exception e) {
			LogWriter.except(e);
			try {
				File file = new File(this.dir, filename + "_old");
				if (!file.exists()) {
					return null;
				}
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			} catch (Exception err) {
				LogWriter.except(err);
				return null;
			}
		}
	}

	public void removePreset(String preset) {
		if (preset == null) {
			return;
		}
		this.presets.remove(preset.toLowerCase());
		this.save();
	}

	public void save() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Preset preset : this.presets.values()) {
			list.appendTag(preset.writeToNBT());
		}
		compound.setTag("Presets", list);
		this.savePreset(compound);
	}

	private void savePreset(NBTTagCompound compound) {
		String filename = "presets.dat";
		try {
			File file = new File(this.dir, filename + "_new");
			File file2 = new File(this.dir, filename + "_old");
			File file3 = new File(this.dir, filename);
			CompressedStreamTools.writeCompressed(compound, (OutputStream) new FileOutputStream(file));
			if (file2.exists()) {
				file2.delete();
			}
			file3.renameTo(file2);
			if (file3.exists()) {
				file3.delete();
			}
			file.renameTo(file3);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}
}
