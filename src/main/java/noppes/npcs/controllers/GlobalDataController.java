package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;

public class GlobalDataController {
	public static GlobalDataController instance;
	private int itemGiverId;

	public GlobalDataController() {
		this.itemGiverId = 0;
		(GlobalDataController.instance = this).load();
	}

	public int incrementItemGiverId() {
		++this.itemGiverId;
		this.saveData();
		return this.itemGiverId;
	}

	private void load() {
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadGlobalData");
		}
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
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.endDebug("Common", null, "loadGlobalData");
		}
	}

	private void loadData(File file) throws Exception {
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
		this.itemGiverId = nbttagcompound1.getInteger("itemGiverId");
	}

	public void saveData() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("itemGiverId", this.itemGiverId);
			File file = new File(saveDir, "global.dat_new");
			File file2 = new File(saveDir, "global.dat_old");
			File file3 = new File(saveDir, "global.dat");
			CompressedStreamTools.writeCompressed(nbttagcompound, (OutputStream) new FileOutputStream(file));
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
			e.printStackTrace();
		}
	}
}
