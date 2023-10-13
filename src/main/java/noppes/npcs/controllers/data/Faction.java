package noppes.npcs.controllers.data;

import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.entity.EntityNPCInterface;

public class Faction
implements IFaction {
	
	public static String formatName(String name) {
		name = name.toLowerCase().trim();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public HashSet<Integer> attackFactions, frendFactions;
	public int color;
	public int defaultPoints;
	public int friendlyPoints;
	public boolean getsAttacked;
	public boolean hideFaction;
	public int id;
	public String name;

	public int neutralPoints;
	public FactionOptions factions;

	public Faction() {
		this.name = "";
		this.color = Integer.parseInt("FF00", 16);
		this.id = -1;
		this.neutralPoints = 500;
		this.friendlyPoints = 1500;
		this.defaultPoints = 1000;
		this.hideFaction = false;
		this.getsAttacked = false;
		this.attackFactions = new HashSet<Integer>();
		this.frendFactions = new HashSet<Integer>();
		this.factions = new FactionOptions();
	}

	public Faction(int id, String name, int color, int defaultPoints) {
		this();
		this.id = id;
		this.name = name;
		this.color = color;
		this.defaultPoints = defaultPoints;
	}

	@Override
	public void addHostile(int id) {
		if (this.attackFactions.contains(id)) {
			throw new CustomNPCsException("Faction " + this.id + " is already hostile to " + id, new Object[0]);
		}
		this.attackFactions.add(id);
	}

	@Override
	public boolean getAttackedByMobs() {
		return this.getsAttacked;
	}

	@Override
	public int getColor() {
		return this.color;
	}

	@Override
	public int getDefaultPoints() {
		return this.defaultPoints;
	}

	@Override
	public int[] getHostileList() {
		int[] a = new int[this.attackFactions.size()];
		int i = 0;
		for (Integer val : this.attackFactions) {
			a[i++] = val;
		}
		return a;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public boolean getIsHidden() {
		return this.hideFaction;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean hasHostile(int id) {
		return this.attackFactions.contains(id);
	}

	@Override
	public boolean hostileToFaction(int factionId) {
		return this.attackFactions.contains(factionId);
	}

	@Override
	public boolean hostileToNpc(ICustomNpc<?> npc) {
		return this.attackFactions.contains(npc.getFaction().getId());
	}

	public boolean isAggressiveToNpc(EntityNPCInterface entity) {
		return this.attackFactions.contains(entity.faction.id) || entity.advanced.attackFactions.contains(this.id);
	}

	public boolean isAggressiveToPlayer(EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			return false;
		}
		PlayerFactionData data = PlayerData.get(player).factionData;
		return data.getFactionPoints(player, this.id) < this.neutralPoints;
	}

	public boolean isFriendlyToPlayer(EntityPlayer player) {
		PlayerFactionData data = PlayerData.get(player).factionData;
		return data.getFactionPoints(player, this.id) >= this.friendlyPoints;
	}

	public boolean isNeutralToPlayer(EntityPlayer player) {
		PlayerFactionData data = PlayerData.get(player).factionData;
		int points = data.getFactionPoints(player, this.id);
		return points >= this.neutralPoints && points < this.friendlyPoints;
	}

	@Override
	public int playerStatus(IPlayer<?> player) {
		PlayerFactionData data = PlayerData.get(player.getMCEntity()).factionData;
		int points = data.getFactionPoints(player.getMCEntity(), this.id);
		if (points >= this.friendlyPoints) {
			return 1;
		}
		if (points < this.neutralPoints) {
			return -1;
		}
		return 0;
	}

	public void readNBT(NBTTagCompound compound) {
		this.name = compound.getString("Name");
		this.color = compound.getInteger("Color");
		this.id = compound.getInteger("Slot");
		this.neutralPoints = compound.getInteger("NeutralPoints");
		this.friendlyPoints = compound.getInteger("FriendlyPoints");
		this.defaultPoints = compound.getInteger("DefaultPoints");
		this.hideFaction = compound.getBoolean("HideFaction");
		this.getsAttacked = compound.getBoolean("GetsAttacked");
		this.attackFactions = NBTTags.getIntegerSet(compound.getTagList("AttackFactions", 10));
		this.frendFactions = NBTTags.getIntegerSet(compound.getTagList("FrendFactions", 10));
		this.factions.readFromNBT(compound.getCompoundTag("FactionPoints"));
	}

	@Override
	public void removeHostile(int id) {
		this.attackFactions.remove(id);
	}

	@Override
	public void save() {
		FactionController.instance.saveFaction(this);
	}

	@Override
	public void setAttackedByMobs(boolean bo) {
		this.getsAttacked = bo;
	}

	@Override
	public void setDefaultPoints(int points) {
		this.defaultPoints = points;
	}

	@Override
	public void setIsHidden(boolean bo) {
		this.hideFaction = bo;
	}

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("Slot", this.id);
		compound.setString("Name", this.name);
		compound.setInteger("Color", this.color);
		compound.setInteger("NeutralPoints", this.neutralPoints);
		compound.setInteger("FriendlyPoints", this.friendlyPoints);
		compound.setInteger("DefaultPoints", this.defaultPoints);
		compound.setBoolean("HideFaction", this.hideFaction);
		compound.setBoolean("GetsAttacked", this.getsAttacked);
		compound.setTag("AttackFactions", NBTTags.nbtIntegerCollection(this.attackFactions));
		compound.setTag("FrendFactions", NBTTags.nbtIntegerCollection(this.frendFactions));
		compound.setTag("FactionPoints", this.factions.writeToNBT(new NBTTagCompound()));
		return compound;
	}
}
