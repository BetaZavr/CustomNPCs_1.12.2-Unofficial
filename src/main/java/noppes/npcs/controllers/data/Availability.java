package noppes.npcs.controllers.data;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemClock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import noppes.npcs.*;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.handler.data.*;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityFaction;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.constants.EnumAvailabilityPlayerName;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumAvailabilityScoreboard;
import noppes.npcs.constants.EnumAvailabilityStoredData;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.api.mixin.scoreboard.IServerScoreboardMixin;
import noppes.npcs.util.ValueUtil;

public class Availability implements ICompatibilty, IAvailability {

	public static HashSet<String> scores = new HashSet<>();
	public int[] daytime = new int[] { 0, 0 };
	public final Map<Integer, EnumAvailabilityDialog> dialogues = new TreeMap<>(); // ID, Availability
	public final Map<Integer, AvailabilityFactionData> factions = new TreeMap<>(); // ID, [Stance, Availability]
	private boolean hasOptions = false;

	public int max = 10;
	public int minPlayerLevel = 0;
	public int health = 100;
	public int healthType = 0;
	public final Map<Integer, EnumAvailabilityQuest> quests = new TreeMap<>(); // ID, Availability
	public final Map<String, AvailabilityScoreboardData> scoreboards = new TreeMap<>(); // Objective, [Value, Availability]
	public final Map<String, EnumAvailabilityPlayerName> playerNames = new TreeMap<>();
	public final List<AvailabilityStoredData> storeddata = new ArrayList<>();
	public final Map<Integer, AvailabilityStack> stacks = new TreeMap<>();
	public boolean onlyGM = false;

	public int version = VersionCompatibility.ModRev;

	private boolean checkHasOptions() {
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
		if (!this.playerNames.isEmpty()) {
			return true;
		}
		if (!this.storeddata.isEmpty()) {
			return true;
		}
		if (this.healthType != 0) {
			return true;
		}
		if (daytime[0] >= 0 && daytime[0] <= 23 && daytime[1] >= 0 && daytime[1] <= 23 && daytime[0] != daytime[1]) {
			return true;
		}
		for (AvailabilityStack aStack : stacks.values()) {
			if (!aStack.getStack().isEmpty()) {
				return true;
			}
		}
		return this.minPlayerLevel > 0 || onlyGM;
	}

	public void clear() {
		this.hasOptions = false;
		this.daytime[0] = 0;
		this.daytime[1] = 0;
		this.minPlayerLevel = 0;
		this.health = 100;
		this.healthType = 0;
		this.dialogues.clear();
		this.quests.clear();
		this.factions.clear();
		this.scoreboards.clear();
		this.playerNames.clear();
	}

	public boolean dialogAvailable(int id, EnumAvailabilityDialog en, EntityPlayer player) {
		if (en == EnumAvailabilityDialog.Always) {
			return true;
		}
		boolean hasRead = PlayerData.get(player).dialogData.dialogsRead.contains(id);
		return (hasRead && en == EnumAvailabilityDialog.After) || (!hasRead && en == EnumAvailabilityDialog.Before);
	}

	public boolean factionAvailable(int id, EnumAvailabilityFaction stance, EnumAvailabilityFactionType available,
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
	public int getHealth() {
		return this.health;
	}

	@Override
	public int getHealthType() {
		return this.healthType;
	}

	@Override
	public int getMinPlayerLevel() {
		return this.minPlayerLevel;
	}

	@Override
	public String[] getPlayerNames() {
		return this.playerNames.keySet().toArray(new String[0]);
	}

	@Override
	public String getStoredDataValue(String key) {
		for (AvailabilityStoredData sd : this.storeddata) {
			if (sd.key.equals(key)) {
				return sd.value;
			}
		}
		return null;
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

	public boolean hasHealth() {
		return this.healthType != 0;
	}

	public boolean hasOptions() {
		return this.hasOptions;
	}

	@Override
	public boolean hasPlayerName(String name) {
		return this.playerNames.containsKey(name);
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

	@Override
	public boolean hasStoredData(String key, String value) {
		for (AvailabilityStoredData sd : this.storeddata) {
			if (sd.key.equals(key) && sd.value.equals(value)) {
				return true;
			}
		}
		return false;
	}

	private void initScore(String objective) {
		if (objective == null || objective.isEmpty()) {
			return;
		}
		Availability.scores.add(objective);
		if (CustomNpcs.Server == null) {
			return;
		}
		for (WorldServer world : CustomNpcs.Server.worlds) {
			ServerScoreboard board = (ServerScoreboard) world.getScoreboard();
			ScoreObjective so = board.getObjective(objective);
			if (so != null) {
				Set<ScoreObjective> addedObjectives = ((IServerScoreboardMixin) board).npcs$getAddedObjectives();
                if (addedObjectives != null && !addedObjectives.contains(so)) {
					board.addObjective(so);
				}
			}
		}
	}

	public boolean isAvailable(EntityPlayer player) {
		if (!this.hasOptions) {
			return true;
		}
		if (daytime[0] >= 0 && daytime[0] <= 23 && daytime[1] >= 0 && daytime[1] <= 23 && daytime[0] != daytime[1]) {
			int time = (int) ((player.world.getWorldTime() + 30000L) % 24000L) / 1000;
			if (daytime[0] < daytime[1]) {
				return time > daytime[0] && time < daytime[1];
			} else {
				return time > daytime[0] || time < daytime[1];
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
		boolean returnName = false;
		boolean hasOnly = false;
		for (String name : this.playerNames.keySet()) {
			boolean exit = false;
			switch (this.playerNames.get(name)) {
			case Only: {
				hasOnly = true;
				if (player.getName().equals(name)) {
					hasOnly = false;
                    exit = true;
				}
				break;
			}
			case Except: {
				if (player.getName().equals(name)) {
					returnName = true;
					exit = true;
				}
				break;
			}
			}
			if (exit) {
				break;
			}
		}
		if (returnName || hasOnly) {
			return false;
		}
		if (!this.storeddata.isEmpty()) {
			IData dataP = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player).getStoreddata();
			for (AvailabilityStoredData sd : this.storeddata) {
				EnumAvailabilityStoredData type = sd.type;
				Object value = dataP.get(sd.key);
				boolean isNumber = false;
				if (type != EnumAvailabilityStoredData.ONLY && type != EnumAvailabilityStoredData.EXCEPT) {
					if (!(value instanceof Number || value instanceof String)) { return false; }
					try {
						double aV = Double.parseDouble(sd.value);
						double dsV = value instanceof Number ? (double) value : Double.parseDouble((String) value);
						if (type == EnumAvailabilityStoredData.EQUAL && dsV != aV) { return false; }
						if (type == EnumAvailabilityStoredData.BIGGER && dsV < aV) { return false; }
						if (type == EnumAvailabilityStoredData.SMALLER && dsV > aV) { return false; }
						isNumber = true;
					}
					catch (Exception e) { return false; }
				}
				if (!isNumber) {
                    if ((dataP.has(sd.key) && type == EnumAvailabilityStoredData.EXCEPT) || (!dataP.has(sd.key) && type == EnumAvailabilityStoredData.ONLY)) { return false; }
				}
			}
		}
		if (this.healthType != 0) {
			int h = (int) (player.getHealth() / player.getMaxHealth() * 100);
			if ((this.healthType == 1 && h < this.health) || (this.healthType == 2 && h > this.health)) {
				return false;
			}
		}
		if (onlyGM && !player.capabilities.isCreativeMode) {
			return false;
		}
		List<Integer> founds = new ArrayList<>();
		for (AvailabilityStack aStack : stacks.values()) {
			int found = -1;
			for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
				if (founds.contains(i)) { continue; }
				ItemStack stack = player.inventory.mainInventory.get(i);
				if (NoppesUtilServer.IsItemStackNull(stack)) { continue; }
				if (NoppesUtilPlayer.compareItems(stack, aStack.getStack(), aStack.getIgnoreDamage(), aStack.getIgnoreNBT())) {
					found = i;
					break;
				}
			}
			if (found < 0) { return false; }
			founds.add(found);
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
		this.clear();

		this.version = compound.getInteger("ModRev");
		VersionCompatibility.CheckAvailabilityCompatibility(this, compound);
		this.minPlayerLevel = compound.getInteger("AvailabilityMinPlayerLevel");

		if (compound.hasKey("AvailabilityDayTime", 11)) {
			this.daytime = compound.getIntArray("AvailabilityDayTime");
		}
		else { // OLD versions
			int v = compound.getInteger("AvailabilityDayTime");
			if (v < 0) {
				v *= -1;
			}
			if (v >= EnumDayTime.values().length) {
				v %= EnumDayTime.values().length;
			}
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

		if (compound.hasKey("AvailabilityDialogs", 9)) {
			for (int d = 0; d < this.max && d < compound.getTagList("AvailabilityDialogs", 10).tagCount(); d++) {
				NBTTagCompound nbtDialog = compound.getTagList("AvailabilityDialogs", 10).getCompoundTagAt(d);
				int v = nbtDialog.getInteger("Availability");
				if (v < 0) {
					v *= -1;
				}
				if (v >= EnumAvailabilityDialog.values().length) {
					v %= EnumAvailabilityDialog.values().length;
				}
				this.dialogues.put(nbtDialog.getInteger("ID"), EnumAvailabilityDialog.values()[v]);
			}
		} else if (compound.hasKey("AvailabilityDialogId", 3)) { // OLD versions
			for (int i = 0; i < 4; i++) {
				String key = i == 0 ? "" : "" + (i + 1);
				if (compound.getInteger("AvailabilityDialog" + key + "Id") > 0) {
					int v = compound.getInteger("AvailabilityDialog" + key);
					if (v < 0) {
						v *= -1;
					}
					if (v >= EnumAvailabilityDialog.values().length) {
						v %= EnumAvailabilityDialog.values().length;
					}
					this.dialogues.put(compound.getInteger("AvailabilityDialog" + key + "Id"),
							EnumAvailabilityDialog.values()[v]);
				}
			}
		}

		if (compound.hasKey("AvailabilityQuests", 9)) {
			for (int q = 0; q < this.max && q < compound.getTagList("AvailabilityQuests", 10).tagCount(); q++) {
				NBTTagCompound nbtQuest = compound.getTagList("AvailabilityQuests", 10).getCompoundTagAt(q);
				int v = nbtQuest.getInteger("Availability");
				if (v < 0) {
					v *= -1;
				}
				if (v >= EnumAvailabilityQuest.values().length) {
					v %= EnumAvailabilityQuest.values().length;
				}
				this.quests.put(nbtQuest.getInteger("ID"), EnumAvailabilityQuest.values()[v]);
			}
		} else if (compound.hasKey("AvailabilityQuestId", 3)) { // OLD versions
			for (int i = 0; i < 4; i++) {
				String key = i == 0 ? "" : "" + (i + 1);
				if (compound.getInteger("AvailabilityQuest" + key + "Id") > 0) {
					int v = compound.getInteger("AvailabilityQuest" + key);
					if (v < 0) {
						v *= -1;
					}
					if (v >= EnumAvailabilityDialog.values().length) {
						v %= EnumAvailabilityDialog.values().length;
					}
					this.dialogues.put(compound.getInteger("AvailabilityQuest" + key + "Id"),
							EnumAvailabilityDialog.values()[v]);
				}
			}
		}

		if (compound.hasKey("AvailabilityFactions", 9)) {
			for (int f = 0; f < this.max && f < compound.getTagList("AvailabilityFactions", 10).tagCount(); f++) {
				NBTTagCompound nbtFaction = compound.getTagList("AvailabilityFactions", 10).getCompoundTagAt(f);
				int v = nbtFaction.getInteger("Stance");
				if (v < 0) {
					v *= -1;
				}
				if (v >= EnumAvailabilityFaction.values().length) {
					v %= EnumAvailabilityFaction.values().length;
				}
				int g = nbtFaction.getInteger("Availability");
				if (g < 0) {
					g *= -1;
				}
				if (g >= EnumAvailabilityFactionType.values().length) {
					v %= EnumAvailabilityFactionType.values().length;
				}
				this.factions.put(nbtFaction.getInteger("ID"), new AvailabilityFactionData(
						EnumAvailabilityFactionType.values()[g], EnumAvailabilityFaction.values()[v]));
			}
		} else if (compound.hasKey("AvailabilityFactionId", 3)) { // OLD versions
			for (int i = 0; i < 4; i++) {
				String key = i == 0 ? "" : "2";
				if (compound.getInteger("AvailabilityFaction" + key + "Id") > 0) {
					int v = compound.getInteger("AvailabilityFaction" + key + "Stance");
					if (v < 0) {
						v *= -1;
					}
					if (v >= EnumAvailabilityFaction.values().length) {
						v %= EnumAvailabilityFaction.values().length;
					}
					int g = compound.getInteger("AvailabilityFaction" + key);
					if (g < 0) {
						g *= -1;
					}
					if (g >= EnumAvailabilityFactionType.values().length) {
						g %= EnumAvailabilityFactionType.values().length;
					}
					this.factions.put(compound.getInteger("AvailabilityFaction" + key + "Id"),
							new AvailabilityFactionData(EnumAvailabilityFactionType.values()[g],
									EnumAvailabilityFaction.values()[v]));
				}
			}
		}

		if (compound.hasKey("AvailabilityScoreboards", 9)) {
			for (int s = 0; s < this.max && s < compound.getTagList("AvailabilityScoreboards", 10).tagCount(); s++) {
				NBTTagCompound nbtScoreboard = compound.getTagList("AvailabilityScoreboards", 10).getCompoundTagAt(s);
				int v = nbtScoreboard.getInteger("Availability");
				if (v < 0) {
					v *= -1;
				}
				v %= EnumAvailabilityScoreboard.values().length;
				this.scoreboards.put(nbtScoreboard.getString("Objective"), new AvailabilityScoreboardData(
						EnumAvailabilityScoreboard.values()[v], nbtScoreboard.getInteger("Value")));
				this.initScore(nbtScoreboard.getString("Objective"));
			}
		} else if (compound.hasKey("AvailabilityScoreboardObjective", 8)) { // OLD versions
			for (int i = 0; i < 2; i++) {
				String key = i == 0 ? "" : "2";
				if (!compound.getString("AvailabilityScoreboard" + key + "Objective").isEmpty()) {
					String objective = compound.getString("AvailabilityScoreboard" + key + "Objective");
					int v = compound.getInteger("AvailabilityScoreboardType" + key);
					if (v < 0) {
						v *= -1;
					}
					v %= EnumAvailabilityScoreboard.values().length;
					this.scoreboards.put(objective,
							new AvailabilityScoreboardData(EnumAvailabilityScoreboard.values()[v],
									compound.getInteger("AvailabilityScoreboard" + key + "Value")));
					this.initScore(objective);
				}
			}
		}

		if (compound.hasKey("AvailabilityPlayerNames", 9)) {
			for (int s = 0; s < compound.getTagList("AvailabilityPlayerNames", 10).tagCount(); s++) {
				NBTTagCompound nbtName = compound.getTagList("AvailabilityPlayerNames", 10).getCompoundTagAt(s);
				int v = compound.getInteger("Availability");
				if (v < 0) {
					v *= -1;
				}
				if (v >= EnumAvailabilityPlayerName.values().length) {
					v %= EnumAvailabilityPlayerName.values().length;
				}
				this.playerNames.put(nbtName.getString("Name"), EnumAvailabilityPlayerName.values()[v]);
			}
		}
		if (compound.hasKey("AvailabilityStoredData", 9)) {
			for (int i = 0; i < compound.getTagList("AvailabilityStoredData", 10).tagCount(); i++) {
				AvailabilityStoredData asd = new AvailabilityStoredData(compound.getTagList("AvailabilityStoredData", 10).getCompoundTagAt(i));
				boolean found = false;
				for (AvailabilityStoredData sd : this.storeddata) {
					if (sd.key.equals(asd.key)) {
						found = true;
						sd.value = asd.value;
						sd.type = asd.type;
						break;
					}
				}
				if (!found) {
					this.storeddata.add(asd);
				}
			}
		}

		if (compound.hasKey("AvailabilityHealth", 3)) {
			this.health = compound.getInteger("AvailabilityHealth");
			if (this.health < 0) {
				this.health = 0;
			}
			if (this.health > 100) {
				this.health = 100;
			}
			this.healthType = compound.getInteger("AvailabilityHealthType");
			if (this.healthType < 0) {
				this.healthType *= -1;
			}
			if (this.healthType > 2) {
				this.healthType = this.healthType % 3;
			}
		}
		onlyGM = compound.getBoolean("OnlyGM");

		stacks.clear();
		if (compound.hasKey("AvailabilityStacks", 9)) {
			NBTTagList list = compound.getTagList("AvailabilityStacks", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				stacks.put(i, (new AvailabilityStack(i)).load(list.getCompoundTagAt(i)));
			}
		}
		fixStacks();

		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removeDialog(int id) {
		this.dialogues.remove(id);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removeFaction(int id) {
		this.factions.remove(id);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removePlayerName(String name) {
		this.playerNames.remove(name);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removeQuest(int id) {
		this.quests.remove(id);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void removeScoreboard(String objective) {
        this.scoreboards.remove(objective);
		for (String obj : this.scoreboards.keySet()) {
			if (obj.equals(objective)) {
				this.scoreboards.remove(obj);
				return;
			}
		}
	}

	@Override
	public void removeStoredData(String key) {
		for (AvailabilityStoredData sd : this.storeddata) {
			if (sd.key.equals(key)) {
				this.storeddata.remove(sd);
				break;
			}
		}
		this.hasOptions = this.checkHasOptions();
	}

	public boolean scoreboardAvailable(EntityPlayer player, String objective, EnumAvailabilityScoreboard type,
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
	public void setHealth(int value, int type) {
		if (value < 0) {
			value = 0;
		}
		if (value > 100) {
			value = 100;
		}
		this.health = value;

		if (type < 0) {
			type *= -1;
		}
		if (type > 2) {
			type = type % 3;
		}
		this.healthType = type;
	}

	@Override
	public void setMinPlayerLevel(int level) {
		this.minPlayerLevel = level;
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setPlayerName(String name, int type) {
		if (type < 0) {
			type *= -1;
		}
		type %= EnumAvailabilityPlayerName.values().length;
		this.playerNames.put(name, EnumAvailabilityPlayerName.values()[type]);
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setQuest(int id, int type) { // Changed
		if (this.quests.size() >= this.max) {
			throw new CustomNPCsException("The maximum number is already set to " + this.max);
		}
		this.quests.put(id, EnumAvailabilityQuest.values()[ValueUtil.correctInt(type, 0, 6)]);
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
		this.scoreboards.put(objective, new AvailabilityScoreboardData(EnumAvailabilityScoreboard.values()[ValueUtil
				.correctInt(type, 0, EnumAvailabilityScoreboard.values().length - 1)], value));
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public void setStoredData(String key, String value, int type) {
		boolean found = false;
		if (type < 0) { type *= -1; }
		EnumAvailabilityStoredData t = EnumAvailabilityStoredData.values()[type % EnumAvailabilityStoredData.values().length];
		for (AvailabilityStoredData sd : this.storeddata) {
			if (sd.key.equals(key)) {
				found = true;
				sd.value = value;
				sd.type = t;
				break;
			}
		}
		if (!found) { this.storeddata.add(new AvailabilityStoredData(key, value, t)); }
		this.hasOptions = this.checkHasOptions();
	}

	@Override
	public boolean getGMOnly() {
		return onlyGM;
	}

	@Override
	public void setGMOnly(boolean gmOnly) { onlyGM = gmOnly; }


	@Override
	public IAvailabilityStack getAvailabilityStack(int id) {
		if (stacks.containsKey(id)) {
			return stacks.get(id);
		}
		return null;
	}

	@Override
	public IAvailabilityStack[] getAvailabilityStacks() {
		return stacks.values().toArray(new IAvailabilityStack[0]);
	}

	@Override
	public IAvailabilityStack addIItemStack(IItemStack item) {
		if (stacks.size() > 8) { return null; }
		int id = stacks.size();
		AvailabilityStack aStack = new AvailabilityStack(id);
		aStack.setStack(item);
		stacks.put(id, aStack);
		fixStacks();
		return aStack;
	}

	@Override
	public void removeIItemStack(int id) {
		if (!stacks.containsKey(id)) { return; }
		stacks.remove(id);
		fixStacks();
	}

	private void fixStacks() {
		int i = 0;
		boolean fix = false;
		Map<Integer, AvailabilityStack> newStacks = new TreeMap<>();
		for (int id : stacks.keySet()) {
			if (i != id) { fix = true;}
			newStacks.put(i, stacks.get(id).setId(i));
			i++;
		}
		if (fix) {
			stacks.clear();
			stacks.putAll(newStacks);
		}
	}


	@Override
	public void setVersion(int version) {
		this.version = version;
	}

	public String toString() {
		return "Availability hasOptions: " + this.hasOptions + ", maxData: " + this.max + ", { scoreboards:"
				+ this.scoreboards.size() + ", dialogues:" + this.dialogues.size() + ", quests:" + this.quests.size()
				+ ", factions:" + this.factions.size() + ", time[min:" + this.daytime[0] + ", max:" + this.daytime[0]
				+ "]" + ", playerNames:" + this.playerNames.size() + ", StoredDatas:" + this.storeddata.size()
				+ ", playerData[Lv:" + this.minPlayerLevel + ", H:" + this.health + ", HT:" + this.healthType + "] }";
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("ModRev", this.version);
		compound.setIntArray("AvailabilityDayTime", this.daytime);
		compound.setInteger("AvailabilityMinPlayerLevel", this.minPlayerLevel);

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

		NBTTagList listPN = new NBTTagList();
		for (String name : this.playerNames.keySet()) {
			NBTTagCompound nbtName = new NBTTagCompound();
			nbtName.setString("Name", name);
			nbtName.setInteger("Availability", this.playerNames.get(name).ordinal());
			listPN.appendTag(nbtName);
		}
		compound.setTag("AvailabilityPlayerNames", listPN);

		NBTTagList listSD = new NBTTagList();
		for (AvailabilityStoredData sd : this.storeddata) {
			listSD.appendTag(sd.writeToNBT());
		}
		compound.setTag("AvailabilityStoredData", listSD);

		compound.setInteger("AvailabilityHealth", this.health);
		compound.setInteger("AvailabilityHealthType", this.healthType);

		compound.setBoolean("OnlyGM", onlyGM);

		NBTTagList listIS = new NBTTagList();
		int i = 0;
		for (AvailabilityStack aStack : stacks.values()) {
			listIS.appendTag(aStack.getNBT(i));
			i++;
		}
		compound.setTag("AvailabilityStacks", listIS);

		return compound;
	}

	public List<String> getAvailability(EntityPlayer player) {
		List<String> list = new ArrayList<>();
		if (!this.hasOptions) { return list; }
		list.add(new TextComponentTranslation("availability.options").getFormattedText()+":");
        StringBuilder data;
        boolean gm = player.capabilities.isCreativeMode;
		// daytime
		if (daytime[0] >= 0 && daytime[0] <= 23 && daytime[1] >= 0 && daytime[1] <= 23 && daytime[0] != daytime[1]) {
			int time = (int) ((player.world.getWorldTime() + 30000L) % 24000L) / 1000;
			boolean bo;
			if (daytime[0] < daytime[1]) { bo = time > daytime[0] && time < daytime[1]; }
			else { bo = time > daytime[0] || time < daytime[1]; }
			boolean hasClock = false;
			if (player.capabilities.isCreativeMode) { hasClock = true; }
			else {
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (!NoppesUtilServer.IsItemStackNull(stack) && stack.getItem() instanceof ItemClock) {
						hasClock = true;
						break;
					}
				}
			}
			if (hasClock) {
				list.add(new TextComponentTranslation("availability.type.daytime.1",
						((char) 167) + "2" + daytime[0]+":00", ((char) 167) + "2" + daytime[1]+":00", ((char) 167) + "6" + time+":00",
						new TextComponentTranslation("quest.task.manual."+(bo ? "0" : "1")).getFormattedText()).getFormattedText());
			} else {
				list.add(new TextComponentTranslation("availability.type.daytime.0",
						new TextComponentTranslation("quest.task.manual."+(bo ? "0" : "1")).getFormattedText()).getFormattedText());
			}
		}
		// dialogue
		if (!this.dialogues.isEmpty()) {
			DialogController dData = DialogController.instance;
			data = new StringBuilder();
			for (int id : this.dialogues.keySet()) {
				if (this.dialogues.get(id) != EnumAvailabilityDialog.Always) { continue; }
				if (data.toString().isEmpty()) { data.append("; "); }
				IDialog d = dData.get(id);
				data.append(new TextComponentTranslation("availability." + this.dialogues.get(id).name().toLowerCase()).getFormattedText()).append(" ");
				if (d == null || gm) {
					data.append((char) 167).append("7ID: ").append(((char) 167)).append("6").append(id).append((char) 167).append("r").append(gm ? " - " : "");
				}
				if (d != null) {
					data.append(new TextComponentTranslation(d.getName()).getFormattedText());
				}
				data.append(new TextComponentTranslation("quest.task.manual."+(this.dialogAvailable(id, this.dialogues.get(id), player) ? "0" : "1")).getFormattedText());
			}
			if (!data.toString().isEmpty()) { list.add(new TextComponentTranslation("availability.type.dialogues", data.toString()).getFormattedText()); }
		}
		// quests
		if (!this.quests.isEmpty()) {
			QuestController qData = QuestController.instance;
			data = new StringBuilder();
			for (int id : this.quests.keySet()) {
				if (this.quests.get(id) != EnumAvailabilityQuest.Always) { continue; }
				if (data.toString().isEmpty()) { data.append("; "); }
				IQuest q = qData.get(id);
				if (q == null || gm) {
					data.append((char) 167).append("7ID: ").append(((char) 167)).append("6").append(id).append((char) 167).append("r").append(gm ? " - " : "");
				}
				data.append(new TextComponentTranslation("availability." + this.quests.get(id).name().toLowerCase()).getFormattedText()).append(" ");
				if (q != null) {
					data.append(q.getTitle());
				}
				data.append(new TextComponentTranslation("quest.task.manual."+(this.questAvailable(id, this.quests.get(id), player) ? "0" : "1")).getFormattedText());
			}
			if (!data.toString().isEmpty()) { list.add(new TextComponentTranslation("availability.type.quests", data.toString()).getFormattedText()); }
		}
		// factions
		if (!this.factions.isEmpty()) {
			FactionController fData = FactionController.instance;
			data = new StringBuilder();
			for (int id : this.factions.keySet()) {
				if (this.factions.get(id).factionAvailable == EnumAvailabilityFactionType.Always) { continue; }
				if (data.toString().isEmpty()) { data.append("; "); }
				IFaction f = fData.get(id);
				if (f == null || gm) {
					data.append((char) 167).append("7ID: ").append(((char) 167)).append("6").append(id).append((char) 167).append("r").append(gm ? " - " : "");
				}
				data.append(new TextComponentTranslation("availability." + this.factions.get(id).factionAvailable.name().toLowerCase()).getFormattedText()).append(" ");
				String attitude = this.factions.get(id).factionStance == EnumAvailabilityFaction.Hostile ? "aggressive": this.factions.get(id).factionAvailable.name().toLowerCase();
				data.append(new TextComponentTranslation("faction.name." + attitude).getFormattedText()).append(" ");
				if (f != null) {
					data.append(f.getName());
				}
				data.append(new TextComponentTranslation("quest.task.manual."+(this.factionAvailable(id, this.factions.get(id).factionStance, this.factions.get(id).factionAvailable, player) ? "0" : "1")).getFormattedText());
			}
			if (!data.toString().isEmpty()) { list.add(new TextComponentTranslation("availability.type.factions", data.toString()).getFormattedText()); }
		}
		// scoreboards
		if (!this.scoreboards.isEmpty()) {
			data = new StringBuilder();
			for (String obj : this.scoreboards.keySet()) {
				if (data.toString().isEmpty()) { data.append("; "); }
				data.append(new TextComponentTranslation("gui.name").getFormattedText()).append(": ").append(obj);
				data.append(new TextComponentTranslation("availability." + this.scoreboards.get(obj).scoreboardType.name().toLowerCase()).getFormattedText()).append(" ").append(this.scoreboards.get(obj).scoreboardValue);
				data.append(new TextComponentTranslation("quest.task.manual."+(this.scoreboardAvailable(player, obj, this.scoreboards.get(obj).scoreboardType, this.scoreboards.get(obj).scoreboardValue) ? "0" : "1")).getFormattedText());
			}
			if (!data.toString().isEmpty()) { list.add(new TextComponentTranslation("availability.type.scoreboards", data.toString()).getFormattedText()); }
		}
		// player names
		if (!this.playerNames.isEmpty()) {
			data = new StringBuilder();
			List<String> listOnly = new ArrayList<>();
			List<String> listExcept = new ArrayList<>();
			for (String name : this.playerNames.keySet()) {
				switch (this.playerNames.get(name)) {
					case Only: {
						listOnly.add(name);
						break;
					}
					case Except: {
						listExcept.add(name);
						break;
					}
				}
			}
			if (!listOnly.isEmpty()) {
				data.append(new TextComponentTranslation("availability.only").getFormattedText()).append("[");
				boolean st = true;
				for (String name : listOnly) {
					if (!st) { data.append("; "); } else { st = false; }
					data.append(name);
				}
				data.append("]").append(new TextComponentTranslation("quest.task.manual."+(listOnly.contains(player.getName()) ? "0" : "1")).getFormattedText());
			}
			if (!listExcept.isEmpty()) {
				data.append(new TextComponentTranslation("availability.except").getFormattedText()).append("[");
				boolean st = true;
				for (String name : listExcept) {
					if (!st) { data.append("; "); } else { st = false; }
					data.append(name);
				}
				data.append("]").append(new TextComponentTranslation("quest.task.manual."+(listExcept.contains(player.getName()) ? "0" : "1")).getFormattedText());
			}
			if (!data.toString().isEmpty()) { list.add(new TextComponentTranslation("availability.type.player.names", data.toString()).getFormattedText()); }
		}
		// storeddata
		if (!this.storeddata.isEmpty()) {
			data = new StringBuilder();
			IData dataP = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player).getStoreddata();
			for (AvailabilityStoredData sd : this.storeddata) {
				EnumAvailabilityStoredData type = sd.type;
				Object value = dataP.get(sd.key);
				boolean isNumber = false;
				boolean bo = true;
				if (type != EnumAvailabilityStoredData.ONLY && type != EnumAvailabilityStoredData.EXCEPT) {
					if (!(value instanceof Number || value instanceof String)) { bo = false; }
					try {
						double aV = Double.parseDouble(sd.value);
						double dsV = value instanceof Number ? (double) value : Double.parseDouble((String) value);
						if (type == EnumAvailabilityStoredData.EQUAL && dsV != aV) { bo = false; }
						if (type == EnumAvailabilityStoredData.BIGGER && dsV < aV) { bo = false; }
						if (type == EnumAvailabilityStoredData.SMALLER && dsV > aV) { bo = false; }
						isNumber = true;
					}
					catch (Exception e) { bo = false; }
				}
				if (!isNumber) {
					if ((dataP.has(sd.key) && type == EnumAvailabilityStoredData.EXCEPT) || (!dataP.has(sd.key) && type == EnumAvailabilityStoredData.ONLY)) { bo = false; }
				}
				if (data.toString().isEmpty()) { data.append("; "); }
				data.append(new TextComponentTranslation("gui.name").getFormattedText()).append(": ").append(sd.key);
				data.append(new TextComponentTranslation("quest.task.item."+(bo ? "0" : "1")).getFormattedText());
			}
			if (!data.toString().isEmpty()) { list.add(new TextComponentTranslation("availability.type.storeddata", data.toString()).getFormattedText()); }
		}
		// stacks
		if (!this.stacks.isEmpty()) {
			data = new StringBuilder();
			boolean st = true;
			for (AvailabilityStack as : stacks.values()) {
				ItemStack stack = as.getStack();
				if (stack.isEmpty()) { continue; }
				if (!st) { data.append("; "); } else { st = false; }
				data.append(stack.getDisplayName());
				if (stack.getCount() > 0) {
					data.append(" x").append(stack.getCount());
				}
			}
			if (!data.toString().isEmpty()) { list.add(new TextComponentTranslation("availability.type.stacks", data.toString()).getFormattedText()); }
		}
		// health
		if (this.healthType != 0) {
			data = new StringBuilder();
			int h = (int) (player.getHealth() / player.getMaxHealth() * 100);
			data.append(new TextComponentTranslation("availability." + (this.healthType == 1 ? "smaller" : "bigger"))).append(" ").append(h).append("%");
			data.append(new TextComponentTranslation("quest.task.item."+((this.healthType == 1 && h < this.health) || (this.healthType == 2 && h > this.health) ? "1" : "0")).getFormattedText());
			list.add(new TextComponentTranslation("availability.type.health", data.toString()).getFormattedText());
		}
		// in creative mode
		if (onlyGM) {
			data = new StringBuilder();
			data.append(new TextComponentTranslation("gui.enabled").getFormattedText());
			data.append(new TextComponentTranslation("quest.task.manual."+(gm ? "0" : "1")).getFormattedText());
			list.add(new TextComponentTranslation("availability.type.only.gm", data.toString()).getFormattedText());
		}
		// xp level
		if (this.minPlayerLevel > 0) {
			data = new StringBuilder();
			data.append(new TextComponentTranslation("availability.bigger").getFormattedText()).append(" ").append(this.minPlayerLevel);
			data.append(new TextComponentTranslation("quest.task.manual."+(player.experienceLevel >= this.minPlayerLevel ? "0" : "1")).getFormattedText());
			list.add(new TextComponentTranslation("availability.type.level", data.toString()).getFormattedText());
		}
		return list;
	}

}
