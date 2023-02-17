package noppes.npcs.controllers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandom;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.SpawnData;

public class SpawnController {
	public static SpawnController instance;
	public HashMap<String, List<SpawnData>> biomes;
	public ArrayList<SpawnData> data;
	private int lastUsedID;
	public Random random;

	public SpawnController() {
		this.biomes = new HashMap<String, List<SpawnData>>();
		this.data = new ArrayList<SpawnData>();
		this.random = new Random();
		this.lastUsedID = 0;
		(SpawnController.instance = this).loadData();
	}

	private void fillBiomeData() {
		HashMap<String, List<SpawnData>> biomes = new HashMap<String, List<SpawnData>>();
		for (SpawnData spawn : this.data) {
			for (String s : spawn.biomes) {
				List<SpawnData> list = biomes.get(s);
				if (list == null) {
					biomes.put(s, list = new ArrayList<SpawnData>());
				}
				list.add(spawn);
			}
		}
		this.biomes = biomes;
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (SpawnData spawn : this.data) {
			NBTTagCompound nbtfactions = new NBTTagCompound();
			spawn.writeNBT(nbtfactions);
			list.appendTag(nbtfactions);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("lastID", this.lastUsedID);
		nbttagcompound.setTag("NPCSpawnData", list);
		return nbttagcompound;
	}

	public SpawnData getRandomSpawnData(String biome, boolean isAir) {
		List<SpawnData> list = this.getSpawnList(biome);
		if (list == null || list.isEmpty()) {
			return null;
		}
		return WeightedRandom.getRandomItem(this.random, list);
	}

	public Map<String, Integer> getScroll() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (SpawnData spawn : this.data) {
			map.put(spawn.name, spawn.id);
		}
		return map;
	}

	public SpawnData getSpawnData(int id) {
		for (SpawnData spawn : this.data) {
			if (spawn.id == id) {
				return spawn;
			}
		}
		return null;
	}

	public List<SpawnData> getSpawnList(String biome) {
		return this.biomes.get(biome);
	}

	public int getUnusedId() {
		return ++this.lastUsedID;
	}

	private void loadData() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			return;
		}
		try {
			File file = new File(saveDir, "spawns.dat");
			if (file.exists()) {
				this.loadDataFile(file);
			}
		} catch (Exception e) {
			try {
				File oldFile = new File(saveDir, "spawns.dat_old");
				if (oldFile.exists()) {
					this.loadDataFile(oldFile);
				}
			} catch (Exception ex) {
			}
		}
	}

	public void loadData(DataInputStream stream) throws IOException {
		ArrayList<SpawnData> data = new ArrayList<SpawnData>();
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.read(stream);
		this.lastUsedID = nbttagcompound1.getInteger("lastID");
		NBTTagList nbtlist = nbttagcompound1.getTagList("NPCSpawnData", 10);
		if (nbtlist != null) {
			for (int i = 0; i < nbtlist.tagCount(); ++i) {
				NBTTagCompound nbttagcompound2 = nbtlist.getCompoundTagAt(i);
				SpawnData spawn = new SpawnData();
				spawn.readNBT(nbttagcompound2);
				data.add(spawn);
			}
		}
		this.data = data;
		this.fillBiomeData();
	}

	private void loadDataFile(File file) throws IOException {
		DataInputStream var1 = new DataInputStream(
				new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		this.loadData(var1);
		var1.close();
	}

	public void removeSpawnData(int id) {
		ArrayList<SpawnData> data = new ArrayList<SpawnData>();
		for (SpawnData spawn : this.data) {
			if (spawn.id == id) {
				continue;
			}
			data.add(spawn);
		}
		this.data = data;
		this.fillBiomeData();
		this.saveData();
	}

	public void saveData() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "spawns.dat_new");
			File file2 = new File(saveDir, "spawns.dat_old");
			File file3 = new File(saveDir, "spawns.dat");
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(file));
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

	public void saveSpawnData(SpawnData spawn) {
		if (spawn.id < 0) {
			spawn.id = this.getUnusedId();
		}
		SpawnData original = this.getSpawnData(spawn.id);
		if (original == null) {
			this.data.add(spawn);
		} else {
			original.readNBT(spawn.writeNBT(new NBTTagCompound()));
		}
		this.fillBiomeData();
		this.saveData();
	}
}
