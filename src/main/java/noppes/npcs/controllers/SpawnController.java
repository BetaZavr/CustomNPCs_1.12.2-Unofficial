package noppes.npcs.controllers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
	public final HashMap<String, List<SpawnData>> biomes = new HashMap<>();
	public final ArrayList<SpawnData> data = new ArrayList<>();
	private int lastUsedID = 0;
	public Random random = new Random();

	public SpawnController() {
		(SpawnController.instance = this).loadData();
	}

	private void fillBiomeData() {
		biomes.clear();
		for (SpawnData spawn : data) {
			for (String s : spawn.biomes) {
                List<SpawnData> list = biomes.computeIfAbsent(s, k -> new ArrayList<>());
                list.add(spawn);
			}
		}
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (SpawnData spawn : this.data) {
			NBTTagCompound nbtSummon = new NBTTagCompound();
			spawn.writeNBT(nbtSummon);
			list.appendTag(nbtSummon);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("lastID", this.lastUsedID);
		nbttagcompound.setTag("NPCSpawnData", list);
		return nbttagcompound;
	}

	public SpawnData getRandomSpawnData(String biome) {
		List<SpawnData> list = this.getSpawnList(biome);
		if (list == null || list.isEmpty()) { return null; }
		return WeightedRandom.getRandomItem(this.random, list);
	}

	public Map<String, Integer> getScroll() {
		Map<String, Integer> map = new HashMap<>();
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

	public List<SpawnData> getSpawnList(String biome) { return this.biomes.get(biome); }

	public int getUnusedId() {
		return ++this.lastUsedID;
	}

	private void loadData() {
		CustomNpcs.debugData.start(null);
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			CustomNpcs.debugData.end(null);
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
			} catch (Exception e1) { LogWriter.error(e1); }
		}
		CustomNpcs.debugData.end(null);
	}

	public void loadData(DataInputStream stream) throws IOException {
		NBTTagCompound compound = CompressedStreamTools.read(stream);
		this.lastUsedID = compound.getInteger("lastID");
		NBTTagList nbtList = compound.getTagList("NPCSpawnData", 10);
		data.clear();
        for (int i = 0; i < nbtList.tagCount(); ++i) {
            NBTTagCompound nbtSummon = nbtList.getCompoundTagAt(i);
            SpawnData spawn = new SpawnData();
            spawn.readNBT(nbtSummon);
			if (spawn.name == null || spawn.name.isEmpty()) { continue; }
            data.add(spawn);
        }
		this.fillBiomeData();
	}

	private void loadDataFile(File file) throws IOException {
		DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(file.toPath()))));
		this.loadData(var1);
		var1.close();
	}

	public void removeSpawnData(int id) {
		ArrayList<SpawnData> newData = new ArrayList<>();
		for (SpawnData spawn : data) {
			if (spawn.id == id) {
				continue;
			}
			if (spawn.name == null || spawn.name.isEmpty()) { continue; }
			newData.add(spawn);
		}
		data.clear();
		data.addAll(newData);
		fillBiomeData();
		saveData();
	}

	@SuppressWarnings("all")
	public void saveData() {
		CustomNpcs.debugData.start(null);
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "spawns.dat_new");
			File file2 = new File(saveDir, "spawns.dat_old");
			File file3 = new File(saveDir, "spawns.dat");
			CompressedStreamTools.writeCompressed(this.getNBT(), Files.newOutputStream(file.toPath()));
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
		catch (Exception e) { LogWriter.except(e); }
		CustomNpcs.debugData.end(null);
	}

	public void saveSpawnData(SpawnData spawn) {
		if (spawn.name == null || spawn.name.isEmpty()) { return; }
		if (spawn.id < 0) { spawn.id = getUnusedId(); }
		SpawnData spawnData = getSpawnData(spawn.id);
		if (spawnData == null) {
			data.add(spawn);
		}
		else { spawnData.readNBT(spawn.writeNBT(new NBTTagCompound())); }
		fillBiomeData();
		saveData();
	}

}
