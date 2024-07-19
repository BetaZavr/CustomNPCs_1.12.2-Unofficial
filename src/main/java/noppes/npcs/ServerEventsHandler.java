package noppes.npcs;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFutureTask;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandGive;
import net.minecraft.command.CommandTime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.registries.ForgeRegistry;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.WrapperEntityData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemSoulstoneEmpty;
import noppes.npcs.quests.QuestObjective;

public class ServerEventsHandler {

	public static EntityVillager Merchant;
	public static Entity mounted;

	private void doFactionPoints(EntityPlayer player, EntityNPCInterface npc) {
		if (npc.advanced.factions.hasOptions()) {
			npc.advanced.factions.addPoints(player);
		} else {
			npc.faction.factions.addPoints(player);
		}
	}

	private void doKillQuest(EntityPlayer player, EntityLivingBase entity, boolean forAll) {
		PlayerData pdata = PlayerData.get(player);
		PlayerQuestData playerdata = pdata.questData;
		String entityName = EntityList.getEntityString(entity);
		if (entity instanceof EntityPlayer) {
			entityName = "Player";
		}
		for (QuestData data : playerdata.activeQuests.values()) { // Changed
			if (data.quest.step == 2 && data.quest.questInterface.isCompleted(player)) {
				continue;
			}
			boolean bo = data.quest.step == 1;
			for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
				if (data.quest.step == 1 && !bo) {
					break;
				}
				bo = obj.isCompleted();
				if (((QuestObjective) obj).getEnumType() != EnumQuestTask.KILL
						&& ((QuestObjective) obj).getEnumType() != EnumQuestTask.AREAKILL) {
					continue;
				}
				String name = null;
				if (obj.getTargetName().equals(entity.getName())) {
					name = entity.getName();
				} else if (obj.getTargetName().equals(entityName)) {
					name = entityName;
				} else if (obj.isPartName() || obj.isAndTitle()) {
					if (obj.isPartName()) {
						if (entity.getName().contains(obj.getTargetName())) {
							name = obj.getTargetName();
						} else {
                            assert entityName != null;
                            if (entityName.contains(obj.getTargetName())) {
                                name = obj.getTargetName();
                            }
                        }
					}
					if (name == null && obj.isAndTitle() && entity instanceof EntityNPCInterface) {
						EntityNPCInterface npc = (EntityNPCInterface) entity;
						String title = npc.display.getTitle();
						if (title.equals(obj.getTargetName())) {
							name = entity.getName();
						} else if (title.equals(entityName)) {
							name = entityName;
						}
						if (name == null && obj.isPartName()) {
							if (title.contains(obj.getTargetName())) {
								name = obj.getTargetName();
							} else if (title.contains(obj.getTargetName())) {
								name = obj.getTargetName();
							}
						}
					}
				} else {
					continue;
				}
				if (name == null) {
					continue;
				}
				if (obj.getType() == EnumQuestTask.AREAKILL.ordinal() && forAll) {
					List<EntityPlayer> list = player.world.getEntitiesWithinAABB(EntityPlayer.class, entity
							.getEntityBoundingBox().grow(obj.getAreaRange(), obj.getAreaRange(), obj.getAreaRange()));
					for (EntityPlayer pl : list) {
						if (pl != player) {
							this.doKillQuest(pl, entity, false);
						}
					}
				}
				HashMap<String, Integer> killed = ((QuestObjective) obj).getKilled(data); // in Data
				if (killed.containsKey(name) && killed.get(name) >= obj.getMaxProgress()) {
					continue;
				}
				int amount = 0;
				if (killed.containsKey(name)) {
					amount = killed.get(name);
				}
				amount++;
				killed.put(name, amount);
				((QuestObjective) obj).setKilled(data, killed);
				// New
				if (data.quest.showProgressInWindow) {
					NBTTagCompound compound = new NBTTagCompound();
					compound.setInteger("QuestID", data.quest.id);
					compound.setString("Type", "kill");
					compound.setIntArray("Progress", new int[] { amount, obj.getMaxProgress() });
					compound.setString("TargetName", new TextComponentTranslation("script.killed").getFormattedText()
							+ ": \"" + entity.getName() + "\"");
					compound.setInteger("MessageType", 0);
					Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE_DATA, compound);
				}
				if (data.quest.showProgressInChat) {
					if (amount >= obj.getMaxProgress()) {
						player.sendMessage(new TextComponentTranslation("quest.message.kill.1",
								new TextComponentTranslation(entity.getName()).getFormattedText(),
								data.quest.getTitle()));
					} else {
						player.sendMessage(new TextComponentTranslation("quest.message.kill.0",
								new TextComponentTranslation(entity.getName()).getFormattedText(), "" + amount,
								"" + obj.getMaxProgress(), data.quest.getTitle()));
					}
				}
				playerdata.checkQuestCompletion(player, data);
				playerdata.updateClient = true;
			}
		}
	}

	@SubscribeEvent
	public void joinServer(ServerConnectionFromClientEvent event) {
		event.getManager().channel().pipeline().addBefore("fml:packet_handler",
				CustomNpcs.MODID + ":custom_packet_handler_server", new CustomPacketHandler());
	}

	@SubscribeEvent
	public void npcCommands(CommandEvent event) {
		CustomNpcs.debugData.startDebug(!event.getSender().getEntityWorld().isRemote ? "Server" : "Client",
				event.getSender(), "ServerEventsHandler_npcCommands");
		if (event.getCommand() instanceof CommandGive) {
			if (!(event.getSender().getEntityWorld() instanceof WorldServer)) {
				CustomNpcs.debugData.endDebug(!event.getSender().getEntityWorld().isRemote ? "Server" : "Client",
						event.getSender(), "ServerEventsHandler_npcCommands");
				return;
			}
			try {
				EntityPlayer player = CommandBase.getPlayer(Objects.requireNonNull(event.getSender().getServer()),  event.getSender(), event.getParameters()[0]);
				Objects.requireNonNull(player.getServer()).futureTaskQueue.add(ListenableFutureTask.create(Executors.callable(() -> {
					PlayerQuestData playerdata = PlayerData.get(player).questData;
					for (QuestData data : playerdata.activeQuests.values()) {
						for (IQuestObjective obj : data.quest
								.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
							if (obj.getType() != EnumQuestTask.ITEM.ordinal()) {
								continue;
							}
							playerdata.checkQuestCompletion(player, data);
							playerdata.updateClient = true;
						}
					}
				})));
			} catch (Exception e) {
				LogWriter.error("Error player check quest completion:", e);
			}
		} else if (event.getCommand() instanceof CommandTime) {
			try {
				List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
				for (EntityPlayerMP playerMP : players) {
					CustomNpcs.visibilityController.onUpdate(playerMP);
				}
			} catch (Exception e) {
				LogWriter.error("Error player update visible NPC:", e);
			}
		}
		CustomNpcs.debugData.endDebug(!event.getSender().getEntityWorld().isRemote ? "Server" : "Client",
				event.getSender(), "ServerEventsHandler_npcCommands");
	}

	@SubscribeEvent
	public void npcDeath(LivingDeathEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", event.getEntityLiving(), "ServerEventsHandler_npcDeath");
		Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
		if (source != null) {
			if (source instanceof EntityNPCInterface && event.getEntityLiving() != null) {
				EntityNPCInterface npc = (EntityNPCInterface) source;
				Line line = npc.advanced.getKillLine();
				if (line != null) {
					npc.saySurrounding(Line.formatTarget(line, event.getEntityLiving()));
				}
				EventHooks.onNPCKills(npc, event.getEntityLiving());
			}
			EntityPlayer player = null;
			if (source instanceof EntityPlayer) {
				player = (EntityPlayer) source;
			} else if (source instanceof EntityNPCInterface
					&& ((EntityNPCInterface) source).getOwner() instanceof EntityPlayer) {
				player = (EntityPlayer) ((EntityNPCInterface) source).getOwner();
			}
			if (player != null) {
				this.doKillQuest(player, event.getEntityLiving(), true);
				if (event.getEntityLiving() instanceof EntityNPCInterface) {
					this.doFactionPoints(player, (EntityNPCInterface) event.getEntityLiving());
				}
			}
		}
		if (event.getEntityLiving() instanceof EntityPlayer) {
			PlayerData data = PlayerData.get((EntityPlayer) event.getEntityLiving());
			data.save(false);
		}
		CustomNpcs.debugData.endDebug("Server", event.getEntityLiving(), "ServerEventsHandler_npcDeath");
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void npcEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		String side = "Common";
		if (event.getObject() != null && event.getObject().world != null) {
			side = !(event.getObject()).world.isRemote ? "Server" : "Client";
		}
		CustomNpcs.debugData.startDebug(side, event.getObject(), "ServerEventsHandler_npcEntityCapabilities");
		if (event.getObject() instanceof EntityPlayer) {
			PlayerData.register(event);
		}
		if (event.getObject() instanceof EntityLivingBase) {
			MarkData.register(event);
		}
		if (event.getObject().world != null) {
			try {
				WrapperEntityData.register(event);
			} catch (Exception e) {
				LogWriter.error("Error register wrapper entity:", e);
			}
		}
		CustomNpcs.debugData.endDebug(side, event.getObject(), "ServerEventsHandler_npcEntityCapabilities");
	}

	@SubscribeEvent
	public void npcEntityJoin(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityPlayer)) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", event.getEntity(), "ServerEventsHandler_npcEntityJoin");
		PlayerData data = PlayerData.get((EntityPlayer) event.getEntity());
		data.updateCompanion(event.getWorld());
		CustomNpcs.debugData.endDebug("Server", event.getEntity(), "ServerEventsHandler_npcEntityJoin");
	}

	@SubscribeEvent
	public void npcItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject().getItem() instanceof UniversalBucket) {
			return;
		}
		CustomNpcs.debugData.startDebug(CustomNpcs.proxy.getPlayer() != null ? "Client" : "Server", null,
				"ServerEventsHandler_npcItemCapabilities");
		ItemStackWrapper.register(event);
		CustomNpcs.debugData.endDebug(CustomNpcs.proxy.getPlayer() != null ? "Client" : "Server", null,
				"ServerEventsHandler_npcItemCapabilities");
	}

	@SubscribeEvent
	public void npcPlayerInteract(PlayerInteractEvent.EntityInteract event) {
		ItemStack item = event.getEntityPlayer().getHeldItemMainhand();
		boolean isClient = event.getEntityPlayer().world.isRemote;
		CustomNpcs.debugData.startDebug(!isClient ? "Server" : "Client", event.getEntityPlayer(),
				"ServerEventsHandler_npcPlayerInteract");
		boolean npcInteracted = event.getTarget() instanceof EntityNPCInterface;
		if (!isClient && CustomNpcs.OpsOnly && !Objects.requireNonNull(event.getEntityPlayer().getServer()).getPlayerList()
				.canSendCommands(event.getEntityPlayer().getGameProfile())) {
			CustomNpcs.debugData.endDebug("Server", event.getEntityPlayer(),
					"ServerEventsHandler_npcPlayerInteract");
			return;
		}
		if (!isClient && item.getItem() == CustomRegisters.soulstoneEmpty
				&& event.getTarget() instanceof EntityLivingBase) {
			((ItemSoulstoneEmpty) item.getItem()).store((EntityLivingBase) event.getTarget(), item,
					event.getEntityPlayer());
		}
		if (item.getItem() == CustomRegisters.wand && npcInteracted && !isClient) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.NPC_GUI)) {
				CustomNpcs.debugData.endDebug("Server", event.getEntityPlayer(),
						"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			event.setCanceled(true);
			NoppesUtilServer.sendOpenGui(event.getEntityPlayer(), EnumGuiType.MainMenuDisplay,
					(EntityNPCInterface) event.getTarget());
		} else if (item.getItem() == CustomRegisters.cloner && !isClient
				&& !(event.getTarget() instanceof EntityPlayer)) {
			NBTTagCompound compound = new NBTTagCompound();
			if (!(event.getTarget() instanceof EntityCustomNpc)
					|| !event.getTarget().writeToNBTAtomically(compound)) {
				CustomNpcs.debugData.endDebug("Server", event.getEntityPlayer(),
						"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			String s = compound.getString("id");
			if (s.equals("minecraft:customnpcs.customnpc") || s.equals("minecraft:customnpcs:customnpc")) {
				compound.setString("id", CustomNpcs.MODID + ":customnpc");
			}
			PlayerData data = PlayerData.get(event.getEntityPlayer());
			ServerCloneController.Instance.cleanTags(compound);
			if (!Server.sendDataChecked((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.CLONE, compound)) {
				event.getEntityPlayer().sendMessage(new TextComponentString("Entity too big to clone"));
			}
			data.cloned = compound;
			event.setCanceled(true);
		} else if (item.getItem() == CustomRegisters.scripter && !isClient && npcInteracted) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.NPC_GUI)) {
				CustomNpcs.debugData.endDebug("Server", event.getEntityPlayer(),
						"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			NoppesUtilServer.setEditingNpc(event.getEntityPlayer(), (EntityNPCInterface) event.getTarget());
			event.setCanceled(true);
			Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI,
					EnumGuiType.Script.ordinal(), 0, 0, 0);
		} else if (item.getItem() == CustomRegisters.mount) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.TOOL_MOUNTER)) {
				CustomNpcs.debugData.endDebug(!isClient ? "Server" : "Client", event.getEntityPlayer(),
						"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			event.setCanceled(true);
			ServerEventsHandler.mounted = event.getTarget();
			if (isClient) {
				CustomNpcs.proxy.openGui(MathHelper.floor(ServerEventsHandler.mounted.posX),
						MathHelper.floor(ServerEventsHandler.mounted.posY),
						MathHelper.floor(ServerEventsHandler.mounted.posZ), EnumGuiType.MobSpawnerMounter,
						event.getEntityPlayer());
			}
		} else if (item.getItem() == CustomRegisters.wand && event.getTarget() instanceof EntityVillager) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.EDIT_VILLAGER)) {
				CustomNpcs.debugData.endDebug(!isClient ? "Server" : "Client", event.getEntityPlayer(),
						"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			event.setCanceled(true);
			ServerEventsHandler.Merchant = (EntityVillager) event.getTarget();
			if (!isClient) {
				EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
				player.openGui(CustomNpcs.instance, EnumGuiType.MerchantAdd.ordinal(), player.world, 0, 0, 0);
				MerchantRecipeList merchantrecipelist = ServerEventsHandler.Merchant.getRecipes(player);
				if (merchantrecipelist != null) {
					Server.sendData(player, EnumPacketClient.VILLAGER_LIST, merchantrecipelist);
				}
			}
		}
		CustomNpcs.debugData.endDebug(!isClient ? "Server" : "Client", event.getEntityPlayer(), "ServerEventsHandler_npcPlayerInteract");
	}

	@SubscribeEvent
	public void npcPlayerTracking(PlayerEvent.StartTracking event) {
		if (!(event.getTarget() instanceof EntityLivingBase) || event.getTarget().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug(!event.getEntityPlayer().world.isRemote ? "Server" : "Client",
				event.getEntityPlayer(), "ServerEventsHandler_npcPlayerTracking");
		if (event.getTarget() instanceof EntityNPCInterface && CustomNpcs.EnableInvisibleNpcs) {
			CustomNpcs.visibilityController.checkIsVisible((EntityNPCInterface) event.getTarget(), (EntityPlayerMP) event.getEntityPlayer());
		}
		MarkData data = MarkData.get((EntityLivingBase) event.getTarget());
		if (data.marks.isEmpty()) {
			CustomNpcs.debugData.endDebug(!event.getEntityPlayer().world.isRemote ? "Server" : "Client",
					event.getEntityPlayer(), "ServerEventsHandler_npcPlayerTracking");
			return;
		}
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.MARK_DATA,
				event.getTarget().getEntityId(), data.getNBT());
		CustomNpcs.debugData.endDebug(!event.getEntityPlayer().world.isRemote ? "Server" : "Client",
				event.getEntityPlayer(), "ServerEventsHandler_npcPlayerTracking");
	}

	@SubscribeEvent
	public void npcPopulateChunk(PopulateChunkEvent.Post event) {
		CustomNpcs.debugData.startDebug(!event.getWorld().isRemote ? "Server" : "Client", null, "ServerEventsHandler_npcPopulateChunk");
		NPCSpawning.performWorldGenSpawning(event.getWorld(), event.getChunkX(), event.getChunkZ(), event.getRand());
		CustomNpcs.debugData.endDebug(!event.getWorld().isRemote ? "Server" : "Client", null, "ServerEventsHandler_npcPopulateChunk");
	}

	@SubscribeEvent
	public void npcSaveChunk(ChunkDataEvent.Save event) {
		CustomNpcs.debugData.startDebug(!event.getWorld().isRemote ? "Server" : "Client", null,
				"ServerEventsHandler_npcSaveChunk");
		for (ClassInheritanceMultiMap<Entity> map : event.getChunk().getEntityLists()) {
			for (Entity e : map) {
				if (e instanceof EntityLivingBase) {
					MarkData md = MarkData.get((EntityLivingBase) e);
					if (md.entity == null) { md.entity = (EntityLivingBase) e; }
					md.save();
				}
			}
		}
		CustomNpcs.debugData.endDebug(!event.getWorld().isRemote ? "Server" : "Client", null,
				"ServerEventsHandler_npcSaveChunk");
	}

	@SubscribeEvent
	public void npcSavePlayer(PlayerEvent.SaveToFile event) {
		CustomNpcs.debugData.startDebug(!event.getEntityPlayer().world.isRemote ? "Server" : "Client",
				event.getEntityPlayer(), "ServerEventsHandler_npcSavePlayer");
		PlayerData.get(event.getEntityPlayer()).save(false);
		CustomNpcs.debugData.endDebug(!event.getEntityPlayer().world.isRemote ? "Server" : "Client",
				event.getEntityPlayer(), "ServerEventsHandler_npcSavePlayer");
	}

	@SubscribeEvent
	public void registryRecipes(RegistryEvent.Register<IRecipe> event) {
		RecipeController.Registry = (ForgeRegistry<IRecipe>) event.getRegistry();
	}

	@SubscribeEvent
	public void worldUnload(net.minecraftforge.event.world.WorldEvent.Unload event) {
		int dimensionID = event.getWorld().provider.getDimension();
		if (!event.getWorld().isRemote) {
			DimensionHandler.getInstance().unload(event.getWorld(), dimensionID);
		}
	}

}
