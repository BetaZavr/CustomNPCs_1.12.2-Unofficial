package noppes.npcs.entity.data;

import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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

	public DataAdvanced(EntityNPCInterface npc) {
		this.npc = npc;
		this.jobInterface = new JobInterface(npc);
		this.roleInterface = new RoleInterface(npc);
		this.scenes = new DataScenes(npc);
	}

	public Line getAttackLine() {
		return this.attackLines.getLine(!this.orderedLines);
	}

	public Line getInteractLine() {
		return this.interactLines.getLine(!this.orderedLines);
	}

	public Line getKilledLine() {
		return this.killedLines.getLine(!this.orderedLines);
	}

	public Line getKillLine() {
		return this.killLines.getLine(!this.orderedLines);
	}

	@Override
	public String getLine(int type, int slot) {
		Lines lines = this.getLines(type);
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
		Lines lines = this.getLines(type);
		return lines == null ? 0 : lines.lines.size();
	}

	private Lines getLines(int type) {
		switch (type) {
		case 0: {
			return this.interactLines;
		}
		case 1: {
			return this.attackLines;
		}
		case 2: {
			return this.worldLines;
		}
		case 3: {
			return this.killedLines;
		}
		case 4: {
			return this.killLines;
		}
		case 5: {
			return this.npcInteractLines;
		}
		}
		return null;
	}

	public Line getNPCInteractLine() {
		return this.npcInteractLines.getLine(!this.orderedLines);
	}

	@Override
	public String getSound(int type) {
		String sound = null;
		switch (type) {
		case 0: {
			sound = this.idleSound;
			break;
		}
		case 1: {
			sound = this.angrySound;
			break;
		}
		case 2: {
			sound = this.hurtSound;
			break;
		}
		case 3: {
			sound = this.deathSound;
			break;
		}
		case 4: {
			sound = this.stepSound;
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
		return this.worldLines.getLine(!this.orderedLines);
	}

	public boolean hasWorldLines() {
		return !this.worldLines.isEmpty();
	}

	private boolean isAggressive(PlayerFactionData data, EntityPlayer player, Faction faction) {
		return data.getFactionPoints(player, faction.id) < faction.neutralPoints;
	}

	public boolean isAggressiveToNpc(EntityNPCInterface entity) {
        return this.attackOtherFactions
                && (this.npc.faction.isAggressiveToNpc(entity) || this.attackFactions.contains(entity.faction.id));
    }

	public boolean isAggressiveToPlayer(EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			return false;
		}
		PlayerFactionData data = PlayerData.get(player).factionData;
		if (this.isAggressive(data, player, this.npc.faction)) {
			return true;
		}
		FactionController fData = FactionController.instance;
		for (int id : this.attackFactions) {
			IFaction faction = fData.get(id);
			if (faction == null) {
				continue;
			}
			if (this.isAggressive(data, player, (Faction) faction)) {
				return true;
			}
		}
		return false;
	}

	public void playSound(int type, float volume, float pitch) {
		String sound = this.getSound(type);
		if (sound == null) {
			return;
		}
		BlockPos pos = this.npc.getPosition();
		Server.sendRangedData(this.npc, 16, EnumPacketClient.PLAY_SOUND, sound, pos.getX(), pos.getY(), pos.getZ(),
				volume, pitch);
	}

	public void readToNBT(NBTTagCompound compound) {
		if (!compound.hasKey("Role")) {
			return;
		}
		this.worldLines.readNBT(compound.getCompoundTag("NpcLines")); // 0
		this.attackLines.readNBT(compound.getCompoundTag("NpcAttackLines")); // 1
		this.interactLines.readNBT(compound.getCompoundTag("NpcInteractLines")); // 2
		this.killedLines.readNBT(compound.getCompoundTag("NpcKilledLines")); // 3
		this.killLines.readNBT(compound.getCompoundTag("NpcKillLines")); // 4
		this.npcInteractLines.readNBT(compound.getCompoundTag("NpcInteractNPCLines")); // 5
		this.orderedLines = compound.getBoolean("OrderedLines");
		this.idleSound = compound.getString("NpcIdleSound");
		this.angrySound = compound.getString("NpcAngrySound");
		this.hurtSound = compound.getString("NpcHurtSound");
		this.deathSound = compound.getString("NpcDeathSound");
		this.stepSound = compound.getString("NpcStepSound");
		this.npc.setFaction(compound.getInteger("FactionID"));
		this.npc.faction = this.npc.getFaction();
		this.attackOtherFactions = compound.getBoolean("AttackOtherFactions");
		this.defendFaction = compound.getBoolean("DefendFaction");
		this.disablePitch = compound.getBoolean("DisablePitch");
		this.factions.readFromNBT(compound.getCompoundTag("FactionPoints"));
		this.scenes.readFromNBT(compound.getCompoundTag("NpcScenes"));

		if (!compound.hasKey("ThroughWalls", 1)) {
			this.throughWalls = true;
		} else {
			this.throughWalls = compound.getBoolean("ThroughWalls");
		}

		if (compound.hasKey("Role", 3) && compound.hasKey("NpcJob", 3)) {
			this.setRole(compound.getInteger("Role"));
			this.setJob(compound.getInteger("NpcJob"));
			this.roleInterface.readFromNBT(compound);
			this.jobInterface.readFromNBT(compound);
		}
		if (compound.hasKey("Role", 10) && compound.hasKey("Job", 10)) {
			this.setRole(compound.getCompoundTag("Role").getInteger("Type"));
			this.setJob(compound.getCompoundTag("Job").getInteger("Type"));
			this.roleInterface.readFromNBT(compound.getCompoundTag("Role"));
			this.jobInterface.readFromNBT(compound.getCompoundTag("Job"));
		}

		if (this.roleInterface instanceof RoleTrader && compound.hasKey("MarketID", 3)) {
			this.roleInterface.readFromNBT(compound);
		}
		if (compound.hasKey("NPCDialogOptions", 11)) {
			this.npc.dialogs = compound.getIntArray("NPCDialogOptions");
		} else if (compound.hasKey("NPCDialogOptions", 9)) {
			this.npc.dialogs = new int[compound.getTagList("NPCDialogOptions", 10).tagCount()];
			for (int i = 0; i < compound.getTagList("NPCDialogOptions", 10).tagCount(); ++i) {
				this.npc.dialogs[i] = compound.getTagList("NPCDialogOptions", 10).getCompoundTagAt(i)
						.getCompoundTag("NPCDialog").getInteger("Dialog");
			}
		}
		this.attackFactions = NBTTags.getIntegerSet(compound.getTagList("AttackFactions", 10));
		this.friendFactions = NBTTags.getIntegerSet(compound.getTagList("FrendFactions", 10));
	}

	public void setJob(int i) {
		JobType.get(i).setToNpc(this.npc);
		if (!this.npc.world.isRemote) {
			this.jobInterface.reset();
		}
	}

	@Override
	public void setLine(int type, int slot, String text, String sound) {
		slot = ValueUtil.correctInt(slot, 0, 7);
		Lines lines = this.getLines(type);
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
		RoleType.get(i).setToNpc(this.npc);
	}

	@Override
	public void setSound(int type, String sound) {
		if (sound == null) {
			sound = "";
		}
		switch (type) {
		case 0:
			this.idleSound = sound;
			break;
		case 1:
			this.angrySound = sound;
			break;
		case 2:
			this.hurtSound = sound;
			break;
		case 3:
			this.deathSound = sound;
			break;
		case 4:
			this.stepSound = sound;
			break;
		}
	}

	public void tryDefendFaction(int id, EntityLivingBase possibleFriend, EntityLivingBase attacked) {
		if (this.npc.isKilled() || !this.defendFaction || possibleFriend.equals(attacked)) {
			return;
		}
		boolean canSee = this.npc.canSee(possibleFriend);
		if (!canSee && this.throughWalls) {
			float dist = this.npc.getDistance(possibleFriend);
			canSee = dist <= this.npc.stats.aggroRange;
		}
		if (!(this.npc.faction.id == id || this.npc.faction.frendFactions.contains(id)
				|| this.friendFactions.contains(id)) || !canSee) {
			return;
		}
		this.npc.onAttack(attacked);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("NpcLines", this.worldLines.writeToNBT()); // 0
		compound.setTag("NpcAttackLines", this.attackLines.writeToNBT()); // 1
		compound.setTag("NpcInteractLines", this.interactLines.writeToNBT()); // 2
		compound.setTag("NpcKilledLines", this.killedLines.writeToNBT()); // 3
		compound.setTag("NpcKillLines", this.killLines.writeToNBT()); // 4
		compound.setTag("NpcInteractNPCLines", this.npcInteractLines.writeToNBT()); // 5
		compound.setBoolean("OrderedLines", this.orderedLines);
		compound.setString("NpcIdleSound", this.idleSound);
		compound.setString("NpcAngrySound", this.angrySound);
		compound.setString("NpcHurtSound", this.hurtSound);
		compound.setString("NpcDeathSound", this.deathSound);
		compound.setString("NpcStepSound", this.stepSound);
		compound.setInteger("FactionID", this.npc.getFaction().id);
		compound.setBoolean("AttackOtherFactions", this.attackOtherFactions);
		compound.setBoolean("DefendFaction", this.defendFaction);
		compound.setBoolean("ThroughWalls", this.throughWalls);
		compound.setBoolean("DisablePitch", this.disablePitch);
		compound.setTag("FactionPoints", this.factions.writeToNBT(new NBTTagCompound()));
		compound.setIntArray("NPCDialogOptions", this.npc.dialogs);
		compound.setTag("NpcScenes", this.scenes.writeToNBT(new NBTTagCompound()));

		NBTTagCompound roleNbt = new NBTTagCompound();
		NBTTagCompound jobNbt = new NBTTagCompound();
		this.jobInterface.writeToNBT(jobNbt);
		this.roleInterface.writeToNBT(roleNbt);
		compound.setTag("Role", roleNbt);
		compound.setTag("Job", jobNbt);
		compound.setTag("AttackFactions", NBTTags.nbtIntegerCollection(this.attackFactions));
		compound.setTag("FrendFactions", NBTTags.nbtIntegerCollection(this.friendFactions));

		return compound;
	}

}
