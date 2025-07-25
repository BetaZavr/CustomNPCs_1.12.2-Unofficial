package noppes.npcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerGameData.FollowerSet;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScenes;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.Util;
import noppes.npcs.util.BuilderData;

public class ServerTickHandler {

	private final static Map<EntityPlayerMP, GameType> visibleData = new HashMap<>();
	public static int ticks = 0;

	@SuppressWarnings("all")
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != Side.SERVER || event.phase != TickEvent.Phase.START) { return; }
		CustomNpcs.debugData.start("Player");
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		if (player.getHealth() > 0 && player.getHealth() < 1.0f) {
			player.setHealth(1.0f);
		}
		PlayerData data = PlayerData.get(player);
		long resTime = player.getName().codePointAt(0);
		if (!ServerTickHandler.visibleData.containsKey(player)
				|| ServerTickHandler.visibleData.get(player) != player.interactionManager.getGameType()
				|| player.world.getTotalWorldTime() % 100L == resTime % 100L
				|| (data.prevHeldItem != player.getHeldItemMainhand()
				&& (data.prevHeldItem.getItem() == CustomRegisters.wand
				|| player.getHeldItemMainhand().getItem() == CustomRegisters.wand))) {
			ServerTickHandler.visibleData.put(player, player.interactionManager.getGameType());
			CustomNpcs.visibilityController.onUpdate(player);
		}
		if (player.world.getTotalWorldTime() % 20L == resTime % 20L) {
			data.hud.updateHud(player);
			data.minimap.update(player);
			boolean isOP = false;
			if (player.getServer() != null) {
				if (player.getServer().isSinglePlayer()) {
					isOP = true;
				} else {
					UserListOpsEntry util = player.getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
					if (util != null) {
						isOP = util.getPermissionLevel() >= 4;
					}
				}
			}
			if (data.game.op != isOP) {
				data.game.op = isOP;
				data.game.updateClient = true;
			}
			if (player.openContainer instanceof ContainerNPCBank) {
				ContainerNPCBank c = (ContainerNPCBank) player.openContainer;
				boolean work = true;
				int ceil = c.bank.ceilSettings.size();
				CeilSettings cs;
				// open
				if (c.items.getSizeInventory() == 0) {
					if (c.ceil > 0) {
						for (int i = c.ceil - 1; i >= 0 && c.data.cells.containsKey(i); i--) {
							NpcMiscInventory inv = c.data.cells.get(i);
							cs = c.bank.ceilSettings.get(i);
							work = inv.getSizeInventory() > 0 && cs.upgradeStack.isEmpty()
									|| cs.maxCells == inv.getSizeInventory();
							if (!work) {
								ceil = i;
								break;
							}
						}
					}
					if (work) {
						cs = c.bank.ceilSettings.get(c.ceil);
						if (!cs.openStack.isEmpty()) {
							int count = Util.instance.inventoryItemCount(player, cs.openStack,
									null, false, false);
							if (count < cs.openStack.getCount()) {
								ceil = -1;
							}
						}
					}
				}
				// update
				else {
					cs = c.bank.ceilSettings.get(c.ceil);
					work = c.items.getSizeInventory() > 0 && c.items.getSizeInventory() < cs.maxCells;
					if (work) {
						if (!cs.upgradeStack.isEmpty()) {
							int count = Util.instance.inventoryItemCount(player, cs.upgradeStack,
									null, false, false);
							if (count < cs.upgradeStack.getCount()) {
								ceil = -1;
							}
						}
					}
				}
				if (c.dataCeil != ceil) {
					c.dataCeil = ceil;
					Server.sendData(player, EnumPacketClient.BANK_CEIL_OPEN, ceil);
				}
			}
			List<FollowerSet> del = new ArrayList<>();
			for (FollowerSet fs : data.game.getFollowers()) {
				EntityNPCInterface npc = null;
				if (fs.npc != null) {
					npc = fs.npc;
				}
				if (npc == null) {
					Entity e = Util.instance.getEntityByUUID(fs.id, player.world);
					if (e instanceof EntityNPCInterface) {
						npc = (EntityNPCInterface) e;
					}
				}
				if (npc == null || npc.isDead || !(npc.advanced.roleInterface instanceof RoleFollower)) {
					del.add(fs);
				} else {
					EntityPlayer owner = ((RoleFollower) npc.advanced.roleInterface).getOwner();
					if (owner == null || !owner.equals(player)) {
						del.add(fs);
					} else if (fs.npc == null) {
						fs.npc = npc;
					}
				}
				if (npc != null && npc.advanced.roleInterface instanceof RoleFollower) {
					if (player.world.provider.getDimension() != npc.world.provider.getDimension()) {
						try {
							Entity entity = Util.instance.teleportEntity(player.world.getMinecraftServer(), npc, player.world.provider.getDimension(), player.posX, player.posY, player.posZ);
							if (entity instanceof EntityNPCInterface) {
								fs.dimId = entity.world.provider.getDimension();
								fs.id = entity.getUniqueID();
								((EntityNPCInterface) entity).getNavigator().tryMoveToEntityLiving(player, ((EntityNPCInterface) entity).ais.canSprint ? 1.3 : 1.0d);
							}
						} catch (CommandException e) {
							LogWriter.error("Error when trying to move an entity:", e);
						}
					} else if (npc.advanced.roleInterface instanceof RoleFollower
							&& player.getDistance(npc) > ((RoleFollower) npc.advanced.roleInterface).getRange()) {
						npc.setPosition(player.posX, player.posY, player.posZ);
					}
				}
			}
			for (FollowerSet fs : del) {
				data.game.removeFollower(fs);
			}
		}

		if (player.world.getTotalWorldTime() % 200L == resTime % 200L) {
			if (data.hud.currentGUI.equalsIgnoreCase("guichat") || data.hud.currentGUI.equalsIgnoreCase("guiingame")) {
				for (QuestData questData : data.questData.activeQuests.values()) {
					data.questData.checkQuestCompletion(player, questData);
				}
			}
			if (!data.mailData.playerMails.isEmpty()) {
				boolean needSend = false;
				long time = System.currentTimeMillis();
				long timeToRemove = -1L;
				if (CustomNpcs.MailTimeWhenLettersWillBeDeleted > 0) {
					timeToRemove = CustomNpcs.MailTimeWhenLettersWillBeDeleted * 86400000L;
				}
				List<PlayerMail> del = new ArrayList<>();
				for (PlayerMail mail : data.mailData.playerMails) {
					if (player.capabilities.isCreativeMode && mail.timeWillCome > 0L) {
						mail.timeWillCome = 0L;
						needSend = true;
					}
					long timeWhenReceived = time - mail.timeWhenReceived - mail.timeWillCome;
					if (timeToRemove > 0L && timeWhenReceived > timeToRemove) {
						del.add(mail);
						needSend = true;
					}
					if (mail.beenRead || timeWhenReceived < 0L) {
						continue;
					}
					needSend = true;
				}
				for (PlayerMail mail : del) {
					data.mailData.playerMails.remove(mail);
				}
				if (needSend) {
					Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.MailData,
							data.mailData.saveNBTData(new NBTTagCompound()));
				}
			}
		}
		// Updates
		if (data.updateClient) {
			Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.PlayerData, data.getSyncNBT());
			data.updateClient = false;
		}
		if (data.questData.updateClient) {
			NBTTagCompound compound = new NBTTagCompound();
			data.questData.saveNBTData(compound);
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.PlayerQuestData, compound);
			data.questData.updateClient = false;
		}
		if (data.game.updateClient) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.GameData,
					data.game.saveNBTData(new NBTTagCompound()));
			data.game.updateClient = false;
		}
		data.bankData.update(player);
		data.prevHeldItem = player.getHeldItemMainhand();
		CustomNpcs.debugData.end("Player");
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.CLIENT || event.phase == TickEvent.Phase.END) { return; }
		CustomNpcs.debugData.start(null);
		BorderController.getInstance().update();
		++ticks;
		if (ticks % 20 == 0) {
			SchematicController.Instance.updateBuilding();
			MassBlockController.Update();
			MarcetController.getInstance().update();
			for (DataScenes.SceneState state : DataScenes.StartedScenes.values()) {
				if (!state.paused) {
                    ++state.ticks;
				}
			}
			for (DataScenes.SceneContainer entry : DataScenes.ScenesToRun) {
				entry.update();
			}
			DataScenes.ScenesToRun.clear();
			if (ticks % 6000 == 0) { // Deleting a construction date from the database every 5 min, for dates without a player
				List<Integer> del = new ArrayList<>();
				for (int id : SyncController.dataBuilder.keySet()) {
					BuilderData bd = SyncController.dataBuilder.get(id);
					if (bd.player == null) {
						del.add(id);
						continue;
					}
					ItemStack stack = null;
					for (ItemStack s : bd.player.inventory.mainInventory) {
						if (ItemBuilder.isBuilder(s, bd)) {
							stack = s;
							break;
						}
					}
					if (stack == null) { del.add(id); }
				}
				for (Integer id : del) {
					SyncController.dataBuilder.remove(id);
				}
			}
		}
		if (ticks % 10 == 0 && CustomNpcs.Server != null && !CustomNpcs.Server.getPlayerList().getPlayers().isEmpty()) {
			EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayers().get(0);
			if (player != null) { EventHooks.onEvent(PlayerData.get(player).scriptData, "worldtick", new WorldEvent.ServerTickEvent(event)); }
		}
		if (ticks % 1200 == 0) {
			BankController.getInstance().update();
		}
		if (ticks % 60 == 0) { BlockWrapper.checkClearCache(); }
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void onServerWorldTick(TickEvent.WorldTickEvent event) {
		if (event.side != Side.SERVER) { return; }
		CustomNpcs.debugData.start(null);
		if (event.phase == TickEvent.Phase.START) {
			NPCSpawning.findChunksForSpawning((WorldServer) event.world);
		}
		CustomNpcs.debugData.end(null);
	}

}
