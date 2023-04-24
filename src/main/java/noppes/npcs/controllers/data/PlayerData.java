package noppes.npcs.controllers.data;

import java.io.File;
import java.io.FileInputStream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.NBTJsonUtil;

public class PlayerData
implements ICapabilityProvider {
	
	@CapabilityInject(PlayerData.class)
	public static Capability<PlayerData> PLAYERDATA_CAPABILITY = null;
	
	private static ResourceLocation key = new ResourceLocation(CustomNpcs.MODID, "playerdata");
	private EntityNPCInterface activeCompanion;
	public PlayerBankData bankData;
	public NBTTagCompound cloned;
	public int companionID;
	public PlayerDialogData dialogData;
	public int dialogId;
	public EntityNPCInterface editingNpc;
	public PlayerFactionData factionData;
	// New
	public PlayerGameData game;
	public PlayerItemGiverData itemgiverData;
	public PlayerMailData mailData;
	public PlayerOverlayHUD hud;
	public EntityPlayer player;
	public int playerLevel;
	public String playername;
	public ItemStack prevHeldItem;
	public PlayerQuestData questData;
	public PlayerScriptData scriptData;
	public NBTTagCompound scriptStoreddata;

	public DataTimers timers;
	public PlayerTransportData transportData;
	public boolean updateClient; // send to -> ServerTickHandler.onPlayerTick()
	public String uuid;

	public PlayerData() {
		this.dialogData = new PlayerDialogData();
		this.bankData = new PlayerBankData();
		this.questData = new PlayerQuestData();
		this.transportData = new PlayerTransportData();
		this.factionData = new PlayerFactionData();
		this.itemgiverData = new PlayerItemGiverData();
		this.mailData = new PlayerMailData();
		this.hud = new PlayerOverlayHUD();
		this.timers = new DataTimers(this);
		this.scriptStoreddata = new NBTTagCompound();
		this.playername = "";
		this.uuid = "";
		this.activeCompanion = null;
		this.companionID = 0;
		this.playerLevel = 0;
		this.updateClient = false;
		this.dialogId = -1;
		this.prevHeldItem = ItemStack.EMPTY;
		// New
		this.game = new PlayerGameData();
	}

	public static PlayerData get(EntityPlayer player) {
		if (player==null || player.world.isRemote) {
			return CustomNpcs.proxy.getPlayerData(player);
		}
		PlayerData data = (PlayerData) player.getCapability(PlayerData.PLAYERDATA_CAPABILITY, null);
		if (data.player == null) {
			data.player = player;
			data.playerLevel = player.experienceLevel;
			data.scriptData = new PlayerScriptData(player);
			NBTTagCompound compound = loadPlayerData(player.getPersistentID().toString());
			if (compound.getKeySet().size() == 0) {
				compound = loadPlayerDataOld(player.getName());
			}
			data.setNBT(compound);
		}
		return data;
	}

	public static NBTTagCompound loadPlayerData(String player) {
		File saveDir = CustomNpcs.getWorldSaveDirectory("playerdata");
		String filename = player;
		if (filename.isEmpty()) {
			filename = "noplayername";
		}
		filename += ".json";
		File file = null;
		try {
			file = new File(saveDir, filename);
			if (file.exists()) {
				return NBTJsonUtil.LoadFile(file);
			}
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
		}
		return new NBTTagCompound();
	}

	public static NBTTagCompound loadPlayerDataOld(String player) {
		File saveDir = CustomNpcs.getWorldSaveDirectory("playerdata");
		String filename = player;
		if (filename.isEmpty()) {
			filename = "noplayername";
		}
		filename += ".dat";
		try {
			File file = new File(saveDir, filename);
			if (file.exists()) {
				NBTTagCompound comp = CompressedStreamTools.readCompressed(new FileInputStream(file));
				file.delete();
				file = new File(saveDir, filename + "_old");
				if (file.exists()) {
					file.delete();
				}
				return comp;
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
		try {
			File file = new File(saveDir, filename + "_old");
			if (file.exists()) {
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
		return new NBTTagCompound();
	}

	public static void register(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(PlayerData.key, (ICapabilityProvider) new PlayerData());
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (this.hasCapability(capability, facing)) {
			return (T) this;
		}
		return null;
	}

	public NBTTagCompound getNBT() {
		if (this.player != null) {
			this.playername = this.player.getName();
			this.uuid = this.player.getPersistentID().toString();
		}
		NBTTagCompound compound = new NBTTagCompound();
		this.dialogData.saveNBTData(compound);
		this.bankData.saveNBTData(compound);
		this.questData.saveNBTData(compound);
		this.transportData.saveNBTData(compound);
		this.factionData.saveNBTData(compound);
		this.itemgiverData.saveNBTData(compound);
		this.mailData.saveNBTData(compound);
		this.hud.saveNBTData(compound);
		this.game.saveNBTData(compound); // New
		this.timers.writeToNBT(compound);
		compound.setString("PlayerName", this.playername);
		compound.setString("UUID", this.uuid);
		compound.setInteger("PlayerCompanionId", this.companionID);
		compound.setTag("ScriptStoreddata", this.scriptStoreddata);
		if (this.hasCompanion()) {
			NBTTagCompound nbt = new NBTTagCompound();
			if (this.activeCompanion.writeToNBTAtomically(nbt)) {
				compound.setTag("PlayerCompanion", nbt);
			}
		}
		return compound;
	}

	public NBTTagCompound getSyncNBT() { // Only Display Datas
		NBTTagCompound compound = new NBTTagCompound();
		this.dialogData.saveNBTData(compound);
		this.questData.saveNBTData(compound);
		this.factionData.saveNBTData(compound);
		return compound;
	}

	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == PlayerData.PLAYERDATA_CAPABILITY;
	}

	public boolean hasCompanion() {
		return this.activeCompanion != null && !this.activeCompanion.isDead;
	}

	public synchronized void save(boolean update) {
		String filename = this.uuid + ".json";
		CustomNPCsScheduler.runTack(() -> {
			try {
				File saveDir = CustomNpcs.getWorldSaveDirectory("playerdata");
				File file = new File(saveDir, filename + "_new");
				File file1 = new File(saveDir, filename);
				NBTTagCompound nbt = this.getNBT();
				NBTJsonUtil.SaveFile(file, nbt);
				if (file1.exists()) {
					file1.delete();
				}
				file.renameTo(file1);
			} catch (Exception e) {
				LogWriter.error("Error save PlayerData to file", e);
			}
		});
		if (update) {
			this.updateClient = true;
		}
	}

	public void setCompanion(EntityNPCInterface npc) {
		if (npc != null && npc.advanced.role != 6) {
			return;
		}
		++this.companionID;
		if ((this.activeCompanion = npc) != null) {
			((RoleCompanion) npc.roleInterface).companionID = this.companionID;
		}
		this.save(false);
	}

	public void setNBT(NBTTagCompound data) {
		this.dialogData.loadNBTData(data);
		this.bankData.loadNBTData(data);
		this.questData.loadNBTData(data);
		this.transportData.loadNBTData(data);
		this.factionData.loadNBTData(data);
		this.itemgiverData.loadNBTData(data);
		this.mailData.loadNBTData(data);
		this.hud.loadNBTData(data);
		this.timers.readFromNBT(data);
		this.game.readFromNBT(data); // New
		if (this.player != null) {
			this.playername = this.player.getName();
			this.uuid = this.player.getPersistentID().toString();
		} else {
			this.playername = data.getString("PlayerName");
			this.uuid = data.getString("UUID");
		}
		this.companionID = data.getInteger("PlayerCompanionId");
		if (data.hasKey("PlayerCompanion") && !this.hasCompanion()) {
			EntityCustomNpc npc = new EntityCustomNpc(this.player.world);
			npc.readEntityFromNBT(data.getCompoundTag("PlayerCompanion"));
			npc.setPosition(this.player.posX, this.player.posY, this.player.posZ);
			if (npc.advanced.role == 6) {
				this.setCompanion(npc);
				((RoleCompanion) npc.roleInterface).setSitting(false);
				this.player.world.spawnEntity(npc);
			}
		}
		this.scriptStoreddata = data.getCompoundTag("ScriptStoreddata");
	}

	public void updateCompanion(World world) {
		if (!this.hasCompanion() || world == this.activeCompanion.world) {
			return;
		}
		RoleCompanion role = (RoleCompanion) this.activeCompanion.roleInterface;
		role.owner = this.player;
		if (!role.isFollowing()) {
			return;
		}
		NBTTagCompound nbt = new NBTTagCompound();
		this.activeCompanion.writeToNBTAtomically(nbt);
		this.activeCompanion.isDead = true;
		EntityCustomNpc npc = new EntityCustomNpc(world);
		npc.readEntityFromNBT(nbt);
		npc.setPosition(this.player.posX, this.player.posY, this.player.posZ);
		this.setCompanion(npc);
		((RoleCompanion) npc.roleInterface).setSitting(false);
		world.spawnEntity(npc);
	}

}
