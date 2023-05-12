package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;

public class TransportController {
	private static TransportController instance;

	public static TransportController getInstance() {
		return TransportController.instance;
	}

	public HashMap<Integer, TransportCategory> categories;
	private int lastUsedID;

	private HashMap<Integer, TransportLocation> locations;

	public TransportController() {
		this.locations = new HashMap<Integer, TransportLocation>();
		this.categories = new HashMap<Integer, TransportCategory>();
		this.lastUsedID = 0;
		(TransportController.instance = this).loadCategories();
		if (this.categories.isEmpty()) {
			TransportCategory cat = new TransportCategory();
			cat.id = 1;
			cat.title = "Default";
			this.categories.put(cat.id, cat);
		}
	}

	private boolean containsCategoryName(String name) {
		name = name.toLowerCase();
		for (TransportCategory cat : this.categories.values()) {
			if (cat.title.toLowerCase().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsLocationName(String name) {
		name = name.toLowerCase();
		for (TransportLocation loc : this.locations.values()) {
			if (loc.name.toLowerCase().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (TransportCategory category : this.categories.values()) {
			NBTTagCompound compound = new NBTTagCompound();
			category.writeNBT(compound);
			list.appendTag(compound);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("lastID", this.lastUsedID);
		nbttagcompound.setTag("NPCTransportCategories", list);
		return nbttagcompound;
	}

	public TransportLocation getTransport(int transportId) {
		return this.locations.get(transportId);
	}

	public TransportLocation getTransport(String name) {
		for (TransportLocation loc : this.locations.values()) {
			if (loc.name.equals(name)) {
				return loc;
			}
		}
		return null;
	}

	private int getUniqueIdCategory() {
		int id = 0;
		for (int catid : this.categories.keySet()) {
			if (catid > id) {
				id = catid;
			}
		}
		return ++id;
	}

	private int getUniqueIdLocation() {
		if (this.lastUsedID == 0) {
			for (int catid : this.locations.keySet()) {
				if (catid > this.lastUsedID) {
					this.lastUsedID = catid;
				}
			}
		}
		return ++this.lastUsedID;
	}

	private void loadCategories() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			return;
		}
		try {
			File file = new File(saveDir, "transport.dat");
			if (!file.exists()) {
				return;
			}
			this.loadCategories(file);
		} catch (IOException e) {
			try {
				File file2 = new File(saveDir, "transport.dat_old");
				if (!file2.exists()) {
					return;
				}
				this.loadCategories(file2);
			} catch (IOException ex) {
			}
		}
	}

	public void loadCategories(File file) throws IOException {
		HashMap<Integer, TransportLocation> locations = new HashMap<Integer, TransportLocation>();
		HashMap<Integer, TransportCategory> categories = new HashMap<Integer, TransportCategory>();
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
		this.lastUsedID = nbttagcompound1.getInteger("lastID");
		NBTTagList list = nbttagcompound1.getTagList("NPCTransportCategories", 10);
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.tagCount(); ++i) {
			TransportCategory category = new TransportCategory();
			NBTTagCompound compound = list.getCompoundTagAt(i);
			category.readNBT(compound);
			for (TransportLocation location : category.locations.values()) {
				locations.put(location.id, location);
			}
			categories.put(category.id, category);
		}
		this.locations = locations;
		this.categories = categories;
	}

	public void removeCategory(int id) {
		if (this.categories.size() == 1) {
			return;
		}
		TransportCategory cat = this.categories.get(id);
		if (cat == null) {
			return;
		}
		for (int i : cat.locations.keySet()) {
			this.locations.remove(i);
		}
		this.categories.remove(id);
		this.saveCategories();
	}

	public TransportLocation removeLocation(int location) {
		TransportLocation loc = this.locations.get(location);
		if (loc == null) {
			return null;
		}
		loc.category.locations.remove(location);
		this.locations.remove(location);
		this.saveCategories();
		return loc;
	}

	public void saveCategories() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "transport.dat_new");
			File file2 = new File(saveDir, "transport.dat_old");
			File file3 = new File(saveDir, "transport.dat");
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

	public void saveCategory(String name, int id) {
		if (id < 0) {
			id = this.getUniqueIdCategory();
		}
		if (this.categories.containsKey(id)) {
			TransportCategory category = this.categories.get(id);
			if (!category.title.equals(name)) {
				while (this.containsCategoryName(name)) {
					name += "_";
				}
				this.categories.get(id).title = name;
			}
		} else {
			while (this.containsCategoryName(name)) {
				name += "_";
			}
			TransportCategory category = new TransportCategory();
			category.id = id;
			category.title = name;
			this.categories.put(id, category);
		}
		this.saveCategories();
	}

	public TransportLocation saveLocation(int categoryId, NBTTagCompound compound, EntityPlayerMP player,
			EntityNPCInterface npc) {
		TransportCategory category = this.categories.get(categoryId);
		if (category == null || !(npc.advanced.roleInterface instanceof RoleTransporter)) { return null; }
		RoleTransporter role = (RoleTransporter) npc.advanced.roleInterface;
		TransportLocation location = new TransportLocation();
		location.readNBT(compound);
		location.category = category;
		if (role.hasTransport()) {
			location.id = role.transportId;
		}
		if (location.id < 0 || !this.locations.get(location.id).name.equals(location.name)) {
			while (this.containsLocationName(location.name)) {
				StringBuilder sb = new StringBuilder();
				TransportLocation transportLocation = location;
				transportLocation.name = sb.append(transportLocation.name).append("_").toString();
			}
		}
		if (location.id < 0) {
			location.id = this.getUniqueIdLocation();
		}
		category.locations.put(location.id, location);
		this.locations.put(location.id, location);
		this.saveCategories();
		return location;
	}

	public void setLocation(TransportLocation location) {
		if (this.locations.containsKey(location.id)) {
			for (TransportCategory cat : this.categories.values()) {
				cat.locations.remove(location.id);
			}
		}
		this.locations.put(location.id, location);
		location.category.locations.put(location.id, location);
	}
}
