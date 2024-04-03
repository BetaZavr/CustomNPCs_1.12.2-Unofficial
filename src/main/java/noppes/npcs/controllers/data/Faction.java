package noppes.npcs.controllers.data;

import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
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

	public HashSet<Integer> attackFactions = new HashSet<Integer>();
	public HashSet<Integer> frendFactions = new HashSet<Integer>();
	public int id = -1;
	public int color = Integer.parseInt("FF00", 16);
	public int defaultPoints = 1000;
	public int friendlyPoints = 1500;
	public int neutralPoints = 500;
	public boolean getsAttacked = false;
	public boolean hideFaction = false;
	public String name = "";
	public String description = "";
	public ResourceLocation flag = new ResourceLocation(CustomNpcs.MODID + ":textures/cloak/mojang.png");
	public FactionOptions factions = new FactionOptions();

	public Faction() { }

	public Faction(int id, String name, int color, int defaultPoints) {
		this();
		this.id = id;
		this.name = name;
		this.color = color;
		this.defaultPoints = defaultPoints;
	}

	@Override
	public void addHostile(int id) {
		if (attackFactions.contains(id)) {
			throw new CustomNPCsException("Faction " + id + " is already hostile to " + id, new Object[0]);
		}
		attackFactions.add(id);
	}

	@Override
	public boolean getAttackedByMobs() {
		return getsAttacked;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public int getDefaultPoints() {
		return defaultPoints;
	}

	@Override
	public int[] getHostileList() {
		int[] a = new int[attackFactions.size()];
		int i = 0;
		for (Integer val : attackFactions) {
			a[i++] = val;
		}
		return a;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean getIsHidden() {
		return hideFaction;
	}

	@Override
	public String getName() { return new TextComponentTranslation(name).getFormattedText(); }
	
	@Override
	public String getFlag() { return flag == null ? "" : flag.toString(); }

	@Override
	public void setFlag(String flagPath) {
		if (flagPath == null || flagPath.isEmpty()) {
			flag = null;
			return;
		}
		flag = new ResourceLocation(flagPath);
	}

	@Override
	public String getDescription() { return description; }

	@Override
	public void setDescription(String descr) {
		if (descr == null) { descr = ""; }
		description = descr;
	}
	

	@Override
	public boolean hasHostile(int id) {
		return attackFactions.contains(id);
	}

	@Override
	public boolean hostileToFaction(int factionId) {
		return attackFactions.contains(factionId);
	}

	@Override
	public boolean hostileToNpc(ICustomNpc<?> npc) {
		return attackFactions.contains(npc.getFaction().getId());
	}

	public boolean isAggressiveToNpc(EntityNPCInterface entity) {
		return attackFactions.contains(entity.faction.id) || entity.advanced.attackFactions.contains(id);
	}

	public boolean isAggressiveToPlayer(EntityPlayer player) {
		if (player.capabilities.isCreativeMode) { return false; }
		PlayerFactionData data = PlayerData.get(player).factionData;
		return data.getFactionPoints(player, id) < neutralPoints;
	}

	public boolean isFriendlyToPlayer(EntityPlayer player) {
		PlayerFactionData data = PlayerData.get(player).factionData;
		return data.getFactionPoints(player, id) >= friendlyPoints;
	}

	public boolean isNeutralToPlayer(EntityPlayer player) {
		PlayerFactionData data = PlayerData.get(player).factionData;
		int points = data.getFactionPoints(player, id);
		return points >= neutralPoints && points < friendlyPoints;
	}

	@Override
	public int playerStatus(IPlayer<?> player) {
		PlayerFactionData data = PlayerData.get(player.getMCEntity()).factionData;
		int points = data.getFactionPoints(player.getMCEntity(), id);
		if (points >= friendlyPoints) { return 1; }
		if (points < neutralPoints) { return -1; }
		return 0;
	}

	public void readNBT(NBTTagCompound compound) {
		name = compound.getString("Name");
		if (compound.hasKey("Flag", 8)) { setFlag(compound.getString("Flag")); }
		if (compound.hasKey("Description", 8)) { description = compound.getString("Description"); }
		color = compound.getInteger("Color");
		id = compound.getInteger("Slot");
		neutralPoints = compound.getInteger("NeutralPoints");
		friendlyPoints = compound.getInteger("FriendlyPoints");
		defaultPoints = compound.getInteger("DefaultPoints");
		hideFaction = compound.getBoolean("HideFaction");
		getsAttacked = compound.getBoolean("GetsAttacked");
		attackFactions = NBTTags.getIntegerSet(compound.getTagList("AttackFactions", 10));
		frendFactions = NBTTags.getIntegerSet(compound.getTagList("FrendFactions", 10));
		factions.readFromNBT(compound.getCompoundTag("FactionPoints"));
	}

	@Override
	public void removeHostile(int id) { attackFactions.remove(id); }

	@Override
	public void save() { FactionController.instance.saveFaction(this); }

	@Override
	public void setAttackedByMobs(boolean bo) { getsAttacked = bo; }

	@Override
	public void setDefaultPoints(int points) { defaultPoints = points; }

	@Override
	public void setIsHidden(boolean bo) { hideFaction = bo; }

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("Slot", id);
		compound.setString("Name", name);
		compound.setString("Flag", flag != null ? flag.toString() : "");
		compound.setString("Description", description);
		compound.setInteger("Color", color);
		compound.setInteger("NeutralPoints", neutralPoints);
		compound.setInteger("FriendlyPoints", friendlyPoints);
		compound.setInteger("DefaultPoints", defaultPoints);
		compound.setBoolean("HideFaction", hideFaction);
		compound.setBoolean("GetsAttacked", getsAttacked);
		compound.setTag("AttackFactions", NBTTags.nbtIntegerCollection(attackFactions));
		compound.setTag("FrendFactions", NBTTags.nbtIntegerCollection(frendFactions));
		compound.setTag("FactionPoints", factions.writeToNBT(new NBTTagCompound()));
		return compound;
	}
	
}
