package noppes.npcs.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.Util;

public class LinkedNpcController {

	public static class LinkedData {
		public NBTTagCompound data;
		public String name;
		public long time;

		public LinkedData() {
			name = "LinkedNpc";
			time = 0L;
			data = new NBTTagCompound();
			time = System.currentTimeMillis();
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("LinkedName", name);
			compound.setTag("NPCData", data);
			return compound;
		}

		public void setNBT(NBTTagCompound compound) {
			name = compound.getString("LinkedName");
			data = compound.getCompoundTag("NPCData");
		}
	}

	public static LinkedNpcController Instance;

	public List<LinkedData> list;

	public LinkedNpcController() {
		list = new ArrayList<>();
		(LinkedNpcController.Instance = this).loadNpcs();
	}

	public void addData(String name) {
		if (getData(name) != null || name.isEmpty()) {
			return;
		}
		LinkedData data = new LinkedData();
		data.name = name;
		list.add(data);
		save();
	}

	private void cleanTags(NBTTagCompound compound) {
		compound.removeTag("MovingPathNew");
	}

	public LinkedData getData(String name) {
		for (LinkedData data : list) {
			if (data.name.equalsIgnoreCase(name)) {
				return data;
			}
		}
		return null;
	}

	@SuppressWarnings("all")
	public File getDir() {
		File dir = new File(CustomNpcs.getWorldSaveDirectory(), "linkednpcs");
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}

	public void loadNpcData(EntityNPCInterface npc) {
		if (npc.linkedName.isEmpty()) { return; }
		LinkedData data = getData(npc.linkedName);
		if (data == null) {
			npc.linkedLast = 0L;
			npc.linkedName = "";
			npc.linkedData = null;
		}
		else {
			npc.linkedData = data;
			if (npc.posX == 0.0 && npc.posY == 0.0 && npc.posZ == 0.0) { return; }
			npc.linkedLast = data.time;
			List<int[]> points = npc.ais.getMovingPath();
			NBTTagCompound compound = NBTTags.NBTMerge(readNpcData(npc), data.data);
			npc.display.readToNBT(compound);
			npc.stats.readToNBT(compound);
			npc.advanced.load(compound);
			npc.inventory.readEntityFromNBT(compound);
			if (compound.hasKey("ModelData")) {
				((EntityCustomNpc) npc).modelData.load(compound.getCompoundTag("ModelData"));
			}
			npc.ais.readToNBT(compound);
			npc.transform.load(compound);
			npc.animation.load(compound);
			npc.ais.setMovingPath(points);
			npc.updateClient = true;
		}
	}

	private void loadNpcs() {
		CustomNpcs.debugData.start(null);
		LogWriter.info("Loading Linked Npcs");
		File dir = getDir();
		if (dir.exists()) {
			List<LinkedData> listIn = new ArrayList<>();
			for (File file : Objects.requireNonNull(dir.listFiles())) {
				if (file.getName().endsWith(".json")) {
					try {
						NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
						LinkedData linked = new LinkedData();
						linked.setNBT(compound);
						listIn.add(linked);
					} catch (Exception e) {
						LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
					}
				}
			}
			list = listIn;
		}
		LogWriter.info("Done loading Linked Npcs");
		CustomNpcs.debugData.end(null);
	}

	private NBTTagCompound readNpcData(EntityNPCInterface npc) {
		NBTTagCompound compound = new NBTTagCompound();
		npc.display.writeToNBT(compound);
		npc.inventory.writeEntityToNBT(compound);
		npc.stats.writeToNBT(compound);
		npc.ais.writeToNBT(compound);
		npc.advanced.save(compound);
		npc.transform.save(compound);
		npc.animation.save(compound);
		compound.setTag("ModelData", ((EntityCustomNpc) npc).modelData.save());
		return compound;
	}

	public void removeData(String name) {
        list.removeIf(linkedData -> linkedData.name.equalsIgnoreCase(name));
		save();
	}

	public void save() {
		CustomNpcs.debugData.start(null);
		for (LinkedData npc : list) {
			try {
				saveNpc(npc);
			} catch (IOException e) {
				LogWriter.except(e);
			}
		}
		CustomNpcs.debugData.end(null);
	}

	@SuppressWarnings("all")
	private void saveNpc(LinkedData npc) throws IOException {
		File file = new File(getDir(), npc.name + ".json_new");
		File file2 = new File(getDir(), npc.name + ".json");
		try {
			Util.instance.saveFile(file, npc.getNBT());
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}

	public void saveNpcData(EntityNPCInterface npc) {
		NBTTagCompound compound = readNpcData(npc);
		cleanTags(compound);
		if (npc.linkedData.data.equals(compound)) { return; }
		npc.linkedData.data = compound;
		npc.linkedData.time = System.currentTimeMillis();
		save();
	}

}
