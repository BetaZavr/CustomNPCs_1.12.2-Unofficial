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
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Faction;

public class FactionController implements IFactionHandler {
	public static FactionController instance = new FactionController();
	public HashMap<Integer, Faction> factions;
	public HashMap<Integer, Faction> factionsSync;
	private int lastUsedID;

	public FactionController() {
		this.factionsSync = new HashMap<Integer, Faction>();
		this.factions = new HashMap<Integer, Faction>();
		this.lastUsedID = 0;
		FactionController.instance = this;
		this.factions.put(0, new Faction(0, "Friendly", 56576, 2000));
		this.factions.put(1, new Faction(1, "Neutral", 15916288, 1000));
		this.factions.put(2, new Faction(2, "Aggressive", 14483456, 0));
	}

	@Override
	public IFaction create(String name, int color) {
		Faction faction = new Faction();
		while (this.hasName(name)) {
			name += "_";
		}
		faction.name = name;
		faction.color = color;
		this.saveFaction(faction);
		return faction;
	}

	@Override
	public IFaction delete(int id) {
		if (id < 0 || this.factions.size() <= 1) {
			return null;
		}
		Faction faction = this.factions.remove(id);
		if (faction == null) {
			return null;
		}
		this.saveFactions();
		faction.id = -1;
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, 1, id);
		return faction;
	}

	@Override
	public IFaction get(int id) {
		return this.factions.get(id);
	}

	public Faction getFaction(int faction) {
		return this.factions.get(faction);
	}

	public Faction getFactionFromName(String factioname) {
		for (Map.Entry<Integer, Faction> entryfaction : this.factions.entrySet()) {
			if (entryfaction.getValue().name.equalsIgnoreCase(factioname)) {
				return entryfaction.getValue();
			}
		}
		return null;
	}

	public Faction getFirstFaction() {
		return this.factions.values().iterator().next();
	}

	public int getFirstFactionId() {
		return this.factions.keySet().iterator().next();
	}

	public String[] getNames() {
		String[] names = new String[this.factions.size()];
		int i = 0;
		for (Faction faction : this.factions.values()) {
			names[i] = faction.name.toLowerCase();
			++i;
		}
		return names;
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (int slot : this.factions.keySet()) {
			Faction faction = this.factions.get(slot);
			NBTTagCompound nbtfactions = new NBTTagCompound();
			faction.writeNBT(nbtfactions);
			list.appendTag(nbtfactions);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("lastID", this.lastUsedID);
		nbttagcompound.setTag("NPCFactions", list);
		return nbttagcompound;
	}

	public int getUnusedId() {
		if (this.lastUsedID == 0) {
			for (int catid : this.factions.keySet()) {
				if (catid > this.lastUsedID) {
					this.lastUsedID = catid;
				}
			}
		}
		return ++this.lastUsedID;
	}

	public boolean hasName(String newName) {
		if (newName.trim().isEmpty()) {
			return true;
		}
		for (Faction faction : this.factions.values()) {
			if (faction.name.equals(newName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<IFaction> list() {
		return new ArrayList<IFaction>(this.factions.values());
	}

	public void load() {
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadFactions");
		}
		this.factions = new HashMap<Integer, Faction>();
		this.lastUsedID = 0;
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			if (saveDir == null) {
				if (CustomNpcs.VerboseDebug) {
					CustomNpcs.debugData.endDebug("Common", null, "loadFactions");
				}
				return;
			}
			try {
				File file = new File(saveDir, "factions.dat");
				if (file.exists()) {
					this.loadFactionsFile(file);
				}
			} catch (Exception e) {
				try {
					File file2 = new File(saveDir, "factions.dat_old");
					if (file2.exists()) {
						this.loadFactionsFile(file2);
					}
				} catch (Exception ex) {
				}
			}
		} finally {
			EventHooks.onGlobalFactionsLoaded(this);
			if (this.factions.isEmpty()) {
				this.factions.put(0, new Faction(0, "Friendly", 56576, 2000));
				this.factions.put(1, new Faction(1, "Neutral", 15916288, 1000));
				this.factions.put(2, new Faction(2, "Aggressive", 14483456, 0));
			}
		}
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.endDebug("Common", null, "loadFactions");
		}
	}

	public void loadFactions(DataInputStream stream) throws IOException {
		HashMap<Integer, Faction> factions = new HashMap<Integer, Faction>();
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.read(stream);
		this.lastUsedID = nbttagcompound1.getInteger("lastID");
		NBTTagList list = nbttagcompound1.getTagList("NPCFactions", 10);
		if (list != null) {
			for (int i = 0; i < list.tagCount(); ++i) {
				NBTTagCompound nbttagcompound2 = list.getCompoundTagAt(i);
				Faction faction = new Faction();
				faction.readNBT(nbttagcompound2);
				factions.put(faction.id, faction);
			}
		}
		this.factions = factions;
	}

	private void loadFactionsFile(File file) throws IOException {
		DataInputStream var1 = new DataInputStream(
				new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		this.loadFactions(var1);
		var1.close();
	}

	public void saveFaction(Faction faction) {
		if (faction.id < 0) {
			faction.id = this.getUnusedId();
			while (this.hasName(faction.name)) {
				faction.name += "_";
			}
		} else {
			Faction existing = this.factions.get(faction.id);
			if (existing != null && !existing.name.equals(faction.name)) {
				while (this.hasName(faction.name)) {
					faction.name += "_";
				}
			}
		}
		this.factions.remove(faction.id);
		this.factions.put(faction.id, faction);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, 1, faction.writeNBT(new NBTTagCompound()));
		this.saveFactions();
	}

	public void saveFactions() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "factions.dat_new");
			File file2 = new File(saveDir, "factions.dat_old");
			File file3 = new File(saveDir, "factions.dat");
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

}
