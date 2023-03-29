package noppes.npcs;

import java.util.HashMap;
import java.util.List;
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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
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
import noppes.npcs.controllers.VisibilityController;
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

	private void doCraftQuest(ItemCraftedEvent event) { // Only Server
		EntityPlayer player = event.player;
		PlayerData pdata = PlayerData.get(player);
		PlayerQuestData playerdata = pdata.questData;
		for (QuestData data : playerdata.activeQuests.values()) {
			if (data.quest.step == 2 && data.quest.questInterface.isCompleted(player)) {
				continue;
			}
			for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
				if (obj.getType() != EnumQuestTask.CRAFT.ordinal()) {
					continue;
				}
				int size = 0;
				if (!NoppesUtilServer.IsItemStackNull(event.crafting) && NoppesUtilPlayer.compareItems(
						obj.getItem().getMCItemStack(), event.crafting, obj.isIgnoreDamage(), obj.isItemIgnoreNBT())) {
					size = event.crafting.getCount();
				}
				if (size == 0) {
					continue;
				}
				HashMap<ItemStack, Integer> crafted = ((QuestObjective) obj).getCrafted(data);
				int amount = 0;
				ItemStack key = obj.getItem().getMCItemStack();
				for (ItemStack inData : crafted.keySet()) {
					if (NoppesUtilPlayer.compareItems(obj.getItem().getMCItemStack(), inData, obj.isIgnoreDamage(),
							obj.isItemIgnoreNBT())) {
						amount = crafted.get(inData);
						key = inData;
						break;
					}
				}
				if (amount >= obj.getMaxProgress()) {
					continue;
				}
				if (amount + size > obj.getMaxProgress()) {
					size = obj.getMaxProgress() - amount;
				}
				amount += size;
				crafted.put(key, amount);
				((QuestObjective) obj).setCrafted(data, crafted);

				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("QuestID", data.quest.id);
				compound.setString("Type", "craft");
				compound.setIntArray("Progress", new int[] { amount, obj.getMaxProgress() });
				compound.setTag("Item", event.crafting.writeToNBT(new NBTTagCompound()));
				compound.setInteger("MessageType", 0);
				Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE_DATA, compound);
				if (amount >= obj.getMaxProgress()) {
					player.sendMessage(new TextComponentTranslation("quest.message.craft.1", event.crafting.getDisplayName(),
							data.quest.getTitle()));
				} else {
					player.sendMessage(new TextComponentTranslation("quest.message.craft.0", event.crafting.getDisplayName(),
							"" + amount, "" + obj.getMaxProgress(), data.quest.getTitle()));
				}

				pdata.updateClient = true;
				if (obj.isItemLeave()) {
					boolean ch = player.inventory.getItemStack().isItemEqual(event.crafting);
					event.crafting.splitStack(size);
					player.openContainer.detectAndSendChanges();
					if (ch) {
						NBTTagCompound nbtStack = new NBTTagCompound();
						player.inventory.getItemStack().writeToNBT(nbtStack);
						Server.sendData((EntityPlayerMP) player, EnumPacketClient.DETECT_HELD_ITEM, nbtStack);
					}
				}
				playerdata.checkQuestCompletion(player, data);
			}
		}
	}

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
			for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
				if (((QuestObjective) obj).getEnumType() != EnumQuestTask.KILL
						&& ((QuestObjective) obj).getEnumType() != EnumQuestTask.AREAKILL) {
					continue;
				}
				String name = entityName;
				if (obj.getTargetName().equals(entity.getName())) {
					name = entity.getName();
				} else {
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
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("QuestID", data.quest.id);
				compound.setString("Type", "kill");
				compound.setIntArray("Progress", new int[] { amount, obj.getMaxProgress() });
				compound.setString("TargetName", obj.getTargetName());
				compound.setInteger("MessageType", 0);
				Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE_DATA, compound);
				if (amount >= obj.getMaxProgress()) {
					player.sendMessage(new TextComponentTranslation("quest.message.kill.1",
							new TextComponentTranslation(name).getFormattedText(), data.quest.getTitle()));
				} else {
					player.sendMessage(new TextComponentTranslation("quest.message.kill.0",
							new TextComponentTranslation(name).getFormattedText(), "" + amount,
							"" + obj.getMaxProgress(), data.quest.getTitle()));
				}

				playerdata.checkQuestCompletion(player, data);
				pdata.updateClient = true;
			}
		}
	}

	@SubscribeEvent
	public void npcCommands(CommandEvent event) { // Changed
		CustomNpcs.debugData.startDebug(event.getSender().getEntityWorld().isRemote ? "Server" : "Client", event.getSender(), "ServerEventsHandler_npcCommands");
		if (event.getCommand() instanceof CommandGive) {
			if (!(event.getSender().getEntityWorld() instanceof WorldServer)) {
				CustomNpcs.debugData.endDebug(event.getSender().getEntityWorld().isRemote ? "Server" : "Client",
							event.getSender(), "ServerEventsHandler_npcCommands");
				return;
			}
			try {
				EntityPlayer player = (EntityPlayer) CommandBase.getPlayer(event.getSender().getServer(),
						event.getSender(), event.getParameters()[0]);
				player.getServer().futureTaskQueue.add(ListenableFutureTask.create(Executors.callable(() -> {
					PlayerQuestData playerdata = PlayerData.get(player).questData;
					for (QuestData data : playerdata.activeQuests.values()) { // Changed
						for (IQuestObjective obj : data.quest
								.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
							if (obj.getType() != EnumQuestTask.ITEM.ordinal()) {
								continue;
							}
							playerdata.checkQuestCompletion(player, data);
						}
					}
				})));
			} catch (Throwable t) {
			}
		} else if (event.getCommand() instanceof CommandTime) {
			try {
				List<EntityPlayerMP> players = (List<EntityPlayerMP>) FMLCommonHandler.instance()
						.getMinecraftServerInstance().getPlayerList().getPlayers();
				for (EntityPlayerMP playerMP : players) {
					VisibilityController.onUpdate(playerMP);
				}
			} catch (Throwable t) {
			}
		}
		CustomNpcs.debugData.endDebug(event.getSender().getEntityWorld().isRemote ? "Server" : "Client",
					event.getSender(), "ServerEventsHandler_npcCommands");
	}

	// New
	@SubscribeEvent
	public void npcCraftedItem(ItemCraftedEvent event) {
		if (event.player.world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug(event.player.world.isRemote ? "Server" : "Client", event.player,
					"ServerEventsHandler_npcCraftedItem");
		if (event.crafting != null && !event.crafting.isEmpty()) {
			this.doCraftQuest(event);
		}
		CustomNpcs.debugData.endDebug(event.player.world.isRemote ? "Server" : "Client", event.player,
					"ServerEventsHandler_npcCraftedItem");
	}

	@SubscribeEvent
	public void npcDeath(LivingDeathEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug(event.getEntityLiving().world.isRemote ? "Server" : "Client",
					event.getEntityLiving(), "ServerEventsHandler_npcDeath");
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
		CustomNpcs.debugData.endDebug(event.getEntityLiving().world.isRemote ? "Server" : "Client",
					event.getEntityLiving(), "ServerEventsHandler_npcDeath");
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void npcEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		String side = "Common";
		if (event.getObject()!=null && event.getObject().world!=null) {
			side = (event.getObject()).world.isRemote ? "Server" : "Client";
		}
		CustomNpcs.debugData.startDebug(side, event.getObject(), "ServerEventsHandler_npcEntityCapabilities");
		if (event.getObject() instanceof EntityPlayer) {
			PlayerData.register(event);
		}
		if (event.getObject() instanceof EntityLivingBase) {
			MarkData.register(event);
		}
		if ((event.getObject()).world!=null && !(event.getObject()).world.isRemote && (event.getObject()).world instanceof WorldServer) {
			WrapperEntityData.register(event);
		}
		CustomNpcs.debugData.endDebug(side, event.getObject(), "ServerEventsHandler_npcEntityCapabilities");
	}

	@SubscribeEvent
	public void npcEntityJoin(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityPlayer)) {
			return;
		}
		CustomNpcs.debugData.startDebug(event.getEntity().world.isRemote ? "Server" : "Client", event.getEntity(),
					"ServerEventsHandler_npcEntityJoin");
		PlayerData data = PlayerData.get((EntityPlayer) event.getEntity());
		data.updateCompanion(event.getWorld());
		CustomNpcs.debugData.endDebug(event.getEntity().world.isRemote ? "Server" : "Client", event.getEntity(),
					"ServerEventsHandler_npcEntityJoin");
	}

	@SubscribeEvent
	public void npcItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		CustomNpcs.debugData.startDebug("Common", null, "ServerEventsHandler_npcEntityCapabilities");
		ItemStackWrapper.register(event);
		CustomNpcs.debugData.endDebug("Common", null, "ServerEventsHandler_npcEntityCapabilities");
	}

	@SubscribeEvent
	public void npcPlayerInteract(PlayerInteractEvent.EntityInteract event) {
		ItemStack item = event.getEntityPlayer().getHeldItemMainhand();
		if (item == null) {
			return;
		}
		boolean isRemote = event.getEntityPlayer().world.isRemote;
		CustomNpcs.debugData.startDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(),
					"ServerEventsHandler_npcPlayerInteract");
		boolean npcInteracted = event.getTarget() instanceof EntityNPCInterface;
		if (!isRemote && CustomNpcs.OpsOnly && !event.getEntityPlayer().getServer().getPlayerList()
				.canSendCommands(event.getEntityPlayer().getGameProfile())) {
			CustomNpcs.debugData.endDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(),
						"ServerEventsHandler_npcPlayerInteract");
			return;
		}
		if (!isRemote && item.getItem() == CustomItems.soulstoneEmpty
				&& event.getTarget() instanceof EntityLivingBase) {
			((ItemSoulstoneEmpty) item.getItem()).store((EntityLivingBase) event.getTarget(), item,
					event.getEntityPlayer());
		}
		if (item.getItem() == CustomItems.wand && npcInteracted && !isRemote) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.NPC_GUI)) {
				CustomNpcs.debugData.endDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(),
							"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			event.setCanceled(true);
			NoppesUtilServer.sendOpenGui(event.getEntityPlayer(), EnumGuiType.MainMenuDisplay,
					(EntityNPCInterface) event.getTarget());
		} else if (item.getItem() == CustomItems.cloner && !isRemote && !(event.getTarget() instanceof EntityPlayer)) {
			NBTTagCompound compound = new NBTTagCompound();
			if (!((EntityCustomNpc) event.getTarget()).writeToNBTAtomically(compound)) {
				CustomNpcs.debugData.endDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(), "ServerEventsHandler_npcPlayerInteract");
				return;
			}
			String s = compound.getString("id");
			if (s.equals("minecraft:" + CustomNpcs.MODID + ".customnpc") || s.equals("minecraft:" + CustomNpcs.MODID + ":customnpc")) {
				compound.setString("id", CustomNpcs.MODID + ":customnpc");
			}
			PlayerData data = PlayerData.get(event.getEntityPlayer());
			ServerCloneController.Instance.cleanTags(compound);
			if (!Server.sendDataChecked((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.CLONE, compound)) {
				event.getEntityPlayer().sendMessage(new TextComponentString("Entity too big to clone"));
			}
			data.cloned = compound;
			event.setCanceled(true);
		} else if (item.getItem() == CustomItems.scripter && !isRemote && npcInteracted) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.NPC_GUI)) {
				CustomNpcs.debugData.endDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(),
							"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			NoppesUtilServer.setEditingNpc(event.getEntityPlayer(), (EntityNPCInterface) event.getTarget());
			event.setCanceled(true);
			Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI,
					EnumGuiType.Script.ordinal(), 0, 0, 0);
		} else if (item.getItem() == CustomItems.mount) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.TOOL_MOUNTER)) {
				CustomNpcs.debugData.endDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(),
							"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			event.setCanceled(true);
			ServerEventsHandler.mounted = event.getTarget();
			if (isRemote) {
				CustomNpcs.proxy.openGui(MathHelper.floor(ServerEventsHandler.mounted.posX),
						MathHelper.floor(ServerEventsHandler.mounted.posY),
						MathHelper.floor(ServerEventsHandler.mounted.posZ), EnumGuiType.MobSpawnerMounter,
						event.getEntityPlayer());
			}
		} else if (item.getItem() == CustomItems.wand && event.getTarget() instanceof EntityVillager) {
			if (!CustomNpcsPermissions.hasPermission(event.getEntityPlayer(), CustomNpcsPermissions.EDIT_VILLAGER)) {
				CustomNpcs.debugData.endDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(),
							"ServerEventsHandler_npcPlayerInteract");
				return;
			}
			event.setCanceled(true);
			ServerEventsHandler.Merchant = (EntityVillager) event.getTarget();
			if (!isRemote) {
				EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
				player.openGui(CustomNpcs.instance, EnumGuiType.MerchantAdd.ordinal(), player.world, 0, 0, 0);
				MerchantRecipeList merchantrecipelist = ServerEventsHandler.Merchant.getRecipes((EntityPlayer) player);
				if (merchantrecipelist != null) {
					Server.sendData(player, EnumPacketClient.VILLAGER_LIST, merchantrecipelist);
				}
			}
		}
		CustomNpcs.debugData.endDebug(isRemote ? "Server" : "Client", event.getEntityPlayer(), "ServerEventsHandler_npcPlayerInteract");
	}

	@SubscribeEvent
	public void npcPlayerStopTracking(PlayerEvent.StopTracking event) {
		CustomNpcs.debugData.startDebug(event.getEntityPlayer().world.isRemote ? "Server" : "Client", event.getEntityPlayer(), "ServerEventsHandler_npcPlayerStopTracking");
		CustomNpcs.debugData.endDebug(event.getEntityPlayer().world.isRemote ? "Server" : "Client", event.getEntityPlayer(), "ServerEventsHandler_npcPlayerStopTracking");
	}

	@SubscribeEvent
	public void npcPlayerTracking(PlayerEvent.StartTracking event) {
		if (!(event.getTarget() instanceof EntityLivingBase) || event.getTarget().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug(event.getEntityPlayer().world.isRemote ? "Server" : "Client", event.getEntityPlayer(), "ServerEventsHandler_npcPlayerTracking");
		if (event.getTarget() instanceof EntityNPCInterface && CustomNpcs.EnableInvisibleNpcs) {
			VisibilityController.checkIsVisible((EntityNPCInterface) event.getTarget(), (EntityPlayerMP) event.getEntityPlayer());
		}
		MarkData data = MarkData.get((EntityLivingBase) event.getTarget());
		CustomNpcs.debugData.endDebug(event.getEntityPlayer().world.isRemote ? "Server" : "Client", event.getEntityPlayer(), "ServerEventsHandler_npcPlayerTracking");
		if (data.marks.isEmpty()) { return; }
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.MARK_DATA, event.getTarget().getEntityId(), data.getNBT());
	}

	@SubscribeEvent
	public void npcPopulateChunk(PopulateChunkEvent.Post event) {
		CustomNpcs.debugData.startDebug(event.getWorld().isRemote ? "Server" : "Client", null, "ServerEventsHandler_npcPopulateChunk");
		NPCSpawning.performWorldGenSpawning(event.getWorld(), event.getChunkX(), event.getChunkZ(), event.getRand());
		CustomNpcs.debugData.endDebug(event.getWorld().isRemote ? "Server" : "Client", null, "ServerEventsHandler_npcPopulateChunk");
	}

	@SubscribeEvent
	public void npcSaveChunk(ChunkDataEvent.Save event) {
		CustomNpcs.debugData.startDebug(event.getWorld().isRemote ? "Server" : "Client", null, "ServerEventsHandler_npcSaveChunk");
		for (ClassInheritanceMultiMap<Entity> map : event.getChunk().getEntityLists()) {
			for (Entity e : map) {
				if (e instanceof EntityLivingBase) {
					MarkData.get((EntityLivingBase) e).save();
				}
			}
		}
		CustomNpcs.debugData.endDebug(event.getWorld().isRemote ? "Server" : "Client", null, "ServerEventsHandler_npcSaveChunk");
	}

	@SubscribeEvent
	public void npcSavePlayer(PlayerEvent.SaveToFile event) {
		CustomNpcs.debugData.startDebug(event.getEntityPlayer().world.isRemote ? "Server" : "Client",
					event.getEntityPlayer(), "ServerEventsHandler_npcSavePlayer");
		PlayerData.get(event.getEntityPlayer()).save(false);
		CustomNpcs.debugData.endDebug(event.getEntityPlayer().world.isRemote ? "Server" : "Client",
					event.getEntityPlayer(), "ServerEventsHandler_npcSavePlayer");
	}

	@SubscribeEvent
	public void registryItems(RegistryEvent.Register<IRecipe> event) {
		RecipeController.Registry = (ForgeRegistry<IRecipe>) event.getRegistry();
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {
		int dimensionID = event.getWorld().provider.getDimension();
		if (!event.getWorld().isRemote) {
			DimensionHandler.getInstance().unload(event.getWorld(), dimensionID);
		}
	}
	
}
