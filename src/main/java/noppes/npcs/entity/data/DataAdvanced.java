package noppes.npcs.entity.data;

import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.NBTTags;
import noppes.npcs.Server;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.util.ValueUtil;

public class DataAdvanced implements INPCAdvanced {

	public boolean attackOtherFactions = false;
	public boolean defendFaction = false;
	public boolean disablePitch = false;
	public boolean orderedLines = false;
	public boolean throughWalls = true;
	public JobInterface jobInterface;
	public RoleInterface roleInterface;
	private String angrySound = "";
	private String deathSound = "minecraft:entity.player.hurt";
	private String hurtSound = "minecraft:entity.player.hurt";
	private String idleSound = "";
	private String stepSound = "";
	private final EntityNPCInterface npc;
	public Lines interactLines = new Lines();
	public Lines npcInteractLines = new Lines();
	public Lines worldLines = new Lines();
	public Lines attackLines = new Lines();
	public Lines killedLines = new Lines();
	public Lines killLines = new Lines();
	public FactionOptions factions = new FactionOptions();
	public EntityNPCInterface spawner;
	public DataScenes scenes;

	public HashSet<Integer> attackFactions = new HashSet<>();
	public HashSet<Integer> friendFactions = new HashSet<>();

	public DataAdvanced(EntityNPCInterface npcIn) {
		npc = npcIn;
		jobInterface = new JobInterface(npc);
		roleInterface = new RoleInterface(npc);
		scenes = new DataScenes(npc);
	}

	public Line getAttackLine() {
		return attackLines.getLine(!orderedLines);
	}

	public Line getInteractLine() {
		return interactLines.getLine(!orderedLines);
	}

	public Line getKilledLine() {
		return killedLines.getLine(!orderedLines);
	}

	public Line getKillLine() {
		return killLines.getLine(!orderedLines);
	}

	@Override
	public String getLine(int type, int slot) {
		Lines lines = getLines(type);
        if (lines == null) {
            return null;
        }
		Line line = lines.lines.get(slot);
        if (line == null) {
            return null;
        }
        return line.getText();
    }

	@Override
	public int getLineCount(int type) {
		Lines lines = getLines(type);
		return lines == null ? 0 : lines.lines.size();
	}

	private Lines getLines(int type) {
		switch (type) {
		case 0: {
			return interactLines;
		}
		case 1: {
			return attackLines;
		}
		case 2: {
			return worldLines;
		}
		case 3: {
			return killedLines;
		}
		case 4: {
			return killLines;
		}
		case 5: {
			return npcInteractLines;
		}
		}
		return null;
	}

	public Line getNPCInteractLine() {
		return npcInteractLines.getLine(!orderedLines);
	}

	@Override
	public String getSound(int type) {
		String sound = null;
		switch (type) {
		case 0: {
			sound = idleSound;
			break;
		}
		case 1: {
			sound = angrySound;
			break;
		}
		case 2: {
			sound = hurtSound;
			break;
		}
		case 3: {
			sound = deathSound;
			break;
		}
		case 4: {
			sound = stepSound;
			break;
		}
		default: {
			break;
		}
		}
		if (sound != null && sound.isEmpty()) {
			sound = null;
		}
		return sound;
	}

	public Line getWorldLine() {
		return worldLines.getLine(!orderedLines);
	}

	public boolean hasWorldLines() {
		return !worldLines.isEmpty();
	}

	private boolean isAggressive(PlayerFactionData data, EntityPlayer player, Faction faction) {
		return data.getFactionPoints(player, faction.id) < faction.neutralPoints;
	}

	public boolean isAggressiveToNpc(EntityNPCInterface entity) {
        return attackOtherFactions && (npc.faction.isAggressiveToNpc(entity) || attackFactions.contains(entity.faction.id));
    }

	public boolean isAggressiveToPlayer(EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			return false;
		}
		PlayerFactionData data = PlayerData.get(player).factionData;
		if (isAggressive(data, player, npc.faction)) {
			return true;
		}
		FactionController fData = FactionController.instance;
		for (int id : attackFactions) {
			IFaction faction = fData.get(id);
			if (faction == null) {
				continue;
			}
			if (isAggressive(data, player, (Faction) faction)) {
				return true;
			}
		}
		return false;
	}

	public void playSound(int type, float volume, float pitch) {
		String sound = getSound(type);
		if (sound == null) {
			return;
		}
		BlockPos pos = npc.getPosition();
		Server.sendRangedData(npc, 16, EnumPacketClient.PLAY_SOUND, sound, pos.getX(), pos.getY(), pos.getZ(), volume, pitch);
	}

	public void load(NBTTagCompound compound) {
		if (!compound.hasKey("Role")) { return; }
		worldLines.readNBT(compound.getCompoundTag("NpcLines")); // 0
		attackLines.readNBT(compound.getCompoundTag("NpcAttackLines")); // 1
		interactLines.readNBT(compound.getCompoundTag("NpcInteractLines")); // 2
		killedLines.readNBT(compound.getCompoundTag("NpcKilledLines")); // 3
		killLines.readNBT(compound.getCompoundTag("NpcKillLines")); // 4
		npcInteractLines.readNBT(compound.getCompoundTag("NpcInteractNPCLines")); // 5
		orderedLines = compound.getBoolean("OrderedLines");
		idleSound = compound.getString("NpcIdleSound");
		angrySound = compound.getString("NpcAngrySound");
		hurtSound = compound.getString("NpcHurtSound");
		deathSound = compound.getString("NpcDeathSound");
		stepSound = compound.getString("NpcStepSound");
		npc.setFaction(compound.getInteger("FactionID"));
		npc.faction = npc.getFaction();
		attackOtherFactions = compound.getBoolean("AttackOtherFactions");
		defendFaction = compound.getBoolean("DefendFaction");
		disablePitch = compound.getBoolean("DisablePitch");
		factions.load(compound.getCompoundTag("FactionPoints"));
		scenes.readFromNBT(compound.getCompoundTag("NpcScenes"));

		if (!compound.hasKey("ThroughWalls", 1)) {
			throughWalls = true;
		} else {
			throughWalls = compound.getBoolean("ThroughWalls");
		}

		if (compound.hasKey("Role", 3) && compound.hasKey("NpcJob", 3)) {
			setRole(compound.getInteger("Role"));
			setJob(compound.getInteger("NpcJob"));
			roleInterface.load(compound);
			jobInterface.load(compound);
		}
		if (compound.hasKey("Role", 10) && compound.hasKey("Job", 10)) {
			setRole(compound.getCompoundTag("Role").getInteger("Type"));
			setJob(compound.getCompoundTag("Job").getInteger("Type"));
			roleInterface.load(compound.getCompoundTag("Role"));
			jobInterface.load(compound.getCompoundTag("Job"));
		}

		if (roleInterface instanceof RoleTrader && compound.hasKey("MarketID", 3)) {
			roleInterface.load(compound);
		}
		if (compound.hasKey("NPCDialogOptions", 11)) {
			npc.dialogs = compound.getIntArray("NPCDialogOptions");
		}
		else if (compound.hasKey("NPCDialogOptions", 9)) {
			NBTTagList list = compound.getTagList("NPCDialogOptions", 10);
			npc.dialogs = new int[list.tagCount()];
			for (int i = 0; i < list.tagCount(); ++i) {
				npc.dialogs[i] = list.getCompoundTagAt(i).getCompoundTag("NPCDialog").getInteger("Dialog");
			}
		}
		attackFactions = NBTTags.getIntegerSet(compound.getTagList("AttackFactions", 10));
		friendFactions = NBTTags.getIntegerSet(compound.getTagList("FrendFactions", 10));
	}

	public void setJob(int i) {
		JobType.get(i).setToNpc(npc);
		if (!npc.world.isRemote) {
			jobInterface.reset();
		}
	}

	@Override
	public void setLine(int type, int slot, String text, String sound) {
		slot = ValueUtil.correctInt(slot, 0, 7);
		Lines lines = getLines(type);
		if (lines == null) { return; }
		if (text == null || text.isEmpty()) {
			lines.lines.remove(slot);
		} else {
            Line line = lines.lines.computeIfAbsent(slot, k -> new Line());
            line.setText(text);
			line.setSound(sound);
		}
	}

	public void setRole(int i) {
		RoleType.get(i).setToNpc(npc);
	}

	@Override
	public void setSound(int type, String sound) {
		if (sound == null) {
			sound = "";
		}
		switch (type) {
		case 0:
			idleSound = sound;
			break;
		case 1:
			angrySound = sound;
			break;
		case 2:
			hurtSound = sound;
			break;
		case 3:
			deathSound = sound;
			break;
		case 4:
			stepSound = sound;
			break;
		}
	}

	public void tryDefendFaction(int id, EntityLivingBase possibleFriend, EntityLivingBase attacked) {
		if (npc.isKilled() || !defendFaction || possibleFriend.equals(attacked)) {
			return;
		}
		boolean canSee = npc.canSee(possibleFriend);
		if (!canSee && throughWalls) {
			float dist = npc.getDistance(possibleFriend);
			canSee = dist <= npc.stats.aggroRange;
		}
		if (!(npc.faction.id == id || npc.faction.frendFactions.contains(id) || friendFactions.contains(id)) || !canSee) {
			return;
		}
		npc.onAttack(attacked);
	}

	public NBTTagCompound save(NBTTagCompound compound) {
		compound.setTag("NpcLines", worldLines.writeToNBT()); // 0
		compound.setTag("NpcAttackLines", attackLines.writeToNBT()); // 1
		compound.setTag("NpcInteractLines", interactLines.writeToNBT()); // 2
		compound.setTag("NpcKilledLines", killedLines.writeToNBT()); // 3
		compound.setTag("NpcKillLines", killLines.writeToNBT()); // 4
		compound.setTag("NpcInteractNPCLines", npcInteractLines.writeToNBT()); // 5
		compound.setBoolean("OrderedLines", orderedLines);
		compound.setString("NpcIdleSound", idleSound);
		compound.setString("NpcAngrySound", angrySound);
		compound.setString("NpcHurtSound", hurtSound);
		compound.setString("NpcDeathSound", deathSound);
		compound.setString("NpcStepSound", stepSound);
		compound.setInteger("FactionID", npc.getFaction().id);
		compound.setBoolean("AttackOtherFactions", attackOtherFactions);
		compound.setBoolean("DefendFaction", defendFaction);
		compound.setBoolean("ThroughWalls", throughWalls);
		compound.setBoolean("DisablePitch", disablePitch);
		compound.setTag("FactionPoints", factions.save(new NBTTagCompound()));
		compound.setIntArray("NPCDialogOptions", npc.dialogs);
		compound.setTag("NpcScenes", scenes.writeToNBT(new NBTTagCompound()));

		NBTTagCompound roleNbt = new NBTTagCompound();
		NBTTagCompound jobNbt = new NBTTagCompound();
		roleInterface.save(roleNbt);
		jobInterface.save(jobNbt);
		compound.setTag("Role", roleNbt);
		compound.setTag("Job", jobNbt);
		compound.setTag("AttackFactions", NBTTags.nbtIntegerCollection(attackFactions));
		compound.setTag("FrendFactions", NBTTags.nbtIntegerCollection(friendFactions));

		return compound;
	}

}
