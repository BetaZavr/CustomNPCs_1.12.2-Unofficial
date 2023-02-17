package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.util.NBTJsonUtil;

public class ServerCloneController
implements ICloneHandler {
	
	public static ServerCloneController Instance;

	public ServerCloneController() {
		this.loadClones();
	}

	public String addClone(NBTTagCompound nbttagcompound, String name, int tab) {
		this.cleanTags(nbttagcompound);
		this.saveClone(tab, name, nbttagcompound);
		return name;
	}

	public void cleanTags(NBTTagCompound nbttagcompound) {
		if (nbttagcompound.hasKey("ItemGiverId")) {
			nbttagcompound.setInteger("ItemGiverId", 0);
		}
		if (nbttagcompound.hasKey("TransporterId")) {
			nbttagcompound.setInteger("TransporterId", -1);
		}
		nbttagcompound.removeTag("StartPosNew");
		nbttagcompound.removeTag("StartPos");
		nbttagcompound.removeTag("MovingPathNew");
		nbttagcompound.removeTag("Pos");
		nbttagcompound.removeTag("Riding");
		nbttagcompound.removeTag("UUID");
		nbttagcompound.removeTag("UUIDMost");
		nbttagcompound.removeTag("UUIDLeast");
		if (!nbttagcompound.hasKey("ModRev")) {
			nbttagcompound.setInteger("ModRev", 1);
		}
		if (nbttagcompound.hasKey("TransformRole")) {
			NBTTagCompound adv = nbttagcompound.getCompoundTag("TransformRole");
			adv.setInteger("TransporterId", -1);
			nbttagcompound.setTag("TransformRole", adv);
		}
		if (nbttagcompound.hasKey("TransformJob")) {
			NBTTagCompound adv = nbttagcompound.getCompoundTag("TransformJob");
			adv.setInteger("ItemGiverId", 0);
			nbttagcompound.setTag("TransformJob", adv);
		}
		if (nbttagcompound.hasKey("TransformAI")) {
			NBTTagCompound adv = nbttagcompound.getCompoundTag("TransformAI");
			adv.removeTag("StartPosNew");
			adv.removeTag("StartPos");
			adv.removeTag("MovingPathNew");
			nbttagcompound.setTag("TransformAI", adv);
		}
		if (nbttagcompound.hasKey("id")) {
			String id = nbttagcompound.getString("id");
			id = id.replace(CustomNpcs.MODID + ".", CustomNpcs.MODID + ":");
			nbttagcompound.setString("id", id);
		}
	}

	@Override
	public IEntity<?> get(int tab, String name, IWorld world) {
		NBTTagCompound compound = this.getCloneData(null, name, tab);
		if (compound == null) {
			throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
		}
		ServerCloneController.Instance.cleanTags(compound);
		Entity entity = EntityList.createEntityFromNBT(compound, world.getMCWorld());
		if (entity == null) {
			return null;
		}
		return NpcAPI.Instance().getIEntity(entity);
	}

	public NBTTagCompound getCloneData(ICommandSender player, String name, int tab) {
		File file = new File(new File(this.getDir(), tab + ""), name + ".json");
		if (!file.exists()) {
			if (player != null) {
				player.sendMessage(new TextComponentString("Could not find clone file"));
			}
			return null;
		}
		try {
			return NBTJsonUtil.LoadFile(file);
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			if (player != null) {
				player.sendMessage(new TextComponentString(e.getMessage()));
			}
			return null;
		}
	}

	public List<String> getClones(int tab) {
		List<String> list = new ArrayList<String>();
		File dir = new File(this.getDir(), tab + "");
		if (!dir.exists() || !dir.isDirectory()) {
			return list;
		}
		for (String file : dir.list()) {
			if (file.endsWith(".json")) {
				list.add(file.substring(0, file.length() - 5));
			}
		}
		return list;
	}

	public File getDir() {
		File dir = new File(CustomNpcs.getWorldSaveDirectory(), "clones");
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}

	private void loadClones() {
		try {
			File dir = new File(this.getDir(), "..");
			File file = new File(dir, "clonednpcs.dat");
			if (file.exists()) {
				Map<Integer, Map<String, NBTTagCompound>> clones = this.loadOldClones(file);
				file.delete();
				file = new File(dir, "clonednpcs.dat_old");
				if (file.exists()) {
					file.delete();
				}
				for (int tab : clones.keySet()) {
					Map<String, NBTTagCompound> map = clones.get(tab);
					for (String name : map.keySet()) {
						this.saveClone(tab, name, map.get(name));
					}
				}
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}

	private Map<Integer, Map<String, NBTTagCompound>> loadOldClones(File file) throws Exception {
		Map<Integer, Map<String, NBTTagCompound>> clones = new HashMap<Integer, Map<String, NBTTagCompound>>();
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
		NBTTagList list = nbttagcompound1.getTagList("Data", 10);
		if (list == null) {
			return clones;
		}
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			if (!compound.hasKey("ClonedTab")) {
				compound.setInteger("ClonedTab", 1);
			}
			Map<String, NBTTagCompound> tab = clones.get(compound.getInteger("ClonedTab"));
			if (tab == null) {
				clones.put(compound.getInteger("ClonedTab"), tab = new HashMap<String, NBTTagCompound>());
			}
			String name = compound.getString("ClonedName");
			for (int number = 1; tab
					.containsKey(name); name = String.format("%s%s", compound.getString("ClonedName"), number)) {
				++number;
			}
			compound.removeTag("ClonedName");
			compound.removeTag("ClonedTab");
			compound.removeTag("ClonedDate");
			this.cleanTags(compound);
			tab.put(name, compound);
		}
		return clones;
	}

	@Override
	public void remove(int tab, String name) {
		this.removeClone(name, tab);
	}

	public boolean removeClone(String name, int tab) {
		File file = new File(new File(this.getDir(), tab + ""), name + ".json");
		if (!file.exists()) {
			return false;
		}
		file.delete();
		return true;
	}

	public void saveClone(int tab, String name, NBTTagCompound compound) {
		try {
			File dir = new File(this.getDir(), tab + "");
			if (!dir.exists()) {
				dir.mkdir();
			}
			String filename = name + ".json";
			File file = new File(dir, filename + "_new");
			File file2 = new File(dir, filename);
			NBTJsonUtil.SaveFile(file, compound);
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}

	@Override
	public void set(int tab, String name, IEntity<?> entity) {
		NBTTagCompound compound = new NBTTagCompound();
		if (!entity.getMCEntity().writeToNBTAtomically(compound)) {
			throw new CustomNPCsException("Cannot save dead entities", new Object[0]);
		}
		this.cleanTags(compound);
		this.saveClone(tab, name, compound);
	}

	@Override
	public IEntity<?> spawn(double x, double y, double z, int tab, String name, IWorld world) {
		NBTTagCompound compound = this.getCloneData(null, name, tab);
		if (compound == null) {
			throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
		}
		Entity entity = NoppesUtilServer.spawnClone(compound, x, y, z, world.getMCWorld());
		if (entity == null) {
			return null;
		}
		return NpcAPI.Instance().getIEntity(entity);
	}
}
