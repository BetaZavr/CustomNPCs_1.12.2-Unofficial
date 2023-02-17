package noppes.npcs;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.AnalyticsTracking;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.MassBlockController;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.data.DataScenes;

public class ServerTickHandler {
	public int ticks;

	public ServerTickHandler() {
		this.ticks = 0;
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != Side.SERVER || event.phase != TickEvent.Phase.START) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", event.player, "ServerTickHandler_onPlayerTick");
		EntityPlayer player = event.player;
		if (player.getHealth() > 0 && player.getHealth() < 1.0f) {
			player.setHealth(1.0f);
		}
		// New - Fix setAttackTarget(player)
		PlayerData data = PlayerData.get(player);
		long resTime = (long) player.getName().codePointAt(0) % 100L;
		if (player.getEntityWorld().getWorldTime() % 100L == resTime) {
			VisibilityController.onUpdate((EntityPlayerMP) player); // any 5 sec.
		}
		if (data.updateClient) {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNC_END, 8, data.getSyncNBT());
			VisibilityController.onUpdate((EntityPlayerMP) player);
			data.updateClient = false;
		}
		if (data.game.update) {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNC_UPDATE, 9, data.game.saveNBTData(new NBTTagCompound()));
			data.game.update = false;
		}
		if (data.prevHeldItem != player.getHeldItemMainhand() && (data.prevHeldItem.getItem() == CustomItems.wand || player.getHeldItemMainhand().getItem() == CustomItems.wand)) {
			VisibilityController.onUpdate((EntityPlayerMP) player); // Wand Item
		}
		data.prevHeldItem = player.getHeldItemMainhand();
		CustomNpcs.debugData.endDebug("Server", event.player, "ServerTickHandler_onPlayerTick");
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START && this.ticks++ >= 20) {
			CustomNpcs.debugData.startDebug("Server", "Mod", "ServerTickHandler_onServerTick");
			SchematicController.Instance.updateBuilding();
			MassBlockController.Update();
			this.ticks = 0;
			for (DataScenes.SceneState state : DataScenes.StartedScenes.values()) {
				if (!state.paused) {
					DataScenes.SceneState sceneState = state;
					++sceneState.ticks;
				}
			}
			for (DataScenes.SceneContainer entry : DataScenes.ScenesToRun) {
				entry.update();
			}
			DataScenes.ScenesToRun = new ArrayList<DataScenes.SceneContainer>();
			CustomNpcs.debugData.endDebug("Server", "Mod", "ServerTickHandler_onServerTick");
		}
	}

	@SubscribeEvent
	public void onServerWorldTick(TickEvent.WorldTickEvent event) {
		if (event.side == Side.SERVER) {
			CustomNpcs.debugData.startDebug("Server", "Mod", "ServerTickHandler_onServerWorldTick");
			if (event.phase == TickEvent.Phase.START) {
				NPCSpawning.findChunksForSpawning((WorldServer) event.world);
			}
			if (event.world.provider.getDimension() == 0 && event.world.getTotalWorldTime() % 10 == 0) {
				MarcetController.getInstance().update();
			}
			CustomNpcs.debugData.endDebug("Server", "Mod", "ServerTickHandler_onServerWorldTick");
		}
	}

	@SubscribeEvent
	public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		CustomNpcs.proxy.updateRecipeBook(event.player);
		if (event.player.world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", event.player, "ServerTickHandler_playerLogin");
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		MinecraftServer server = event.player.getServer();

		for (WorldServer world : server.worlds) {
			ServerScoreboard board = (ServerScoreboard) world.getScoreboard();
			for (String objective : Availability.scores) {
				ScoreObjective so = board.getObjective(objective);
				if (so != null) {
					if (board.getObjectiveDisplaySlotCount(so) == 0) {
						player.connection.sendPacket(new SPacketScoreboardObjective(so, 0));
					}
					Score sco = board.getOrCreateScore(player.getName(), so);
					player.connection.sendPacket(new SPacketUpdateScore(sco));
				}
			}
		}

		event.player.inventoryContainer.addListener(new IContainerListener() {
			public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
			}

			public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
			}

			public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
				if (player.world.isRemote) {
					return;
				}
				PlayerQuestData playerdata = PlayerData.get(event.player).questData;
				for (QuestData data : playerdata.activeQuests.values()) { // Changed
					for (IQuestObjective obj : data.quest
							.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
						if (obj.getType() != 0) {
							continue;
						}
						playerdata.checkQuestCompletion(player, data);
					}
				}
			}

			public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
			}
		});

		if (server.isSnooperEnabled()) {
			String serverName = null;
			if (server.isDedicatedServer()) {
				serverName = "server";
			} else {
				serverName = (((IntegratedServer) server).getPublic() ? "lan" : "local");
			}
			AnalyticsTracking.sendData(event.player, "join", serverName);
		}
		SyncController.syncPlayer(player);
		CustomNpcs.debugData.endDebug("Server", event.player, "ServerTickHandler_playerLogin");
	}
}
