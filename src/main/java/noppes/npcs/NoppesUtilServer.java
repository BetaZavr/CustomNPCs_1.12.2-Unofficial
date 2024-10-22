package noppes.npcs;

import java.io.File;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerDialogData;
import noppes.npcs.controllers.data.PlayerFactionData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.PlayerTransportData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.Util;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class NoppesUtilServer {

	private static final HashMap<UUID, Quest> editingQuests = new HashMap<>();
	private static final HashMap<UUID, Quest> editingQuestsClient = new HashMap<>();

	public static void createMobSpawner(BlockPos pos, NBTTagCompound comp, EntityPlayer player) {
		ServerCloneController.Instance.cleanTags(comp);
		if (comp.getString("id").equalsIgnoreCase("entityhorse")) {
			player.sendMessage(new TextComponentTranslation("Currently you cant create horse spawner, its a minecraft bug"));
			return;
		}
		player.world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState());
		TileEntityMobSpawner tile = (TileEntityMobSpawner) player.world.getTileEntity(pos);
        assert tile != null;
        MobSpawnerBaseLogic logic = tile.getSpawnerBaseLogic();
		if (!comp.hasKey("id", 8)) {
			comp.setString("id", "Pig");
		}
		comp.setIntArray("StartPosNew", new int[] { pos.getX(), pos.getY(), pos.getZ() });
		logic.setNextSpawnData(new WeightedSpawnerEntity(1, comp));
	}

	public static void deleteEntity(EntityLivingBase entity) {
		Server.sendAssociatedData(entity, EnumPacketClient.DELETE_ENTITY, entity.getEntityId());
	}

	public static BlockPos GetClosePos(BlockPos origin, World world) {
		for (int x = -1; x < 2; ++x) {
			for (int z = -1; z < 2; ++z) {
				for (int y = 2; y >= -2; --y) {
					BlockPos pos = origin.add(x, y, z);
					if (world.isSideSolid(pos, EnumFacing.UP) && world.isAirBlock(pos.up())
							&& world.isAirBlock(pos.up(2))) {
						return pos.up();
					}
				}
			}
		}
		return world.getTopSolidOrLiquidBlock(origin);
	}

	public static Entity GetDamageSource(DamageSource damagesource) {
		Entity entity = damagesource.getTrueSource();
		if (entity == null) {
			entity = damagesource.getImmediateSource();
		}
		if (entity instanceof EntityArrow && ((EntityArrow) entity).shootingEntity instanceof EntityLivingBase) {
			entity = ((EntityArrow) entity).shootingEntity;
		} else if (entity instanceof EntityThrowable) {
			entity = ((EntityThrowable) entity).getThrower();
		}
		return entity;
	}

	public static EntityNPCInterface getEditingNpc(EntityPlayer player) {
		PlayerData data = PlayerData.get(player);
		return data.editingNpc;
	}

	public static Quest getEditingQuest(EntityPlayer player) {
		if (player.world.isRemote) {
			return NoppesUtilServer.editingQuestsClient.get(player.getUniqueID());
		}
		return NoppesUtilServer.editingQuests.get(player.getUniqueID());
	}

	public static EntityPlayer getPlayer(MinecraftServer minecraftserver, UUID id) {
		List<EntityPlayerMP> list = minecraftserver.getPlayerList().getPlayers();
		for (EntityPlayer player : list) {
			if (id.equals(player.getUniqueID())) {
				return player;
			}
		}
		return null;
	}

	private static Map<String, Integer> getScrollData(EntityPlayer player, EnumGuiType gui, EntityNPCInterface npc) {
		Map<String, Integer> map = Maps.newHashMap();
		if (gui == EnumGuiType.PlayerTransporter) {
			RoleTransporter role = (RoleTransporter) npc.advanced.roleInterface;
			TransportLocation location = role.getLocation();
			String name = role.getLocation().name;
			for (TransportLocation loc : location.category.getDefaultLocations()) {
				if (!map.containsKey(loc.name)) {
					map.put(loc.name, loc.id);
				}
			}
			PlayerTransportData playerdata = PlayerData.get(player).transportData;
			for (int i : playerdata.transports) {
				TransportLocation loc = TransportController.getInstance().getTransport(i);
				if (loc != null && location.category.locations.containsKey(loc.id) && !map.containsKey(loc.name)) {
					map.put(loc.name, loc.id);
				}
			}
			map.remove(name);
		}
		return map;
	}

	public static void GivePlayerItem(Entity entity, EntityPlayer player, ItemStack item) {
		if (entity.world.isRemote || item == null || item.isEmpty()) {
			return;
		}
		item = item.copy();
		float f = 0.7f;
		double d = entity.world.rand.nextFloat() * f + (1.0f - f);
		double d2 = entity.world.rand.nextFloat() * f + (1.0f - f);
		double d3 = entity.world.rand.nextFloat() * f + (1.0f - f);
		EntityItem entityitem = new EntityItem(entity.world, entity.posX + d, entity.posY + d2, entity.posZ + d3, item);
		entityitem.setPickupDelay(2);
		entity.world.spawnEntity(entityitem);
		int i = item.getCount();
		if (player.inventory.addItemStackToInventory(item)) {
			entity.world.playSound(null, player.posX, player.posY, player.posZ,
					SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f,
					((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
			player.onItemPickup(entityitem, i);
			PlayerQuestData playerdata = PlayerData.get(player).questData;
			for (QuestData data : playerdata.activeQuests.values()) { // Changed
				for (IQuestObjective obj : data.quest
						.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
					if (obj.getType() != EnumQuestTask.ITEM.ordinal()) {
						continue;
					}
					playerdata.checkQuestCompletion(player, data);
				}
			}
			if (item.getCount() <= 0) {
				entityitem.setDead();
			}
		}
	}

	public static boolean IsItemStackNull(ItemStack is) {
		return is == null || is.isEmpty();
	}

	public static boolean isOp(EntityPlayer player) {
		return Objects.requireNonNull(player.getServer()).getPlayerList().canSendCommands(player.getGameProfile());
	}

	public static void moveNpcDialogs(EntityPlayer player, int slot, boolean isUp) {
		EntityNPCInterface npc = getEditingNpc(player);
		if (npc == null) {
			return;
		}
		if ((isUp && slot <= 0) || (!isUp && slot >= (npc.dialogs.length - 1))) {
			return;
		}
		int[] newIDs = new int[npc.dialogs.length];
		for (int s = 0; s < npc.dialogs.length; s++) {
			if ((s + (isUp ? 1 : -1)) == slot) {
				newIDs[s] = npc.dialogs[s + (isUp ? 1 : -1)];
			} else if (s == slot) {
				newIDs[s] = npc.dialogs[s + (isUp ? -1 : 1)];
			} else {
				newIDs[s] = npc.dialogs[s];
			}
			Dialog d = (Dialog) DialogController.instance.get(newIDs[s]);
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("Id", newIDs[s]);
			compound.setInteger("Slot", s);
			compound.setString("Category", d != null ? d.category.title : "");
			compound.setString("Title", d != null ? d.title : "null");
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.GUI_DATA, compound);
		}
		npc.dialogs = newIDs;
	}

	public static void moveNpcSpawn(EntityPlayerMP player, int slot, boolean isUp, boolean isDead) {
		EntityNPCInterface npc = getEditingNpc(player);
		if (npc == null || !(npc.advanced.jobInterface instanceof JobSpawner)) {
			return;
		}
		JobSpawner job = (JobSpawner) npc.advanced.jobInterface;
		if ((isUp && slot <= 0) || (!isUp && slot >= (job.size(isDead) - 1))) {
			return;
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("JobData", true);
		npc.advanced.jobInterface.writeToNBT(compound);
		job.cleanCompound(compound);
		Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
	}

	public static void NotifyOPs(String message, Object... obs) {
		TextComponentTranslation chat_component_translation = new TextComponentTranslation(message, obs);
		chat_component_translation.getStyle().setColor(TextFormatting.GRAY);
		chat_component_translation.getStyle().setItalic(Boolean.TRUE);
		for (EntityPlayer entityplayer : CustomNpcs.Server.getPlayerList().getPlayers()) {
			if (entityplayer.sendCommandFeedback() && isOp(entityplayer)) {
				entityplayer.sendMessage(chat_component_translation);
			}
		}
		if (CustomNpcs.Server.worlds[0].getGameRules().getBoolean("logAdminCommands")) {
			LogWriter.info(chat_component_translation.getUnformattedText());
		}
	}

	public static void openDialog(EntityPlayer player, EntityNPCInterface npc, Dialog dia) {
		if (dia == null) {
			return;
		}
		Dialog dialog = dia.copy(player);
		PlayerData playerdata = PlayerData.get(player);
		if (EventHooks.onNPCDialog(npc, player, dialog)) {
			playerdata.dialogId = -1;
			return;
		}
		playerdata.dialogId = dialog.id;
		if (npc instanceof EntityDialogNpc || dia.id < 0) {
			dialog.hideNPC = true;
			Server.sendDataDelayed((EntityPlayerMP) player, EnumPacketClient.DIALOG_DUMMY, 100, npc.getName(),
					dialog.writeToNBT(new NBTTagCompound()));
		} else {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.DIALOG, npc.getEntityId(), dialog.id);
		}
		dia.factionOptions.addPoints(player);
		if (dialog.hasQuest()) {
			PlayerQuestController.addActiveQuest(dialog.getQuest(), player, false);
		}
		if (!dialog.command.isEmpty()) {
			runCommand(npc, npc.getName(), dialog.command, player);
		}
		if (dialog.mail.isValid()) {
			PlayerDataController.instance.addPlayerMessage(player.getServer(), player.getName(), dialog.mail);
		}
		PlayerDialogData data = playerdata.dialogData;
		if (!data.dialogsRead.contains(dialog.id) && dialog.id >= 0) {
			data.dialogsRead.add(dialog.id);
			playerdata.updateClient = true;
		}
		setEditingNpc(player, npc);
		for (QuestData qdata : playerdata.questData.activeQuests.values()) { // Changed
			for (IQuestObjective obj : qdata.quest.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
				if (obj.getType() != EnumQuestTask.DIALOG.ordinal()) {
					continue;
				}
				playerdata.questData.checkQuestCompletion(player, qdata);
			}
		}
	}

	public static void playSound(EntityLivingBase entity, SoundEvent sound, float volume, float pitch) {
		entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, sound, SoundCategory.NEUTRAL, volume, pitch);
	}

	public static void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory cat, float volume,
			float pitch) {
		world.playSound(null, pos, sound, cat, volume, pitch);
	}

	public static void removePlayerData(int id, ByteBuf buffer, EntityPlayerMP player) {
		if (EnumPlayerData.values().length <= id) {
			return;
		}
		String name = Server.readString(buffer);
		if (name == null || name.isEmpty()) {
			return;
		}
		EnumPlayerData type = EnumPlayerData.values()[id];
		EntityPlayerMP pl = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayerByUsername(name);
		PlayerData playerdata;
		if (pl == null) {
			playerdata = PlayerDataController.instance.getDataFromUsername(player.getServer(), name);
		} else {
			playerdata = PlayerData.get(pl);
		}
		if (type == EnumPlayerData.Players) { // Wipe
			File playerDir = new File(CustomNpcs.getWorldSaveDirectory("playerdata"), playerdata.uuid);
			if (playerDir.exists()) {
				Util.instance.removeFile(playerDir);
			}
			if (pl != null) {
				playerdata.setNBT(new NBTTagCompound());
				sendPlayerData(type, player, name);
				playerdata.save(true);
				return;
			}
		}
		if (pl != null) {
			SyncController.syncPlayer(pl);
		}
		sendPlayerData(type, player, name);
	}

	public static String runCommand(ICommandSender sender, String name, String command, EntityPlayer player) {
		return runCommand(sender.getEntityWorld(), sender.getPosition(), name, command, player, sender);
	}

	public static String runCommand(World world, BlockPos pos, String name, String command, EntityPlayer player, ICommandSender sender) {
		if (!Objects.requireNonNull(world.getMinecraftServer()).isCommandBlockEnabled()) {
			LogWriter.warn("Cant run commands if CommandBlocks are disabled");
			return "Cant run commands if CommandBlocks are disabled";
		}
		if (player != null) {
			command = command.replace("@dp", player.getName());
		}
		command = command.replace("@npc", name);
		TextComponentString output = new TextComponentString("");
		ICommandSender icommandsender = new RConConsoleSource(world.getMinecraftServer()) {
			public boolean canUseCommand(int permLevel, @Nonnull String commandName) {
				return CustomNpcs.NpcUseOpCommands || permLevel <= 2;
			}

			public Entity getCommandSenderEntity() {
				if (sender == null) {
					return null;
				}
				return sender.getCommandSenderEntity();
			}

			public @Nonnull ITextComponent getDisplayName() {
				return new TextComponentString(this.getName());
			}

			public @Nonnull World getEntityWorld() {
				return world;
			}

			public @Nonnull String getName() {
				return "@CustomNPCs-" + name;
			}

			public @Nonnull BlockPos getPosition() {
				return pos;
			}

			public @Nonnull Vec3d getPositionVector() {
				return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			}

			public boolean sendCommandFeedback() {
				return Objects.requireNonNull(this.getServer()).worlds[0].getGameRules().getBoolean("commandBlockOutput");
			}

			public void sendMessage(@Nonnull ITextComponent component) {
				output.appendSibling(component);
			}
		};
		ICommandManager icommandmanager = world.getMinecraftServer().getCommandManager();
		icommandmanager.executeCommand(icommandsender, command);
		if (output.getUnformattedText().isEmpty()) {
			return null;
		}
		return output.getUnformattedText();
	}

	public static TileEntity saveTileEntity(EntityPlayerMP player, NBTTagCompound compound) {
		int x = compound.getInteger("x");
		int y = compound.getInteger("y");
		int z = compound.getInteger("z");
		TileEntity tile = player.world.getTileEntity(new BlockPos(x, y, z));
		if (tile != null) {
			tile.readFromNBT(compound);
		}
		return tile;
	}

	public static void sendBank(EntityPlayerMP player, Bank bank, int ceil) {
		NBTTagCompound compound = new NBTTagCompound();
		bank.writeToNBT(compound);
		compound.setInteger("CurrentCeil", Math.max(ceil, 0));
		Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		if (player.openContainer instanceof ContainerManageBanks) {
			((ContainerManageBanks) player.openContainer).setBank(bank, ceil);
		}
		player.sendAllContents(player.openContainer, player.openContainer.getInventory());
	}

	public static void sendBankDataAll(EntityPlayerMP player) {
		Map<String, Integer> map = new HashMap<>();
		for (Bank bank : BankController.getInstance().banks.values()) {
			map.put(bank.name, bank.id);
		}
		sendScrollData(player, map);
	}

	private static void sendExtraData(EntityPlayer player, EntityNPCInterface npc, EnumGuiType gui) {
		if (gui == EnumGuiType.PlayerFollower || gui == EnumGuiType.PlayerFollowerHire || gui == EnumGuiType.PlayerTrader || gui == EnumGuiType.PlayerTransporter) {
			sendRoleData(player, npc);
		}
	}

	public static void sendFactionDataAll(EntityPlayerMP player) {
		Map<String, Integer> map = new HashMap<>();
		for (Faction faction : FactionController.instance.factions.values()) {
			map.put(faction.name, faction.id);
		}
		sendScrollData(player, map);
	}

	public static void sendGuiClose(EntityPlayerMP player, int i, NBTTagCompound comp) {
		Server.sendData(player, EnumPacketClient.GUI_CLOSE, i, comp);
	}

	public static void sendGuiError(EntityPlayer player, int i) {
		Server.sendData((EntityPlayerMP) player, EnumPacketClient.GUI_ERROR, i, new NBTTagCompound());
	}

	public static void sendNearbyEntitys(EntityPlayerMP player, boolean all) { // to gui show
		HashMap<Float, NBTTagCompound> map = new HashMap<>();
		List<Float> alist = Lists.newArrayList();
		List<Float> nlist = Lists.newArrayList();
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Entity entity : player.world.loadedEntityList) {
			if (entity.isDead || (!all && !(entity instanceof EntityNPCInterface))) {
				continue;
			}
			if (entity instanceof EntityPlayer && entity.getName().equals(player.getName())) {
				continue;
			}
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("Id", entity.getEntityId());
			float distance = player.getDistance(entity);
			if (entity instanceof EntityNPCInterface) {
				nlist.add(distance);
			} else {
				alist.add(distance);
			}
			map.put(distance, nbt);
		}
		Collections.sort(alist);
		Collections.sort(nlist);
		for (float d : nlist) {
			list.appendTag(map.get(d));
		}
		for (float d : alist) {
			list.appendTag(map.get(d));
		}
		compound.setTag("Data", list);
		Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
	}

	public static void sendNpcDialogs(EntityPlayer player) {
		EntityNPCInterface npc = getEditingNpc(player);
		if (npc == null) {
			return;
		}
		int slot = 0;
		for (int dialogId : npc.dialogs) {
			if (!DialogController.instance.hasDialog(dialogId)) {
				continue;
			}
			Dialog d = (Dialog) DialogController.instance.get(dialogId);
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("Id", d.id);
			compound.setInteger("Slot", slot);
			compound.setString("Category", d.category.title);
			compound.setString("Title", d.title);
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.GUI_DATA, compound);
			slot++;
		}
	}

	public static void sendOpenGui(EntityPlayer player, EnumGuiType gui, EntityNPCInterface npc) {
		sendOpenGui(player, gui, npc, 0, 0, 0);
	}

	public static void sendOpenGui(EntityPlayer player, EnumGuiType gui, EntityNPCInterface npc, int x, int y, int z) {
		if (!(player instanceof EntityPlayerMP)) {
			return;
		}
		setEditingNpc(player, npc);
		sendExtraData(player, npc, gui);
		CustomNPCsScheduler.runTack(() -> {
			try {
				if (CustomNpcs.proxy.getServerGuiElement(gui.ordinal(), player, player.world, x, y, z) != null) {
					player.openGui(CustomNpcs.instance, gui.ordinal(), player.world, x, y, z);
				} else {
					Server.sendDataChecked((EntityPlayerMP) player, EnumPacketClient.GUI, gui.ordinal(), x, y, z);
					Map<String, Integer> map = getScrollData(player, gui, npc);
					sendScrollData((EntityPlayerMP) player, map);
				}
			}
			catch (Exception e) { LogWriter.error("Error:", e); }
		}, 100);
	}

	public static void sendPlayerData(EnumPlayerData type, EntityPlayerMP player, String name) {
		Map<String, Integer> map = new HashMap<>();
		if (type == EnumPlayerData.Players) {
			for (String username : PlayerDataController.instance.getPlayerNames()) {
				map.put(username, 0);
			}
			for (String username : Objects.requireNonNull(player.getServer()).getPlayerList().getOnlinePlayerNames()) {
				map.put(username, 1);
			}
		} else {
			PlayerData playerdata = PlayerDataController.instance.getDataFromUsername(Objects.requireNonNull(player.getServer()), name);
			if (type == EnumPlayerData.Dialog) {
				PlayerDialogData data = playerdata.dialogData;
				for (int questId : data.dialogsRead) {
					Dialog dialog = DialogController.instance.dialogs.get(questId);
					if (dialog == null) {
						continue;
					}
					map.put(dialog.category.title + ": " + dialog.title, questId);
				}
			} else if (type == EnumPlayerData.Quest) {
				PlayerQuestData data2 = playerdata.questData;
				for (int questId : data2.activeQuests.keySet()) {
					Quest quest = QuestController.instance.quests.get(questId);
					if (quest == null) {
						continue;
					}
					map.put(quest.category.title + ": " + quest.getTitle() + "(Active quest)", questId); // Changed
				}
				for (int questId : data2.finishedQuests.keySet()) {
					Quest quest = QuestController.instance.quests.get(questId);
					if (quest == null) {
						continue;
					}
					map.put(quest.category.title + ": " + quest.getTitle() + "(Finished quest)", questId); // Changed
				}
			} else if (type == EnumPlayerData.Transport) {
				PlayerTransportData data3 = playerdata.transportData;
				for (int transportId : data3.transports) {
					TransportLocation location = TransportController.getInstance().getTransport(transportId);
					if (location == null) {
						continue;
					}
					map.put(location.category.title + ": " + location.name, transportId);
				}
				/*} else if (type == EnumPlayerData.Bank) {
				 * PlayerBankData data4 = playerdata.bankData; for (int bankId :
				 * data4.banks.keySet()) { Bank bank =
				 * BankController.getInstance().banks.get(bankId); if (bank == null) { continue;
				 * } map.put(bank.name, bankId); }
				 */
			} else if (type == EnumPlayerData.Factions) {
				PlayerFactionData data5 = playerdata.factionData;
				for (int factionId : data5.factionData.keySet()) {
					Faction faction = FactionController.instance.factions.get(factionId);
					if (faction == null) {
						continue;
					}
					map.put(faction.name + ";" + data5.getFactionPoints(player, factionId), factionId);
				}
			} else if (type == EnumPlayerData.Game) {
				Server.sendData(player, EnumPacketClient.GUI_DATA, playerdata.game.saveNBTData(new NBTTagCompound()));
			}
		}
		sendScrollData(player, map);
	}

	public static void sendRoleData(EntityPlayer player, EntityNPCInterface npc) {
		if (npc == null) {
			return;
		}
		NBTTagCompound comp = new NBTTagCompound();
		npc.advanced.roleInterface.writeToNBT(comp);
		comp.setInteger("EntityId", npc.getEntityId());
		Server.sendData((EntityPlayerMP) player, EnumPacketClient.ROLE, comp);
	}

	public static void sendScrollData(EntityPlayerMP player, Map<String, Integer> map) {
		Map<String, Integer> send = new HashMap<>();
		for (String key : map.keySet()) {
			send.put(key, map.get(key));
			if (send.size() == 100) {
				Server.sendData(player, EnumPacketClient.SCROLL_DATA_PART, send);
				send = new HashMap<>();
			}
		}
		Server.sendData(player, EnumPacketClient.SCROLL_DATA, send);
	}

	public static void sendTransportData(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.TransportData,
				TransportController.getInstance().getNBT());
		Server.sendData(player, EnumPacketClient.GUI_DATA, new NBTTagCompound());
	}

	public static void sendTransportData(EntityPlayerMP player, int categoryid) {
		TransportCategory category = TransportController.getInstance().categories.get(categoryid);
		HashMap<String, Integer> map = Maps.newHashMap();
		if (category != null) {
			for (TransportLocation transport : category.locations.values()) {
				map.put(transport.name, transport.id);
			}
		} else {
			for (TransportCategory cat : TransportController.getInstance().categories.values()) {
				map.put(cat.title, cat.id);
			}
		}
		sendScrollData(player, map);
	}

	public static void setEditingNpc(EntityPlayer player, EntityNPCInterface npc) {
		PlayerData data = PlayerData.get(player);
		data.editingNpc = npc;
		Server.sendDataChecked((EntityPlayerMP) player, EnumPacketClient.EDIT_NPC, npc != null ? npc.getEntityId() : -1);
	}

	public static void setEditingQuest(EntityPlayer player, Quest quest) {
		if (player.world.isRemote) {
			NoppesUtilServer.editingQuestsClient.put(player.getUniqueID(), quest);
		} else {
			NoppesUtilServer.editingQuests.put(player.getUniqueID(), quest);
		}
	}

	public static NBTTagCompound setNpcDialog(int slot, int dialogId, EntityPlayer player) {
		EntityNPCInterface npc = getEditingNpc(player);
		if (npc == null || !DialogController.instance.hasDialog(dialogId)) {
			return null;
		}
		if (slot >= 0 && slot < npc.dialogs.length) {
			npc.dialogs[slot] = dialogId;
		} // change
		else { // add
			int[] newIDs = new int[npc.dialogs.length + 1];
            System.arraycopy(npc.dialogs, 0, newIDs, 0, npc.dialogs.length);
			slot = npc.dialogs.length;
			newIDs[slot] = dialogId;
			npc.dialogs = newIDs;
		}
		Dialog d = (Dialog) DialogController.instance.get(dialogId);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("Id", d.id);
		compound.setInteger("Slot", slot);
		compound.setString("Category", d.category.title);
		compound.setString("Title", d.title);
		return compound;
	}

	public static Entity spawnClone(NBTTagCompound compound, double x, double y, double z, World world) {
		if (world == null || world.isRemote) {
			LogWriter.error("Clone summoning Error: World is Client: " + (world == null ? "null" : "true") + " - " + world);
			return null;
		}
		if (compound == null) {
			LogWriter.error("Clone summoning Error: Missing NBT Tags: "
					+ "null or World: "
					+ world.provider.getDimension());
			return null;
		}
		ServerCloneController.Instance.cleanTags(compound);
		compound.setTag("Pos", NBTTags.nbtDoubleList(x, y, z));
		Entity entity = EntityList.createEntityFromNBT(compound, world);
		if (entity == null) {
			LogWriter.error("Clone summoning error: Failed to create an entity based on the passed NBT tags: " + compound);
			return null;
		}
		if (entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			npc.ais.setStartPos(new BlockPos(npc));
		}
		world.spawnEntity(entity);
		LogWriter.debug("Summon Clone: Successful \"" + entity.getName() + "\"; " + entity.world.isRemote);
		return entity;
	}

	public static void spawnParticle(Entity entity, String particle) {
		Server.sendAssociatedData(entity, EnumPacketClient.PARTICLE, entity.posX, entity.posY, entity.posZ, entity.height, entity.width, particle);
	}

}
