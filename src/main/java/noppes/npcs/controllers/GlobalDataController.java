package noppes.npcs.controllers;

import java.io.File;
import java.nio.file.Files;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class GlobalDataController {
	public static GlobalDataController instance;
	private int itemGiverId;

	public GlobalDataController() {
		this.itemGiverId = 0;
		(GlobalDataController.instance = this).load();
	}

	public int incrementItemGiverId() {
		++this.itemGiverId;
		this.save();
		return this.itemGiverId;
	}

	private void load() {
		CustomNpcs.debugData.start(null);
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		try {
			File file = new File(saveDir, "global.dat");
			if (file.exists()) {
				this.loadData(file);
			}
		} catch (Exception e) {
			try {
				File file2 = new File(saveDir, "global.dat_old");
				if (file2.exists()) {
					this.loadData(file2);
				}
			} catch (Exception ee) { LogWriter.error(ee); }
		}
		CustomNpcs.debugData.end(null);
	}

	private void loadData(File file) throws Exception {
		NBTTagCompound compound = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
		this.itemGiverId = compound.getInteger("itemGiverId");
	}

	@SuppressWarnings("all")
	public void save() {
		CustomNpcs.debugData.start(null);
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("itemGiverId", this.itemGiverId);
			File file = new File(saveDir, "global.dat_new");
			File file2 = new File(saveDir, "global.dat_old");
			File file3 = new File(saveDir, "global.dat");
			CompressedStreamTools.writeCompressed(nbttagcompound, Files.newOutputStream(file.toPath()));
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
		}
		catch (Exception e) { LogWriter.error(e); }
		CustomNpcs.debugData.end(null);
	}
}
