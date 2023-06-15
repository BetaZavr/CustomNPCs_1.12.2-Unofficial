package noppes.npcs.controllers.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ICompatibilty;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityFaction;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumAvailabilityScoreboard;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.util.ObfuscationHelper;
import noppes.npcs.util.ValueUtil;

public class Availability
implements ICompatibilty, IAvailability {
	
	public static HashSet<String> scores = new HashSet<String>();
	public int[] daytime;
	public Map<Integer, EnumAvailabilityDialog> dialogues; // ID, Availability
	public Map<Integer, AvailabilityFactionData> factions; // ID, [Stance, Availability]
	private boolean hasOptions;
	// New
	public int max = 10;
	public int minPlayerLevel;
	public Map<Integer, EnumAvailabilityQuest> quests; // ID, Availability
	public Map<String, AvailabilityScoreboardData> scoreboards; // Objective, [Value, Availability]
	public int version;
	public int health = 100;
	public int healthType = 0;

	public Availability() {
		this.version = VersionCompatibility.ModRev;
		this.daytime = new int[] { 0, 0 };
		this.minPlayerLevel = 0;
		this.hasOptions = false;
		// New
		this.dialogues = new HashMap<Integer, EnumAvailabilityDialog>();
		this.quests = new HashMap<Integer, EnumAvailabilityQuest>();
		this.factions = new HashMap<Integer, AvailabilityFactionData>();
		this.scoreboards = new HashMap<String, AvailabilityScoreboardData>();
	}

	private boolean checkHasOptions() { // Changed
		for (EnumAvailabilityDialog ead : this.dialogues.values()) {
			if (ead != EnumAvailabilityDialog.Always) {
				return true;
			}
		}
		for (EnumAvailabilityQuest eaq : this.quests.values()) {
			if (eaq != EnumAvailabilityQuest.Always) {
				return true;
			}
		}
		for (AvailabilityFactionData afd : this.factions.values()) {
			if (afd.factionAvailable != EnumAvailabilityFactionType.Always) {
				return true;
			}
		}
		for (String obj : this.scoreboards.keySet()) {
			if (!obj.isEmpty()) {
				return true;
			}
		}
		if (this.healthType!=0) { return true; }
		return this.daytime[0] != -1 || this.daytime[1] != -1 || this.minPlayerLevel > 0;
	}

	public boolean dialogAvailable(int id, EnumAvailabilityDialog en, EntityPlayer player) {
		if (en == EnumAvailabilityDialog.Always) {
			return true;
		}
		boolean hasRead = PlayerData.get(player).dialogData.dialogsRead.contains(id);
		return (hasRead && en == EnumAvailabilityDialog.After) || (!hasRead && en == EnumAvailabilityDialog.Before);
	}

	private boolean factionAvailable(int id, EnumAvailabilityFaction stance, EnumAvailabilityFactionType available,
			EntityPlayer player) {
		if (available == EnumAvailabilityFactionType.Always) {
			return true;
		}
		Faction faction = FactionController.instance.getFaction(id);
		if (faction == null) {
			return true;
		}
		PlayerFactionData data = PlayerData.get(player).factionData;
		int points = data.getFactionPoints(player, id);
		EnumAvailabilityFaction current = EnumAvailabilityFaction.Neutral;
		if (points < faction.neutralPoints) {
			current = EnumAvailabilityFaction.Hostile;
		}
		if (points >= faction.friendlyPoints) {
			current = EnumAvailabilityFaction.Friendly;
		}
		return (available == EnumAvailabilityFactionType.Is && stance == current)
				|| (available == EnumAvailabilityFactionType.IsNot && stance != current);
	}

	@Override
	public int[] getDaytime() {
		return this.daytime;
	}

	@Override
	public int getMinPlayerLevel() {
		return this.minPlayerLevel;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public boolean hasDialog(int id) {
		return this.dialogues.containsKey(id);
	}

	@Override
	public boolean hasFaction(int id) {
		return this.factions.containsKey(id);
	}

	public boolean hasOptions() {
		return this.hasOptions;
	}
	
	public boolean hasHealth() {
		return this.healthType!=0;
	}

	@Override
	public boolean hasQuest(int id) {
		return this.quests.containsKey(id);
	}

	@Override
	public boolean hasScoreboard(String objective) {
		if (this.scoreboards.containsKey(objective)) {
			return true;
		}
		for (String obj : this.scoreboards.keySet()) {
			if (obj.equals(objective)) {
				return true;
			}
		}
		return false;
	}

	private void initScore(String objective) {
		if (objective == null || objective.isEmpty()) {
			return;
		}
		if (CustomNpcs.Server == null) {
			return;
		}
		Availability.scores.add(objective);
		for (WorldServer world : CustomNpcs.Server.worlds) {
			ServerScoreboard board = (ServerScoreboard) world.getScoreboard();
			ScoreObjective so = board.getObjective(objective);
			if (so != null) {
				Set<ScoreObjective> addedObjectives = ObfuscationHelper.getValue(ServerScoreboard.class, board, 1);
				if (!addedObjectives.contains(so)) {
					board.addObjective(so);
				}
			}
		}
	}

	public boolean isAvailable(EntityPlayer player) { // Changed
		if (!this.hasOptions) {
			return true;
		}
		if (this.daytime[0] != 0 && this.daytime[1] != 0 && this.daytime[0] != this.daytime[1]) {
			int time = (int) ((player.world.getWorldTime() + 30000L) % 24000L) / 1000;
			if (time < this.daytime[0] || time > this.daytime[1]) {
				return false;
			}
		}
		for (int id : this.dialogues.keySet()) {
			if (!this.dialogAvailable(id, this.dialogues.get(id), player)) {
				return false;
			}
		}
		for (int id : this.quests.keySet()) {
			if (!this.questAvailable(id, this.quests.get(id), player)) {
				return false;
			}
		}
		for (int id : this.factions.keySet()) {
			if (!this.factionAvailable(id, this.factions.get(id).factionStance, this.factions.get(id).factionAvailable,
					player)) {
				return false;
			}
		}
		for (String obj : this.scoreboards.keySet()) {
			if (!this.scoreboardAvailable(player, obj, this.scoreboards.get(obj).scoreboardType,
					this.scoreboards.get(obj).scoreboardValue)) {
				return false;
			}
		}
		if (this.healthType!=0) {
			int h = (int) (player.getHealth() / player.getMaxHealth()*100);
			if ((this.healthType==1 && h<this.health) || (this.healthType==2 && h>this.health)) {
				return false;
			}
		}
		return player.experienceLevel >= this.minPlayerLevel;
	}

	@Override
	public boolean isAvailable(IPlayer<?> player) {
		return this.isAvailable(player.getMCEntity());
	}

	public boolean questAvailable(int id, EnumAvailabilityQuest en, EntityPlayer player) {
		switch (en) {
		case Always: {
			return true;
		}
		case After: {
			return PlayerQuestController.isQuestFinished(player, id);
		}
		case Before: {
			return !PlayerQuestController.isQuestFinished(player, id);
		}
		case Active: {
			return PlayerQuestController.isQuestActive(player, id);
		}
		case NotActive: {
			return !PlayerQuestController.isQuestActive(player, id);
		}
		case Completed: {
			return PlayerQuestController.isQuestCompleted(player, id);
		}
		case CanStart: {
			return PlayerQuestController.canQuestBeAccepted(player, id);
		}
		default: {
		}
		}
		return false;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.version = compound.getInteger("ModRev");
		VersionCompatibility.CheckAvailabilityCompatibility(this, compound);
		this.minPlayerLevel = compound.getInteger("AvailabilityMinPlayerLevel");
		// New
		if (compound.hasKey("AvailabilityDayTime", 11)) {
			this.daytime = compound.getIntArray("AvailabilityDayTime");
		} else if (CustomNpcs.FixUpdateFromPre_1_12) { // OLD versions
			int v = compound.getInteger("AvailabilityDayTime");
			if (v<0) { v *= -1; }
			if (v>=EnumDayTime.values().length) { v %= EnumDayTime.values().length; }
			switch (EnumDayTime.values()[v]) {
				case Night: {
					this.daytime[0] = 18;
					this.daytime[1] = 6;
					break;
				}
				case Day: {
					this.daytime[0] = 6;
					this.daytime[1] = 18;
					break;
				}
				default: {
					this.daytime[0] = 0;
					this.daytime[1] = 0;
				}
			}
		}

		this.dialogues.clear();
		if (compound.hasKey("AvailabilityDialogs", 9)) {
			for (int d = 0; d < this.max && d < compound.getTagList("AvailabilityDialogs", 10).tagCount(); d++) {
				NBTTagCompound nbtDialog = compound.getTagList("AvailabilityDialogs", 10).getCompoundTagAt(d);
				int v = nbtDialog.getInteger("Availability");
				if (v<0) { v *= -1; }
				if (v>=EnumAvailabilityDialog.values().length) { v %= EnumAvailabilityDialog.values().length; }
				this.dialogues.put(nbtDialog.getInteger("ID"), EnumAvailabilityDialog.values()[v]);
			}
		} else if (compound.hasKey("AvailabilityDialogId", 3) && CustomNpcs.FixUpdateFromPre_1_12) { // OLD versions
			for (int i = 0; i < 4; i++) {
				String key = i == 0 ? "" : "" + (i+1);
				if (compound.getInteger("AvailabilityDialog" + key + "Id") > 0) {
					int v = compound.getInteger("AvailabilityDialog" + key);
					if (v<0) { v *= -1; }
					if (v>=EnumAvailabilityDialog.values().length) { v %= EnumAvailabilityDialog.values().length; }
					this.dialogues.put(compound.getInteger("AvailabilityDialog" + key + "Id"), EnumAvailabilityDialog.values()[v]);
				}
			}
		}

		this.quests.clear();
		if (compound.hasKey("AvailabilityQuests", 9)) {
			for (int q = 0; q < this.max && q < compound.getTagList("AvailabilityQuests", 10).tagCount(); q++) {
				NBTTagCompound nbtQuest = compound.getTagList("AvailabilityQuests", 10).getCompoundTagAt(q);
				int v = nbtQuest.getInteger("Availability");
				if (v<0) { v *= -1; }
				if (v>=EnumAvailabilityQuest.values().length) { v %= EnumAvailabilityQuest.values().length; }
				this.quests.put(nbtQuest.getInteger("ID"), EnumAvailabilityQuest.values()[v]);
			}
		} else if (compound.hasKey("AvailabilityQuestId", 3) && CustomNpcs.FixUpdateFromPre_1_12) { // OLD versions
			for (int i = 0; i < 4; i++) {
				String key = i == 0 ? "" : "" + (i+1);
				if (compound.getInteger("AvailabilityQuest" + key + "Id") > 0) {
					int v = compound.getInteger("AvailabilityQuest" + key);
					if (v<0) { v *= -1; }
					if (v>=EnumAvailabilityDialog.values().length) { v %= EnumAvailabilityDialog.values().length; }
					this.dialogues.put(compound.getInteger("AvailabilityQuest" + key + "Id"), EnumAvailabilityDialog.values()[v]);
				}
			}
		}

		this.factions.clear();
		if (compound.hasKey("AvailabilityFactions", 9)) {
			for (int f = 0; f < this.max && f < compound.getTagList("AvailabilityFactions", 10).tagCount(); f++) {
				NBTTagCompound nbtFaction = compound.getTagList("AvailabilityFactions", 10).getCompoundTagAt(f);
				int v = nbtFaction.getInteger("Stance");
				if (v<0) { v *= -1; }
				if (v>=EnumAvailabilityFaction.values().length) { v %= EnumAvailabilityFaction.values().length; }
				int g = nbtFaction.getInteger("Availability");
				if (g<0) { g *= -1; }
				if (g>=EnumAvailabilityFactionType.values().length) { v %= EnumAvailabilityFactionType.values().length; }
				this.factions.put(nbtFaction.getInteger("ID"),
						new AvailabilityFactionData(EnumAvailabilityFactionType.values()[g], EnumAvailabilityFaction.values()[v]));
			}
		} else if (compound.hasKey("AvailabilityFactionId", 3) && CustomNpcs.FixUpdateFromPre_1_12) { // OLD versions
			for (int i = 0; i < 4; i++) {
				String key = i == 0 ? "" : "2";
				if (compound.getInteger("AvailabilityFaction" + key + "Id") > 0) {
					int v = compound.getInteger("AvailabilityFaction" + key + "Stance");
					if (v<0) { v *= -1; }
					if (v>=EnumAvailabilityFaction.values().length) { v %= EnumAvailabilityFaction.values().length; }
					int g = compound.getInteger("AvailabilityFaction" + key);
					if (g<0) { g *= -1; }
					if (g>=EnumAvailabilityFactionType.values().length) { g %= EnumAvailabilityFactionType.values().length; }
					this.factions.put(compound.getInteger("AvailabilityFaction" + key + "Id"),
							new AvailabilityFactionData(
									EnumAvailabilityFactionType.values()[g],
									EnumAvailabilityFaction.values()[v]));
				}
			}
		}

		this.scoreboards.clear();
		if (compound.hasKey("AvailabilityScoreboards", 9)) {
			for (int s = 0; s < this.max && s < compound.getTagList("AvailabilityScoreboards", 10).tagCount(); s++) {
				NBTTagCompound nbtScoreboard = compound.getTagList("AvailabilityScoreboards", 10).getCompoundTagAt(s);
				int v = compound.getInteger("Availability");
				if (v<0) { v *= -1; }
				if (v>=EnumAvailabilityScoreboard.values().length) { v %= EnumAvailabilityScoreboard.values().length; }
				this.scoreboards.put(nbtScoreboard.getString("Objective"),
						new AvailabilityScoreboardData(
								EnumAvailabilityScoreboard.values()[v],
								nbtScoreboard.getInteger("Value")));
				this.initScore(nbtScoreboard.getString("Objective"));
			}
		} else if (compound.hasKey("AvailabilityScoreboardObjective", 8) && CustomNpcs.FixUpdateFromPre_1_12) { // OLD versions
			for (int i = 0; i < 2; i++) {
				String key = i == 0 ? "" : "2";
				if (!compound.getString("AvailabilityScoreboard" + key + "Objective").isEmpty()) {
					String objective = compound.getString("AvailabilityScoreboard" + key + "Objective");
					int v = compound.getInteger("AvailabilityScoreboardType" + key);
					if (v<0) { v *= -1; }
					if (v>=EnumAvailabilityScoreboard.values().length) { v %= EnumAvailabilityScoreboard.values().length; }
					this.scoreboards.put(objective,
							new AvailabilityScoreboardData(
									EnumAvailabilityScoreboard.values()[v],
									compound.getInteger("AvailabilityScoreboard" + key + "Value")));
					this.initScore(objective);
				}
			}
		}
		this.hasOptions = this.checkHasOptions();
		
		if (compound.hasKey("AvailabilityHealth", 3)) {
			this.health = compound.getInteger("AvailabilityHealth");
			if (this.health<0) { this.health = 0; }
			if (this.health>100) { this.health = 100; }
			this.healthType = compound.getInteger("AvailabilityHealthType");
			if (this.healthType<0) { this.healthType *= -1; }
			if (this.healthType>2) { this.healthType = this.healthType%3; }
		}
	}

	@Override
	public void removeDialog(int id) { // Changed
		this.dialogues.remove(id);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removeFaction(int id) { // Changed
		this.factions.remove(id);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removeQuest(int id) { // Changed
		this.quests.remove(id);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removeScoreboard(String objective) {
		if (this.scoreboards.containsKey(objective)) {
			this.scoreboards.remove(objective);
		}
		for (String obj : this.scoreboards.keySet()) {
			if (obj.equals(objective)) {
				this.scoreboards.remove(obj);
				return;
			}
		}
	}

	private boolean scoreboardAvailable(EntityPlayer player, String objective, EnumAvailabilityScoreboard type,
			int value) {
		if (objective.isEmpty()) {
			return true;
		}
		ScoreObjective sbObjective = player.getWorldScoreboard().getObjective(objective);
		if (sbObjective == null) {
			return false;
		}
		if (!player.getWorldScoreboard().entityHasObjective(player.getName(), sbObjective)) {
			return false;
		}
		int i = player.getWorldScoreboard().getOrCreateScore(player.getName(), sbObjective).getScorePoints();
		if (type == EnumAvailabilityScoreboard.EQUAL) {
			return i == value;
		}
		if (type == EnumAvailabilityScoreboard.BIGGER) {
			return i > value;
		}
		return i < value;
	}

	@Override
	public void setDaytime(int type) {
		switch (EnumDayTime.values()[MathHelper.clamp(type, 0, 2)]) {
		case Night: {
			this.daytime[0] = 18;
			this.daytime[1] = 6;
			break;
		}
		case Day: {
			this.daytime[0] = 6;
			this.daytime[1] = 18;
			break;
		}
		default: {
			this.daytime[0] = 0;
			this.daytime[1] = 0;
		}
		}
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setDaytime(int minHour, int maxHour) {
		this.daytime[0] = minHour;
		this.daytime[1] = maxHour;
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setDialog(int id, int type) { // Changed
		if (this.dialogues.size() >= this.max) {
			throw new CustomNPCsException("The maximum number is already set to " + this.max);
		}
		this.dialogues.put(id, EnumAvailabilityDialog.values()[ValueUtil.correctInt(type, 0, 2)]);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setFaction(int id, int type, int stance) {
		if (this.factions.size() >= this.max) {
			throw new CustomNPCsException("The maximum number is already set to " + this.max);
		}
		this.factions.put(id,
				new AvailabilityFactionData(EnumAvailabilityFactionType.values()[ValueUtil.correctInt(type, 0, 2)],
						EnumAvailabilityFaction.values()[ValueUtil.correctInt(stance, 0, 2)]));
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setMinPlayerLevel(int level) {
		this.minPlayerLevel = level;
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setQuest(int id, int type) { // Changed
		if (this.quests.size() >= this.max) {
			throw new CustomNPCsException("The maximum number is already set to " + this.max);
		}
		this.quests.put(id, EnumAvailabilityQuest.values()[ValueUtil.correctInt(type, 0, 2)]);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setScoreboard(String objective, int type, int value) {
		if (this.scoreboards.size() >= this.max) {
			throw new CustomNPCsException("The maximum number is already set to " + this.max);
		}
		if (objective == null || objective.isEmpty()) {
			throw new CustomNPCsException("Objective must not be empty");
		}
		this.scoreboards.put(objective, new AvailabilityScoreboardData(
				EnumAvailabilityScoreboard.values()[ValueUtil.correctInt(type, 0, 2)], value));
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public int getHealth() { return this.health; }
	
	@Override
	public int getHealthType() { return this.healthType; }
	
	@Override
	public void setHealth(int value, int type) {
		if (value<0) { value = 0; }
		if (value>100) { value = 100; }
		this.health = value;
		
		if (type<0) { type *= -1; }
		if (type>2) { type = type%3; }
		this.healthType = type;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("ModRev", this.version);
		compound.setIntArray("AvailabilityDayTime", this.daytime);
		compound.setInteger("AvailabilityMinPlayerLevel", this.minPlayerLevel);
		// New
		NBTTagList listD = new NBTTagList();
		for (int id : this.dialogues.keySet()) {
			NBTTagCompound nbtDialog = new NBTTagCompound();
			nbtDialog.setInteger("ID", id);
			nbtDialog.setInteger("Availability", this.dialogues.get(id).ordinal());
			listD.appendTag(nbtDialog);
		}
		compound.setTag("AvailabilityDialogs", listD);
		NBTTagList listQ = new NBTTagList();
		for (int id : this.quests.keySet()) {
			NBTTagCompound nbtQuest = new NBTTagCompound();
			nbtQuest.setInteger("ID", id);
			nbtQuest.setInteger("Availability", this.quests.get(id).ordinal());
			listQ.appendTag(nbtQuest);
		}
		compound.setTag("AvailabilityQuests", listQ);
		NBTTagList listF = new NBTTagList();
		for (int id : this.factions.keySet()) {
			NBTTagCompound nbtFaction = new NBTTagCompound();
			nbtFaction.setInteger("ID", id);
			nbtFaction.setInteger("Availability", this.factions.get(id).factionAvailable.ordinal());
			nbtFaction.setInteger("Stance", this.factions.get(id).factionStance.ordinal());
			listF.appendTag(nbtFaction);
		}
		compound.setTag("AvailabilityFactions", listF);

		NBTTagList listS = new NBTTagList();
		for (String obj : this.scoreboards.keySet()) {
			NBTTagCompound nbtScoreboard = new NBTTagCompound();
			nbtScoreboard.setString("Objective", obj);
			nbtScoreboard.setInteger("Availability", this.scoreboards.get(obj).scoreboardType.ordinal());
			nbtScoreboard.setInteger("Value", this.scoreboards.get(obj).scoreboardValue);
			listS.appendTag(nbtScoreboard);
		}
		compound.setTag("AvailabilityScoreboards", listS);
		
		compound.setInteger("AvailabilityHealth", this.health);
		compound.setInteger("AvailabilityHealthType", this.healthType);
		return compound;
	}

}
