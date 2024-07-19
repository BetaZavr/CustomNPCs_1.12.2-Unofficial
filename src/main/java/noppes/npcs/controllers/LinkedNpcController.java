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

public class LinkedNpcController {
	public static class LinkedData {
		public NBTTagCompound data;
		public String name;
		public long time;

		public LinkedData() {
			this.name = "LinkedNpc";
			this.time = 0L;
			this.data = new NBTTagCompound();
			this.time = System.currentTimeMillis();
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("LinkedName", this.name);
			compound.setTag("NPCData", this.data);
			return compound;
		}

		public void setNBT(NBTTagCompound compound) {
			this.name = compound.getString("LinkedName");
			this.data = compound.getCompoundTag("NPCData");
		}
	}

	public static LinkedNpcController Instance;

	public List<LinkedData> list;

	public LinkedNpcController() {
		this.list = new ArrayList<>();
		(LinkedNpcController.Instance = this).load();
	}

	public void addData(String name) {
		if (this.getData(name) != null || name.isEmpty()) {
			return;
		}
		LinkedData data = new LinkedData();
		data.name = name;
		this.list.add(data);
		this.save();
	}

	private void cleanTags(NBTTagCompound compound) {
		compound.removeTag("MovingPathNew");
	}

	public LinkedData getData(String name) {
		for (LinkedData data : this.list) {
			if (data.name.equalsIgnoreCase(name)) {
				return data;
			}
		}
		return null;
	}

	public File getDir() {
		File dir = new File(CustomNpcs.getWorldSaveDirectory(), "linkednpcs");
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}

	private void load() {
		try {
			this.loadNpcs();
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}

	public void loadNpcData(EntityNPCInterface npc) {
		if (npc.linkedName.isEmpty()) {
			return;
		}
		LinkedData data = this.getData(npc.linkedName);
		if (data == null) {
			npc.linkedLast = 0L;
			npc.linkedName = "";
			npc.linkedData = null;
		} else {
			npc.linkedData = data;
			if (npc.posX == 0.0 && npc.posY == 0.0 && npc.posZ == 0.0) {
				return;
			}
			npc.linkedLast = data.time;
			List<int[]> points = npc.ais.getMovingPath();
			NBTTagCompound compound = NBTTags.NBTMerge(this.readNpcData(npc), data.data);
			npc.display.readToNBT(compound);
			npc.stats.readToNBT(compound);
			npc.advanced.readToNBT(compound);
			npc.inventory.readEntityFromNBT(compound);
			if (compound.hasKey("ModelData")) {
				((EntityCustomNpc) npc).modelData.readFromNBT(compound.getCompoundTag("ModelData"));
			}
			npc.ais.readToNBT(compound);
			npc.transform.readToNBT(compound);
			npc.ais.setMovingPath(points);
			npc.updateClient = true;
		}
	}

	private void loadNpcs() {
		LogWriter.info("Loading Linked Npcs");
		File dir = this.getDir();
		if (dir.exists()) {
			List<LinkedData> list = new ArrayList<>();
			for (File file : Objects.requireNonNull(dir.listFiles())) {
				if (file.getName().endsWith(".json")) {
					try {
						NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
						LinkedData linked = new LinkedData();
						linked.setNBT(compound);
						list.add(linked);
					} catch (Exception e) {
						LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
					}
				}
			}
			this.list = list;
		}
		LogWriter.info("Done loading Linked Npcs");
	}

	private NBTTagCompound readNpcData(EntityNPCInterface npc) {
		NBTTagCompound compound = new NBTTagCompound();
		npc.display.writeToNBT(compound);
		npc.inventory.writeEntityToNBT(compound);
		npc.stats.writeToNBT(compound);
		npc.ais.writeToNBT(compound);
		npc.advanced.writeToNBT(compound);
		npc.transform.writeToNBT(compound);
		compound.setTag("ModelData", ((EntityCustomNpc) npc).modelData.writeToNBT());
		return compound;
	}

	public void removeData(String name) {
        this.list.removeIf(linkedData -> linkedData.name.equalsIgnoreCase(name));
		this.save();
	}

	public void save() {
		for (LinkedData npc : this.list) {
			try {
				this.saveNpc(npc);
			} catch (IOException e) {
				LogWriter.except(e);
			}
		}
	}

	private void saveNpc(LinkedData npc) throws IOException {
		File file = new File(this.getDir(), npc.name + ".json_new");
		File file2 = new File(this.getDir(), npc.name + ".json");
		try {
			NBTJsonUtil.SaveFile(file, npc.getNBT());
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
		} catch (NBTJsonUtil.JsonException e) {
			LogWriter.except(e);
		}
	}

	public void saveNpcData(EntityNPCInterface npc) {
		NBTTagCompound compound = this.readNpcData(npc);
		this.cleanTags(compound);
		if (npc.linkedData.data.equals(compound)) {
			return;
		}
		npc.linkedData.data = compound;
		npc.linkedData.time = System.currentTimeMillis();
		this.save();
	}
}
