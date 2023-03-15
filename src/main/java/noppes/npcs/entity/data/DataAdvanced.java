package noppes.npcs.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.Server;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.roles.JobChunkLoader;
import noppes.npcs.roles.JobConversation;
import noppes.npcs.roles.JobFarmer;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.roles.JobPuppet;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleBank;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleDialog;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RolePostman;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.ValueUtil;

public class DataAdvanced
implements INPCAdvanced {
	
	private String angrySound;
	public Lines attackLines;
	public boolean attackOtherFactions;
	private String deathSound;
	public boolean defendFaction;
	public boolean disablePitch;
	public FactionOptions factions;
	private String hurtSound;
	private String idleSound;
	public Lines interactLines;
	public int job;
	public Lines killedLines;
	public Lines killLines;
	private EntityNPCInterface npc;
	public Lines npcInteractLines;
	public boolean orderedLines;
	public int role;
	public DataScenes scenes;
	private String stepSound;
	public Lines worldLines;
	public EntityNPCInterface spawner;

	public DataAdvanced(EntityNPCInterface npc) {
		this.interactLines = new Lines();
		this.worldLines = new Lines();
		this.attackLines = new Lines();
		this.killedLines = new Lines();
		this.killLines = new Lines();
		this.npcInteractLines = new Lines();
		this.orderedLines = false;
		this.idleSound = "";
		this.angrySound = "";
		this.hurtSound = "minecraft:entity.player.hurt";
		this.deathSound = "minecraft:entity.player.hurt";
		this.stepSound = "";
		this.factions = new FactionOptions();
		this.role = 0;
		this.job = 0;
		this.attackOtherFactions = false;
		this.defendFaction = false;
		this.disablePitch = false;
		this.npc = npc;
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
		Line line = this.getLines(type).lines.get(slot);
		if (line == null) {
			return null;
		}
		return line.getText();
	}

	@Override
	public int getLineCount(int type) {
		return this.getLines(type).lines.size();
	}

	private Lines getLines(int type) {
		if (type == 0) {
			return this.interactLines;
		}
		if (type == 1) {
			return this.attackLines;
		}
		if (type == 2) {
			return this.worldLines;
		}
		if (type == 3) {
			return this.killedLines;
		}
		if (type == 4) {
			return this.killLines;
		}
		if (type == 5) {
			return this.npcInteractLines;
		}
		return null;
	}

	public Line getNPCInteractLine() {
		return this.npcInteractLines.getLine(!this.orderedLines);
	}

	@Override
	public String getSound(int type) {
		String sound = null;
		if (type == 0) {
			sound = this.idleSound;
		} else if (type == 1) {
			sound = this.angrySound;
		} else if (type == 2) {
			sound = this.hurtSound;
		} else if (type == 3) {
			sound = this.deathSound;
		} else if (type == 4) {
			sound = this.stepSound;
		}
		if (sound != null && sound.isEmpty()) {
			return null;
		}
		return sound;
	}

	public Line getWorldLine() {
		return this.worldLines.getLine(!this.orderedLines);
	}

	public boolean hasWorldLines() {
		return !this.worldLines.isEmpty();
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
		this.interactLines.readNBT(compound.getCompoundTag("NpcInteractLines"));
		this.worldLines.readNBT(compound.getCompoundTag("NpcLines"));
		this.attackLines.readNBT(compound.getCompoundTag("NpcAttackLines"));
		this.killedLines.readNBT(compound.getCompoundTag("NpcKilledLines"));
		this.killLines.readNBT(compound.getCompoundTag("NpcKillLines"));
		this.npcInteractLines.readNBT(compound.getCompoundTag("NpcInteractNPCLines"));
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
		this.setRole(compound.getInteger("Role"));
		this.setJob(compound.getInteger("NpcJob"));
		this.factions.readFromNBT(compound.getCompoundTag("FactionPoints"));
		this.scenes.readFromNBT(compound.getCompoundTag("NpcScenes"));
		// New
		if (this.role == 1 && compound.hasKey("MarketID", 3)) {
			((RoleTrader) this.npc.roleInterface).marcet = compound.getInteger("MarketID");
		}
		if (compound.hasKey("NPCDialogOptions", 11)) {
			this.npc.dialogs = compound.getIntArray("NPCDialogOptions"); // new
		} else if (compound.hasKey("NPCDialogOptions", 9)) {
			// Old
			this.npc.dialogs = new int[compound.getTagList("NPCDialogOptions", 10).tagCount()];
			for (int i = 0; i < compound.getTagList("NPCDialogOptions", 10).tagCount(); ++i) {
				NBTTagCompound nbttagcompound = compound.getTagList("NPCDialogOptions", 10).getCompoundTagAt(i);
				DialogOption option = new DialogOption();
				option.readNBT(nbttagcompound.getCompoundTag("NPCDialog"));
				this.npc.dialogs[i] = option.dialogId;
			}
		}
	}

	public void setJob(int i) {
		if (this.npc.jobInterface != null && !this.npc.world.isRemote) {
			this.npc.jobInterface.reset();
		}
		this.job = i % 12;
		switch (this.job) {
		case 1: {
			if (!(this.npc.jobInterface instanceof JobBard)) {
				this.npc.jobInterface = new JobBard(this.npc);
			}
			break;
		}
		case 2: {
			if (!(this.npc.jobInterface instanceof JobHealer)) {
				this.npc.jobInterface = new JobHealer(this.npc);
			}
			break;
		}
		case 3: {
			if (!(this.npc.jobInterface instanceof JobGuard)) {
				this.npc.jobInterface = new JobGuard(this.npc);
			}
			break;
		}
		case 4: {
			if (!(this.npc.jobInterface instanceof JobItemGiver)) {
				this.npc.jobInterface = new JobItemGiver(this.npc);
			}
			break;
		}
		case 5: {
			if (!(this.npc.jobInterface instanceof JobFollower)) {
				this.npc.jobInterface = new JobFollower(this.npc);
			}
			break;
		}
		case 6: {
			if (!(this.npc.jobInterface instanceof JobSpawner)) {
				this.npc.jobInterface = new JobSpawner(this.npc);
			}
			break;
		}
		case 7: {
			if (!(this.npc.jobInterface instanceof JobConversation)) {
				this.npc.jobInterface = new JobConversation(this.npc);
			}
			break;
		}
		case 8: {
			if (!(this.npc.jobInterface instanceof JobChunkLoader)) {
				this.npc.jobInterface = new JobChunkLoader(this.npc);
			}
			break;
		}
		case 9: {
			if (!(this.npc.jobInterface instanceof JobPuppet)) {
				this.npc.jobInterface = new JobPuppet(this.npc);
			}
			break;
		}
		case 10: {
			if (!(this.npc.jobInterface instanceof JobBuilder)) {
				this.npc.jobInterface = new JobBuilder(this.npc);
			}
			break;
		}
		case 11: {
			if (!(this.npc.jobInterface instanceof JobFarmer)) {
				this.npc.jobInterface = new JobFarmer(this.npc);
			}
			break;
		}
		default: {
			this.npc.jobInterface = null;
		}
		}
	}

	@Override
	public void setLine(int type, int slot, String text, String sound) {
		slot = ValueUtil.correctInt(slot, 0, 7);
		Lines lines = this.getLines(type);
		if (text == null || text.isEmpty()) {
			lines.lines.remove(slot);
		} else {
			Line line = lines.lines.get(slot);
			if (line == null) {
				lines.lines.put(slot, line = new Line());
			}
			line.setText(text);
			line.setSound(sound);
		}
	}

	public void setRole(int i) {
		if (8 <= i) {
			i -= 2;
		}
		this.role = i % 8;
		switch (this.role) {
		case 1: {
			if (!(this.npc.roleInterface instanceof RoleTrader)) {
				this.npc.roleInterface = new RoleTrader(this.npc);
			}
			break;
		}
		case 2: {
			if (!(this.npc.roleInterface instanceof RoleFollower)) {
				this.npc.roleInterface = new RoleFollower(this.npc);
			}
			break;
		}
		case 3: {
			if (!(this.npc.roleInterface instanceof RoleBank)) {
				this.npc.roleInterface = new RoleBank(this.npc);
			}
			break;
		}
		case 4: {
			if (!(this.npc.roleInterface instanceof RoleTransporter)) {
				this.npc.roleInterface = new RoleTransporter(this.npc);
			}
			break;
		}
		case 5: {
			if (!(this.npc.roleInterface instanceof RolePostman)) {
				this.npc.roleInterface = new RolePostman(this.npc);
			}
			break;
		}
		case 6: {
			if (!(this.npc.roleInterface instanceof RoleCompanion)) {
				this.npc.roleInterface = new RoleCompanion(this.npc);
			}
			break;
		}
		case 7: {
			if (!(this.npc.roleInterface instanceof RoleDialog)) {
				this.npc.roleInterface = new RoleDialog(this.npc);
			}
			break;
		}
		default: {
			this.npc.roleInterface = null;
		}
		}
	}

	@Override
	public void setSound(int type, String sound) {
		if (sound == null) {
			sound = "";
		}
		if (type == 0) {
			this.idleSound = sound;
		} else if (type == 1) {
			this.angrySound = sound;
		} else if (type == 2) {
			this.hurtSound = sound;
		} else if (type == 3) {
			this.deathSound = sound;
		} else if (type == 4) {
			this.stepSound = sound;
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("NpcLines", this.worldLines.writeToNBT());
		compound.setTag("NpcKilledLines", this.killedLines.writeToNBT());
		compound.setTag("NpcInteractLines", this.interactLines.writeToNBT());
		compound.setTag("NpcAttackLines", this.attackLines.writeToNBT());
		compound.setTag("NpcKillLines", this.killLines.writeToNBT());
		compound.setTag("NpcInteractNPCLines", this.npcInteractLines.writeToNBT());
		compound.setBoolean("OrderedLines", this.orderedLines);
		compound.setString("NpcIdleSound", this.idleSound);
		compound.setString("NpcAngrySound", this.angrySound);
		compound.setString("NpcHurtSound", this.hurtSound);
		compound.setString("NpcDeathSound", this.deathSound);
		compound.setString("NpcStepSound", this.stepSound);
		compound.setInteger("FactionID", this.npc.getFaction().id);
		compound.setBoolean("AttackOtherFactions", this.attackOtherFactions);
		compound.setBoolean("DefendFaction", this.defendFaction);
		compound.setBoolean("DisablePitch", this.disablePitch);
		compound.setInteger("Role", this.role);
		compound.setInteger("NpcJob", this.job);
		compound.setTag("FactionPoints", this.factions.writeToNBT(new NBTTagCompound()));
		compound.setIntArray("NPCDialogOptions", this.npc.dialogs);
		compound.setTag("NpcScenes", this.scenes.writeToNBT(new NBTTagCompound()));
		// New
		if (this.role == 1) {
			compound.setInteger("MarketID", ((RoleTrader) this.npc.roleInterface).marcet);
		}
		return compound;
	}
}
