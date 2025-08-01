package noppes.npcs.controllers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.data.Faction;

public class FactionController implements IFactionHandler {

	public static FactionController instance = new FactionController();
	public final HashMap<Integer, Faction> factions = new HashMap<>();
	public final HashMap<Integer, Faction> factionsSync = new HashMap<>();
	private int lastUsedID;

	public FactionController() {
		this.lastUsedID = 0;
		FactionController.instance = this;
		this.factions.put(0, new Faction(0, "faction.name.friendly", 0x00DD00, 2000));
		Faction faction = new Faction(1, "faction.name.neutral", 0xF2DD00, 1000);
		faction.flag = new ResourceLocation(CustomNpcs.MODID + ":textures/cloak/baconcape.png");
		this.factions.put(1, faction);
		faction = new Faction(2, "faction.name.aggressive", 0xDD0000, 0);
		faction.flag = new ResourceLocation(CustomNpcs.MODID + ":textures/cloak/enderdragoncape.png");
		this.factions.put(2, faction);
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
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, EnumSync.FactionsData, id);
		return faction;
	}

	@Override
	public IFaction get(int id) {
		return this.factions.get(id);
	}

	public Faction getFaction(int faction) {
		return this.factions.get(faction);
	}

	public Faction getFactionFromName(String faction) {
		for (Map.Entry<Integer, Faction> entryfaction : this.factions.entrySet()) {
			if (entryfaction.getValue().name.equalsIgnoreCase(faction)) {
				return entryfaction.getValue();
			}
		}
		return null;
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
	public IFaction[] list() {
		return this.factions.values().toArray(new IFaction[0]);
	}

	public void load() {
		CustomNpcs.debugData.start(null);
		this.factions.clear();
		this.lastUsedID = 0;
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			if (saveDir == null) {
				CustomNpcs.debugData.end(null);
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
				} catch (Exception ex) { LogWriter.error(ex); }
			}
		} finally {
			EventHooks.onGlobalFactionsLoaded(this);
			if (!factions.containsKey(0)) {
				Faction friendly = new Faction(0, "faction.name.friendly", 0x00DD00, 2000);
				friendly.frendFactions.add(1);
				this.factions.put(0, friendly);
			}
			if (!factions.containsKey(1)) {
				Faction neutral = new Faction(1, "faction.name.neutral", 0xF2DD00, 1000);
				neutral.flag = new ResourceLocation(CustomNpcs.MODID + ":textures/cloak/baconcape.png");
				this.factions.put(1, neutral);
			}
			if (!factions.containsKey(2)) {
				Faction aggressive = new Faction(2, "faction.name.aggressive", 0xDD0000, 0);
				aggressive.attackFactions.add(0);
				aggressive.attackFactions.add(1);
				aggressive.flag = new ResourceLocation(CustomNpcs.MODID + ":textures/cloak/enderdragoncape.png");
				this.factions.put(2, aggressive);
			}
		}
		CustomNpcs.debugData.end(null);
	}

	public void loadFactions(DataInputStream stream) throws IOException {
		factions.clear();
		NBTTagCompound compound = CompressedStreamTools.read(stream);
		this.lastUsedID = compound.getInteger("lastID");
		NBTTagList list = compound.getTagList("NPCFactions", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            Faction faction = new Faction();
            faction.readNBT(nbt);
            factions.put(faction.id, faction);
        }
    }

	private void loadFactionsFile(File file) throws IOException {
		DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(file.toPath()))));
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
		this.factions.put(faction.id, faction);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.FactionsData, faction.writeNBT(new NBTTagCompound()));
		this.saveFactions();
	}

	@SuppressWarnings("all")
	public void saveFactions() {
		CustomNpcs.debugData.start(null);
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "factions.dat_new");
			File file2 = new File(saveDir, "factions.dat_old");
			File file3 = new File(saveDir, "factions.dat");
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

}
