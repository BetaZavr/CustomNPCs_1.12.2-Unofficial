package noppes.npcs.controllers.data;

import java.io.File;
import java.nio.file.Files;

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
import noppes.npcs.api.handler.ICustomPlayerData;
import noppes.npcs.api.handler.capability.IPlayerDataHandler;
import noppes.npcs.api.mixin.entity.IEntityMixin;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class PlayerData implements IPlayerDataHandler, ICapabilityProvider, ICustomPlayerData {

	@CapabilityInject(IPlayerDataHandler.class)
	public static Capability<IPlayerDataHandler> PLAYERDATA_CAPABILITY = null;

	protected static final ResourceLocation key = new ResourceLocation(CustomNpcs.MODID, "playerdata");

    public static PlayerData get(EntityPlayer player) {
		if (player == null || player.world == null || player.world.isRemote) { return CustomNpcs.proxy.getPlayerData(player); }
		PlayerData data = (PlayerData) player.getCapability(PlayerData.PLAYERDATA_CAPABILITY, null);
		if (data != null && data.player == null) {
			data.player = player;
			data.playerLevel = player.experienceLevel;
			data.animation = new DataAnimation(player);
			data.scriptData = new PlayerScriptData(player);
			NBTTagCompound compound = PlayerData.loadPlayerData(player.getPersistentID().toString(), player.getName());
			if (compound.getKeySet().isEmpty()) { compound = loadPlayerDataOld(player.getPersistentID().toString(), player.getName()); }
			data.setNBT(compound);
		}
		return data;
	}

	@SuppressWarnings("all")
	public static NBTTagCompound loadPlayerData(String uuid, String name) {
		File dir = CustomNpcs.getWorldSaveDirectory("playerdata");
		File saveDir = new File(dir, uuid);
		File file = new File(saveDir, name + ".json");
		if (!saveDir.exists()) {
			saveDir.mkdirs();
			File oldVersionFile = new File(dir, uuid + ".json");
			try {
				if (oldVersionFile.exists()) {
					NBTTagCompound nbt = NBTJsonUtil.LoadFile(oldVersionFile);
					oldVersionFile.delete();
					if (!file.exists()) {
						try {
							file.createNewFile();
							Util.instance.saveFile(file, nbt);
						}
						catch (Exception e) { LogWriter.error("Error create player data: " + file.getAbsolutePath(), e); }
					}
					return nbt;
				}
			}
			catch (Exception e) { LogWriter.error("Error old loading: " + oldVersionFile.getAbsolutePath(), e); }
		}
		if (!file.exists()) {
			try { file.createNewFile(); }
			catch (Exception e) { LogWriter.error("Error create player data: " + file.getAbsolutePath(), e); }
			return new NBTTagCompound();
		}
		try {
			if (file.exists()) { return NBTJsonUtil.LoadFile(file); }
		}
		catch (Exception e) { LogWriter.error("Error loading: " + file.getAbsolutePath(), e); }
		return new NBTTagCompound();
	}

	@SuppressWarnings("all")
	public static NBTTagCompound loadPlayerDataOld(String uuid, String name) {
		File saveDir = CustomNpcs.getWorldSaveDirectory("playerdata/" + uuid);
		if (name.isEmpty()) { name = "noplayername"; }
		name += ".dat";
		try {
			File file = new File(saveDir, name);
			if (file.exists()) {
				NBTTagCompound comp = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
				file.delete();
				file = new File(saveDir, name + "_old");
				if (file.exists()) {
					file.delete();
				}
				return comp;
			}
		}
		catch (Exception e) { LogWriter.except(e); }
		try {
			File file = new File(saveDir, name + "_old");
			if (file.exists()) { return CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath())); }
		}
		catch (Exception e) { LogWriter.except(e); }
		return new NBTTagCompound();
	}
	public static void register(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) { event.addCapability(PlayerData.key, new PlayerData()); }
	}

	protected EntityNPCInterface activeCompanion = null;
	public PlayerBankData bankData = new PlayerBankData();
	public NBTTagCompound cloned;
	public int companionID = 0;
	public int dialogId = -1;
	public PlayerDialogData dialogData = new PlayerDialogData();
	public EntityNPCInterface editingNpc;
	public PlayerFactionData factionData = new PlayerFactionData();
	public PlayerGameData game = new PlayerGameData();
	public PlayerItemGiverData itemgiverData = new PlayerItemGiverData();
	public PlayerMailData mailData = new PlayerMailData();
	public DataAnimation animation;
	public PlayerOverlayHUD hud = new PlayerOverlayHUD();
	public PlayerQuestData questData = new PlayerQuestData();
	public PlayerScriptData scriptData;
	public PlayerMiniMapData minimap = new PlayerMiniMapData();
	public PlayerTransportData transportData = new PlayerTransportData();

	public EntityPlayer player;
	public int playerLevel = 0;
	public ItemStack prevHeldItem = ItemStack.EMPTY;

	public DataTimers timers = new DataTimers(this);

	public boolean updateClient = false; // send to -> ServerTickHandler.onPlayerTick() 112

	public String uuid = "";
	public String playername = "";

	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (hasCapability(capability, facing)) { return (T) this; }
		return null;
	}

	@Override
	public NBTTagCompound getNBT() {
		CustomNpcs.debugData.start(this);
		if (player != null) {
			playername = player.getName();
			uuid = player.getPersistentID().toString();
		}
		NBTTagCompound compound = new NBTTagCompound();
		dialogData.saveNBTData(compound);
		questData.saveNBTData(compound);
		transportData.saveNBTData(compound);
		factionData.saveNBTData(compound);
		itemgiverData.saveNBTData(compound);
		mailData.saveNBTData(compound);
		timers.writeToNBT(compound);
		hud.saveNBTData(compound);
		game.saveNBTData(compound);
		minimap.saveNBTData(compound);
		if (animation != null) { animation.save(compound); }
		compound.setInteger("PlayerCompanionId", companionID);
		if (playername != null && !playername.isEmpty()) { compound.setString("PlayerName", playername); }
		if (uuid != null && !uuid.isEmpty()) { compound.setString("UUID", uuid); }
		if (player != null) { compound.setTag("ScriptStoreddata", ((IEntityMixin) player).npcs$getStoredData().getNbt().getMCNBT()); }
		if (hasCompanion()) {
			NBTTagCompound nbt = new NBTTagCompound();
			if (activeCompanion.writeToNBTAtomically(nbt)) { compound.setTag("PlayerCompanion", nbt); }
		}
		CustomNpcs.debugData.end(this);
		return compound;
	}

	public NBTTagCompound getSyncNBT() { // Only Display Datas
		NBTTagCompound compound = new NBTTagCompound();
		dialogData.saveNBTData(compound);
		questData.saveNBTData(compound);
		factionData.saveNBTData(compound);
		return compound;
	}

	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) { return capability == PlayerData.PLAYERDATA_CAPABILITY; }

	public boolean hasCompanion() { return activeCompanion != null && !activeCompanion.isDead; }

	@SuppressWarnings("all")
	public synchronized void save(boolean update) {
		CustomNPCsScheduler.runTack(() -> {
			try {
				if (uuid.isEmpty()) { uuid = "noplayeruuid"; }
				if (playername.isEmpty()) { playername = "noplayername"; }
				File saveDir = CustomNpcs.getWorldSaveDirectory("playerdata/" + uuid);
				String filename = playername + ".json";
				File file = new File(saveDir, filename + "_new");
				File file1 = new File(saveDir, filename);
				NBTTagCompound nbt = getNBT();
				Util.instance.saveFile(file, nbt);
				if (file1.exists()) { file1.delete(); }
				file.renameTo(file1);
			}
			catch (Exception e) { LogWriter.error("Error save PlayerData to file", e); }
			if (update) { updateClient = true; }
		});
	}

	public void setCompanion(EntityNPCInterface npc) {
		if (npc == null || !(npc.advanced.roleInterface instanceof RoleCompanion)) { return; }
		++companionID;
		activeCompanion = npc;
        ((RoleCompanion) npc.advanced.roleInterface).companionID = companionID;
        save(false);
	}

	@Override
	public void setNBT(NBTTagCompound data) {
		if (player != null) {
			playername = player.getName();
			uuid = player.getPersistentID().toString();
		} else {
			playername = data.getString("PlayerName");
			uuid = data.getString("UUID");
		}
		dialogData.loadNBTData(data);
		bankData.loadNBTData(data, uuid);
		questData.loadNBTData(data);
		transportData.loadNBTData(data);
		factionData.loadNBTData(data);
		itemgiverData.loadNBTData(data);
		mailData.loadNBTData(data);
		hud.loadNBTData(data);
		timers.readFromNBT(data);
		game.readFromNBT(data);
		companionID = data.getInteger("PlayerCompanionId");
		if (data.hasKey("PlayerCompanion") && !hasCompanion()) {
			EntityCustomNpc npc = new EntityCustomNpc(player.world);
			npc.readEntityFromNBT(data.getCompoundTag("PlayerCompanion"));
			npc.setPosition(player.posX, player.posY, player.posZ);
			if (npc.advanced.roleInterface instanceof RoleCompanion) {
				setCompanion(npc);
				((RoleCompanion) npc.advanced.roleInterface).setSitting(false);
				player.world.spawnEntity(npc);
			}
		}
		if (player != null) { ((IEntityMixin) player).npcs$getStoredData().setNbt(data.getCompoundTag("ScriptStoreddata")); }
	}

	public void updateCompanion(World world) {
		if (!hasCompanion() || world == activeCompanion.world) { return; }
		RoleCompanion role = (RoleCompanion) activeCompanion.advanced.roleInterface;
		role.owner = player;
		if (!role.isFollowing()) { return; }
		NBTTagCompound nbt = new NBTTagCompound();
		activeCompanion.writeToNBTAtomically(nbt);
		activeCompanion.isDead = true;
		EntityCustomNpc npc = new EntityCustomNpc(world);
		npc.readEntityFromNBT(nbt);
		npc.setPosition(player.posX, player.posY, player.posZ);
		setCompanion(npc);
		((RoleCompanion) npc.advanced.roleInterface).setSitting(false);
		world.spawnEntity(npc);
	}

	public EntityPlayer getPlayer() { return player; }

	public void setPlayer(EntityPlayer playerIn) {
		player = playerIn;
		if (player != null) {
			NBTTagCompound compound = new NBTTagCompound();
			if (animation != null) { animation.save(compound); }
			animation = new DataAnimation(player);
			if (!compound.hasNoTags()) { animation.load(compound); }
		}
	}

}
