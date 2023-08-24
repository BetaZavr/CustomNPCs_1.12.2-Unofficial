package noppes.npcs;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.HandlerEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.NpcEvent.CustomNpcTeleport;
import noppes.npcs.api.event.PackageReceived;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.PlayerEvent.CustomTeleport;
import noppes.npcs.api.event.PlayerEvent.PlayerPackage;
import noppes.npcs.api.event.PlayerEvent.PlayerSound;
import noppes.npcs.api.event.ProjectileEvent;
import noppes.npcs.api.event.QuestEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.event.WorldEvent.ScriptTriggerEvent;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.data.IKeySetting;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.api.wrapper.ContainerCustomChestWrapper;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.IScriptBlockHandler;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.ClientScriptData;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.controllers.data.PotionScriptData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.entity.data.DataScript;

public class EventHooks {
	
	private static Map<String, Long> clientMap = Maps.<String, Long>newHashMap();

	public static void onCustomChestClicked(CustomContainerEvent.SlotClickedEvent event) {
		ContainerCustomChestWrapper container = (ContainerCustomChestWrapper) event.container;
		if (!container.script.isValid()) { return; }
		EventHooks.onEvent(container.script, EnumScriptType.CUSTOM_CHEST_CLICKED, event);
	}

	public static void onCustomChestClosed(CustomContainerEvent.CloseEvent event) {
		ContainerCustomChestWrapper container = (ContainerCustomChestWrapper) event.container;
		if (!container.script.isValid()) { return; }
		EventHooks.onEvent(container.script, EnumScriptType.CUSTOM_CHEST_CLOSED, event);
	}

	public static void onCustomGuiButton(PlayerWrapper<?> player, ICustomGui gui, int buttonId) {
		CustomGuiController.onButton(new CustomGuiEvent.ButtonEvent(player, gui, buttonId));
	}

	public static void onCustomGuiClose(PlayerWrapper<?> player, ICustomGui gui) {
		CustomGuiController.onClose(new CustomGuiEvent.CloseEvent(player, gui));
	}

	public static void onCustomGuiScrollClick(PlayerWrapper<?> player, ICustomGui gui, int scrollId, int scrollIndex, String[] selection, boolean doubleClick) {
		CustomGuiController.onScrollClick(new CustomGuiEvent.ScrollEvent(player, gui, scrollId, scrollIndex, selection, doubleClick));
	}

	public static void onCustomGuiSlot(PlayerWrapper<?> player, ICustomGui gui, int slotId, IItemStack stack, IItemStack heldItem) {
		CustomGuiController.onSlotChange(new CustomGuiEvent.SlotEvent(player, gui, slotId, stack, heldItem));
	}

	public static boolean onCustomGuiSlotClicked(PlayerWrapper<?> player, ICustomGui gui, int slotId, int dragType, String clickType, IItemStack heldItem) {
		return CustomGuiController.onSlotClick(new CustomGuiEvent.SlotClickEvent(player, gui, slotId, player.getOpenContainer().getSlot(slotId), dragType, clickType, heldItem));
	}

	public static void onForgeEvent(ForgeEvent event) {
		ForgeScriptData handler = ScriptController.Instance.forgeScripts;
		String eventName;
		if (!handler.isClient() && handler.isEnabled() && CustomNpcs.forgeEventNames.containsKey(event.event.getClass())) {
			eventName = CustomNpcs.forgeEventNames.get(event.event.getClass());
			try {
				handler.runScript(eventName, event);
				if (event.isCanceled() && event.event.isCancelable()) { event.event.setCanceled(true); }
				WrapperNpcAPI.EVENT_BUS.post(event.event);
				if (event.isCancelable()) { event.setCanceled(event.event.isCanceled()); }
			}
			catch (Exception e) { }
		}
		
		if (handler.isClient()) {
			ClientScriptData handlerClient = ScriptController.Instance.clientScripts;
			if (!handlerClient.isClient()) { return; }
			if (handlerClient.isEnabled()) {
				if (!CustomNpcs.forgeClientEventNames.containsKey(event.event.getClass())) {
					eventName = event.getClass().getName();
					int i = eventName.lastIndexOf(".");
					eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
					CustomNpcs.forgeClientEventNames.put(event.event.getClass(), eventName);
				} else {
					eventName = CustomNpcs.forgeClientEventNames.get(event.event.getClass());
				}
				if (eventName.isEmpty() || (EventHooks.clientMap.containsKey(eventName) && EventHooks.clientMap.get(eventName)==System.currentTimeMillis())) { return; }
				EventHooks.clientMap.put(eventName, System.currentTimeMillis());
				try {
					/*List<String> falseList = Lists.<String>newArrayList();
					falseList.add("tickEventPlayerTickEvent");
					falseList.add("playerSPPushOutOfBlocksEvent");
					List<String> trueList = Lists.<String>newArrayList();
					if (trueList.contains(eventName) || eventName.toLowerCase().indexOf("init")!=-1 || (eventName.toLowerCase().indexOf("player")!=-1 && !falseList.contains(eventName))) {
						System.out.println("Run Client Event: \""+eventName+"\"");
					}*/
					handlerClient.runScript(eventName, event);
					if (event.isCanceled() && event.event.isCancelable()) { event.event.setCanceled(true); }
					WrapperNpcAPI.EVENT_BUS.post(event.event);
				}
				catch (Exception e) { }
			}
			return;
		}
	}

	public static void onForgeInit(ForgeScriptData handler) {
		EventHooks.onEvent(handler, EnumScriptType.INIT, new ForgeEvent.InitEvent());
	}

	public static void onGlobalFactionsLoaded(IFactionHandler handler) {
		HandlerEvent.FactionsLoadedEvent event = new HandlerEvent.FactionsLoadedEvent(handler);
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static boolean onNPCAttacksMelee(EntityNPCInterface npc, NpcEvent.MeleeAttackEvent event) {
		if (npc.script.isClient()) { return false; }
		return EventHooks.onEvent(npc.script, EnumScriptType.ATTACK_MELEE, event);
	}

	public static void onNPCCollide(EntityNPCInterface npc, Entity entity) {
		if (npc.script.isClient()) { return; }
		EventHooks.onEvent(npc.script, EnumScriptType.COLLIDE, new NpcEvent.CollideEvent(npc.wrappedNPC, entity));
	}

	public static boolean onNPCDamaged(EntityNPCInterface npc, NpcEvent.DamagedEvent event) {
		if (npc.script.isClient()) { return false; }
		event.setCanceled(npc.isKilled());
		return EventHooks.onEvent(npc.script, EnumScriptType.DAMAGED, event);
	}

	public static boolean onNPCDialog(EntityNPCInterface npc, EntityPlayer player, Dialog dialog) {
		if (npc.script.isClient()) { return false; }
		DialogEvent.OpenEvent event = new DialogEvent.OpenEvent(npc.wrappedNPC, player, dialog);
		if (!(npc instanceof EntityDialogNpc)) {
			EventHooks.onEvent(npc.script, EnumScriptType.DIALOG, event);
		}
		return EventHooks.onEvent(PlayerData.get(player).scriptData, EnumScriptType.DIALOG, event);
	}

	public static void onNPCDialogClose(EntityNPCInterface npc, EntityPlayerMP player, Dialog dialog) {
		if (npc.script.isClient()) { return; }
		DialogEvent.CloseEvent event = new DialogEvent.CloseEvent(npc.wrappedNPC, (EntityPlayer) player, dialog);
		if (!(npc instanceof EntityDialogNpc)) {
			EventHooks.onEvent(npc.script, EnumScriptType.DIALOG, event);
		}
		EventHooks.onEvent(PlayerData.get(player).scriptData, EnumScriptType.DIALOG_CLOSE, event);
	}

	public static boolean onNPCDialogOption(EntityNPCInterface npc, EntityPlayerMP player, Dialog dialog, DialogOption option) {
		if (npc.script.isClient()) { return false; }
		DialogEvent.OptionEvent event = new DialogEvent.OptionEvent(npc.wrappedNPC, (EntityPlayer) player, dialog, option);
		if (!(npc instanceof EntityDialogNpc)) {
			EventHooks.onEvent(npc.script, EnumScriptType.DIALOG_OPTION, event);
		}
		return EventHooks.onEvent(PlayerData.get(player).scriptData, EnumScriptType.DIALOG_OPTION, event);
	}

	public static void onNPCDied(EntityNPCInterface npc, NpcEvent.DiedEvent event) {
		if (npc.script.isClient()) { return; }
		EventHooks.onEvent(npc.script, EnumScriptType.DIED, event);
	}

	public static void onNPCInit(EntityNPCInterface npc) {
		if (npc.script.isClient()) { return; }
		EventHooks.onEvent(npc.script, EnumScriptType.INIT, new NpcEvent.InitEvent(npc.wrappedNPC));
	}

	public static boolean onNPCInteract(EntityNPCInterface npc, EntityPlayer player) {
		if (npc.script.isClient()) { return false; }
		NpcEvent.InteractEvent event = new NpcEvent.InteractEvent(npc.wrappedNPC, player);
		event.setCanceled(npc.isAttacking() || npc.isKilled() || npc.faction.isAggressiveToPlayer(player));
		return EventHooks.onEvent(npc.script, EnumScriptType.INTERACT, event);
	}

	public static void onNPCKills(EntityNPCInterface npc, EntityLivingBase entityLiving) {
		if (npc.script.isClient()) { return; }
		EventHooks.onEvent(npc.script, EnumScriptType.KILL, new NpcEvent.KilledEntityEvent(npc.wrappedNPC, entityLiving));
	}

	public static void onNPCRangedLaunched(EntityNPCInterface npc, NpcEvent.RangedLaunchedEvent event) {
		if (npc.script.isClient()) { return; }
		EventHooks.onEvent(npc.script, EnumScriptType.RANGED_LAUNCHED, event);
	}

	public static boolean onNPCRole(EntityNPCInterface npc, RoleEvent event) {
		if (npc.script.isClient()) { return false; }
		return EventHooks.onEvent(npc.script, EnumScriptType.ROLE, event);
	}

	public static boolean onNPCTarget(EntityNPCInterface npc, NpcEvent.TargetEvent event) {
		if (npc.script.isClient()) { return false; }
		return EventHooks.onEvent(npc.script, EnumScriptType.TARGET, event);
	}

	public static boolean onNPCTargetLost(EntityNPCInterface npc, EntityLivingBase prevtarget) {
		if (npc.script.isClient()) { return false; }
		return EventHooks.onEvent(npc.script, EnumScriptType.TARGET_LOST, new NpcEvent.TargetLostEvent(npc.wrappedNPC, prevtarget));
	}

	public static void onNPCTick(EntityNPCInterface npc) {
		if (npc.script.isClient()) {
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.TICK, new NpcEvent.UpdateEvent(npc.wrappedNPC));
			return;
		}
		EventHooks.onEvent(npc.script, EnumScriptType.TICK, new NpcEvent.UpdateEvent(npc.wrappedNPC));
	}

	public static void onNPCTimer(EntityNPCInterface npc, int id) {
		EventHooks.onEvent(npc.script, EnumScriptType.TIMER, new NpcEvent.TimerEvent(npc.wrappedNPC, id));
	}

	public static boolean onPlayerAttack(PlayerScriptData handler, PlayerEvent.AttackEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.ATTACK, event);
	}

	public static boolean onPlayerPlace(PlayerScriptData handler, PlayerEvent.PlaceEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.PLASED, event);
	}

	public static boolean onPlayerBreak(PlayerScriptData handler, PlayerEvent.BreakEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.BROKEN, event);
	}

	public static void onPlayerChat(PlayerScriptData handler, PlayerEvent.ChatEvent event) {
		EventHooks.onEvent(handler, EnumScriptType.CHAT, event);
	}

	public static void onPlayerContainerClose(PlayerScriptData handler, Container container) {
		EventHooks.onEvent(handler, EnumScriptType.CONTAINER_CLOSED, new PlayerEvent.ContainerClosed(handler.getPlayer(), NpcAPI.Instance().getIContainer(container)));
	}

	public static void onPlayerContainerOpen(PlayerScriptData handler, Container container) {
		EventHooks.onEvent(handler, EnumScriptType.CONTAINER_OPEN, new PlayerEvent.ContainerOpen(handler.getPlayer(), NpcAPI.Instance().getIContainer(container)));
	}

	public static void onPlayerCrafted(PlayerScriptData handler, ItemStack crafting, IInventory craftMatrix) {
		EventHooks.onEvent(handler, EnumScriptType.ITEM_CRAFTED, new PlayerEvent.ItemCrafted(handler.getPlayer(), NpcAPI.Instance().getIItemStack(crafting), craftMatrix));
	}

	public static boolean onPlayerDamaged(PlayerScriptData handler, PlayerEvent.DamagedEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.DAMAGED, event);
	}

	public static boolean onPlayerDamagedEntity(PlayerScriptData handler, PlayerEvent.DamagedEntityEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.DAMAGED_ENTITY, event);
	}

	public static void onPlayerDeath(PlayerScriptData handler, DamageSource source, Entity entity) {
		EventHooks.onEvent(handler, EnumScriptType.DIED, new PlayerEvent.DiedEvent(handler.getPlayer(), source, entity));
	}

	public static void OnPlayerFactionChange(PlayerScriptData handler, PlayerEvent.FactionUpdateEvent event) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.FACTION_UPDATE, event);
	}

	public static boolean onPlayerFished(PlayerScriptData handler, NonNullList<ItemStack> drops, int rodDamage) {
		return EventHooks.onEvent(handler, EnumScriptType.ITEM_FISHED, new PlayerEvent.ItemFished(handler.getPlayer(), drops, rodDamage));
	}

	public static void onPlayerInit(PlayerScriptData handler) {
		EventHooks.onEvent(handler, EnumScriptType.INIT, new PlayerEvent.InitEvent(handler.getPlayer()));
	}

	public static void onPotoinInit(PotionScriptData handler) {
		EventHooks.onEvent(handler, EnumScriptType.INIT, new ForgeEvent.InitEvent());
	}

	public static boolean onPlayerInteract(PlayerScriptData handler, PlayerEvent.InteractEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.INTERACT, event);
	}
	
	public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isDown, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed) {
		PlayerScriptData handler = PlayerData.get((EntityPlayer) player).scriptData;
		Event event = new PlayerEvent.KeyPressedEvent(handler.getPlayer(), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed);
		EventHooks.onEvent(handler, isDown ? EnumScriptType.KEY_DOWN : EnumScriptType.KEY_UP, event);
	}
	
	public static void onPlayerMousePressed(EntityPlayerMP player, int button, boolean isDown, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed) {
		PlayerScriptData handler = PlayerData.get((EntityPlayer) player).scriptData;
		Event event = new PlayerEvent.KeyPressedEvent(handler.getPlayer(), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed);
		EventHooks.onEvent(handler, isDown ? EnumScriptType.MOUSE_DOWN : EnumScriptType.MOUSE_UP, event);
	}

	public static void onPlayerKills(PlayerScriptData handler, EntityLivingBase entityLiving) {
		EventHooks.onEvent(handler, EnumScriptType.KILL, new PlayerEvent.KilledEntityEvent(handler.getPlayer(), entityLiving));
	}

	public static void onPlayerLevelUp(PlayerScriptData handler, int change) {
		EventHooks.onEvent(handler, EnumScriptType.LEVEL_UP, new PlayerEvent.LevelUpEvent(handler.getPlayer(), change));
	}

	public static void onPlayerLogin(PlayerScriptData handler) {
		EventHooks.onEvent(handler, EnumScriptType.LOGIN, new PlayerEvent.LoginEvent(handler.getPlayer()));
	}

	public static void onPlayerLogout(PlayerScriptData handler) {
		EventHooks.onEvent(handler, EnumScriptType.LOGOUT, new PlayerEvent.LogoutEvent(handler.getPlayer()));
	}

	public static boolean onPlayerPickUp(PlayerScriptData handler, EntityItem entityItem) {
		return EventHooks.onEvent(handler, EnumScriptType.PICKUP, new PlayerEvent.PickUpEvent(handler.getPlayer(), NpcAPI.Instance().getIItemStack(entityItem.getItem())));
	}

	public static boolean onPlayerRanged(PlayerScriptData handler, PlayerEvent.RangedLaunchedEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.RANGED_LAUNCHED, event);
	}

	public static void onPlayerTick(PlayerScriptData handler) {
		if (handler.isClient()) {
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.TICK, new PlayerEvent.UpdateEvent(handler.getPlayer()));
			return;
		}
		EventHooks.onEvent(handler, EnumScriptType.TICK, new PlayerEvent.UpdateEvent(handler.getPlayer()));
	}

	public static void onPlayerTimer(PlayerData data, int id) {
		EventHooks.onEvent(data.scriptData, EnumScriptType.TIMER, new PlayerEvent.TimerEvent(data.scriptData.getPlayer(), id));
	}

	public static boolean onPlayerToss(PlayerScriptData handler, EntityItem entityItem) {
		return EventHooks.onEvent(handler, EnumScriptType.TOSS, new PlayerEvent.TossEvent(handler.getPlayer(), NpcAPI.Instance().getIItemStack(entityItem.getItem())));
	}

	public static void onProjectileImpact(EntityProjectile projectile, ProjectileEvent.ImpactEvent event) {
		for (ScriptContainer script : projectile.scripts) {
			if (script.isValid()) {
				script.run(EnumScriptType.PROJECTILE_IMPACT.function, event, projectile!=null && !projectile.world.isRemote);
			}
		}
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static void onProjectileTick(EntityProjectile projectile) {
		ProjectileEvent.UpdateEvent event = new ProjectileEvent.UpdateEvent((IProjectile<?>) NpcAPI.Instance().getIEntity(projectile));
		for (ScriptContainer script : projectile.scripts) {
			if (script.isValid()) {
				script.run(EnumScriptType.PROJECTILE_TICK.function, event, projectile!=null && !projectile.world.isRemote);
			}
		}
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static boolean onQuestCanceled(PlayerScriptData handler, Quest quest) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.QUEST_CANCELED, new QuestEvent.QuestCanceledEvent(handler.getPlayer(), quest));
	}

	public static void onQuestFinished(PlayerScriptData handler, Quest quest) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.QUEST_COMPLETED, new QuestEvent.QuestCompletedEvent(handler.getPlayer(), (IQuest) quest));
	}

	public static boolean onQuestStarted(PlayerScriptData handler, Quest quest) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.QUEST_START, new QuestEvent.QuestStartEvent(handler.getPlayer(), (IQuest) quest));
	}

	public static void onQuestTurnedIn(PlayerScriptData handler, QuestEvent.QuestTurnedInEvent event) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.QUEST_TURNIN, event);
	}

	public static void onRecipesLoaded(IRecipeHandler handler) {
		HandlerEvent.RecipesLoadedEvent event = new HandlerEvent.RecipesLoadedEvent(handler);
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static void onScriptBlockBreak(IScriptBlockHandler handler) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.BROKEN, new BlockEvent.BreakEvent(handler.getBlock()));
	}

	public static void onScriptBlockClicked(IScriptBlockHandler handler, EntityPlayer player) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.CLICKED, new BlockEvent.ClickedEvent(handler.getBlock(), player));
	}

	public static void onScriptBlockCollide(IScriptBlockHandler handler, Entity entityIn) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.COLLIDE, new BlockEvent.CollidedEvent(handler.getBlock(), entityIn));
	}

	public static boolean onScriptBlockDoorToggle(IScriptBlockHandler handler) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.DOOR_TOGGLE, new BlockEvent.DoorToggleEvent(handler.getBlock()));
	}

	public static boolean onScriptBlockExploded(IScriptBlockHandler handler) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.EXPLODED, new BlockEvent.ExplodedEvent(handler.getBlock()));
	}

	public static float onScriptBlockFallenUpon(IScriptBlockHandler handler, Entity entity, float distance) {
		if (handler.isClient()) { return distance; }
		BlockEvent.EntityFallenUponEvent event = new BlockEvent.EntityFallenUponEvent(handler.getBlock(), entity, distance);
		if (EventHooks.onEvent(handler, EnumScriptType.FALLEN_UPON, event)) { return 0.0f; }
		return event.distanceFallen;
	}

	public static boolean onScriptBlockHarvest(IScriptBlockHandler handler, EntityPlayer player) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.HARVESTED, new BlockEvent.HarvestedEvent(handler.getBlock(), player));
	}

	public static void onScriptBlockInit(IScriptBlockHandler handler) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.INIT, new BlockEvent.InitEvent(handler.getBlock()));
	}

	public static boolean onScriptBlockInteract(IScriptBlockHandler handler, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.INTERACT, new BlockEvent.InteractEvent(handler.getBlock(), player, side, hitX, hitY, hitZ));
	}

	public static void onScriptBlockNeighborChanged(IScriptBlockHandler handler, BlockPos changedPos) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.NEIGHBOR_CHANGED, new BlockEvent.NeighborChangedEvent(handler.getBlock(), new BlockPosWrapper(changedPos)));
	}

	public static void onScriptBlockRainFill(IScriptBlockHandler handler) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.RAIN_FILLED, new BlockEvent.RainFillEvent(handler.getBlock()));
	}

	public static void onScriptBlockRedstonePower(IScriptBlockHandler handler, int prevPower, int power) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.REDSTONE, new BlockEvent.RedstoneEvent(handler.getBlock(), prevPower, power));
	}

	public static void onScriptBlockTimer(IScriptBlockHandler handler, int id) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.TIMER, new BlockEvent.TimerEvent(handler.getBlock(), id));
	}

	public static void onScriptBlockUpdate(IScriptBlockHandler handler) {
		if (handler.isClient()) {
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.TICK, new BlockEvent.UpdateEvent(handler.getBlock()));
			return;
		}
		EventHooks.onEvent(handler, EnumScriptType.TICK, new BlockEvent.UpdateEvent(handler.getBlock()));
	}

	public static boolean onScriptItemAttack(ItemScriptedWrapper handler, ItemEvent.AttackEvent event) {
		return EventHooks.onEvent(handler, EnumScriptType.ATTACK, event);
	}

	public static void onScriptItemInit(ItemScriptedWrapper handler) {
		if (handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.INIT, new ItemEvent.InitEvent(handler));
	}

	public static void onClientInit(ClientScriptData handler) {
		if (!handler.isClient()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.INIT, new PlayerEvent.InitEvent((IPlayer<?>) NpcAPI.Instance().getIEntity(CustomNpcs.proxy.getPlayer())));
	}
	public static boolean onScriptItemInteract(ItemScriptedWrapper handler, ItemEvent.InteractEvent event) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.INTERACT, new ItemEvent.InitEvent(handler));
	}

	public static boolean onScriptItemPickedUp(ItemScriptedWrapper handler, EntityPlayer player, EntityItem entity) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.PICKEDUP, new ItemEvent.PickedUpEvent(handler, PlayerData.get(player).scriptData.getPlayer(), (IEntityItem<?>) NpcAPI.Instance().getIEntity(entity)));
	}

	public static boolean onScriptItemSpawn(ItemScriptedWrapper handler, EntityItem entity) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.SPAWN, new ItemEvent.SpawnEvent(handler, (IEntityItem<?>) NpcAPI.Instance().getIEntity(entity)));
	}

	public static boolean onScriptItemTossed(ItemScriptedWrapper handler, EntityPlayer player, EntityItem entity) {
		if (handler.isClient()) { return false; }
		return EventHooks.onEvent(handler, EnumScriptType.TOSSED, new ItemEvent.TossedEvent(handler, PlayerData.get(player).scriptData.getPlayer(), (IEntityItem<?>) NpcAPI.Instance().getIEntity(entity)));
	}

	public static void onScriptItemUpdate(ItemScriptedWrapper handler, EntityPlayer player) {
		if (handler.isClient()) {
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.TICK, new ItemEvent.UpdateEvent(handler, PlayerData.get(player).scriptData.getPlayer()));
			return;
		}
		EventHooks.onEvent(handler, EnumScriptType.TICK, new ItemEvent.UpdateEvent(handler, PlayerData.get(player).scriptData.getPlayer()));
	}

	public static void onWorldScriptEvent(noppes.npcs.api.event.WorldEvent.ScriptCommandEvent event) {
		EventHooks.onEvent(ScriptController.Instance.playerScripts, EnumScriptType.SCRIPT_COMMAND, event);
	}

	public static void onCustomPotionIsReady(IsReadyEvent event) {
		PotionScriptData data = ScriptController.Instance.potionScripts;
		if (!data.isEnabled() || CustomNpcs.proxy.hasClient()) { return; }
		EventHooks.onEvent(data, EnumScriptType.POTION_IS_READY, event);
	}

	public static void onCustomPotionPerformEffect(PerformEffect event) {
		PotionScriptData data = ScriptController.Instance.potionScripts;
		if (!data.isEnabled() || CustomNpcs.proxy.hasClient()) { return; }
		EventHooks.onEvent(data, EnumScriptType.POTION_PERFORM, event);
	}

	public static void onCustomPotionAffectEntity(AffectEntity event) {
		PotionScriptData data = ScriptController.Instance.potionScripts;
		if (!data.isEnabled() || CustomNpcs.proxy.hasClient()) { return; }
		EventHooks.onEvent(data, EnumScriptType.POTION_AFFECT, event);
	}

	public static void onCustomPotionEndEffect(EndEffect event) {
		PotionScriptData data = ScriptController.Instance.potionScripts;
		if (!data.isEnabled() || CustomNpcs.proxy.hasClient()) { return; }
		EventHooks.onEvent(data, EnumScriptType.POTION_END, event);
	}

	
	public static void onScriptTriggerEvent(int id, IWorld level, IPos pos, IEntity<?> entity, Object[] arguments) {
		ScriptTriggerEvent event = new ScriptTriggerEvent(id, level, pos, entity, arguments);
		if (event.entity != null && event.world != null && !(event.entity.getMCEntity() instanceof FakePlayer)) {
			if (event.entity.getType() == 1) {
				EventHooks.onEvent(PlayerData.get((EntityPlayer) event.entity.getMCEntity()).scriptData, EnumScriptType.SCRIPT_TRIGGER, event);
			}
			else if (event.entity.getType() == 2) {
				EventHooks.onEvent(((EntityNPCInterface) event.entity.getMCEntity()).script, EnumScriptType.SCRIPT_TRIGGER, event);
			}
			else {
				TileEntity tile = event.world.getMCWorld().getTileEntity(event.pos.getMCBlockPos());
				if (tile instanceof IScriptBlockHandler) {
					EventHooks.onEvent((IScriptBlockHandler) tile, EnumScriptType.SCRIPT_TRIGGER, event);
				}
			}
		}
		if (ScriptController.Instance.forgeScripts.isClient()) { EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.SCRIPT_TRIGGER, event); }
		else { EventHooks.onEvent(ScriptController.Instance.forgeScripts, EnumScriptType.SCRIPT_TRIGGER, event); }
	}
	
	public static void onScriptTriggerEvent(IScriptHandler handler, int id, IWorld world, IPos pos, IEntity<?> entity, Object[] arguments) {
		ScriptTriggerEvent event = new ScriptTriggerEvent(id, world, pos, entity, arguments);
		EventHooks.onEvent(handler, EnumScriptType.SCRIPT_TRIGGER, event);
	}

	public static void onPlayerPlaySound(PlayerScriptData handler, PlayerSound event) {
		EventHooks.onEvent(handler, EnumScriptType.SOUND_PLAY, event);
	}

	public static void onPlayerStopSound(PlayerScriptData handler, PlayerSound event) {
		EventHooks.onEvent(handler, EnumScriptType.SOUND_STOP, event);
	}

	public static void onPackageReceived(PackageReceived event) {
		IScriptHandler handler;
		if (event.side) { handler = ScriptController.Instance.forgeScripts; }
		else { handler = ScriptController.Instance.clientScripts; }
		EventHooks.onEvent(handler, EnumScriptType.PACKEGE_RECEIVED, event);
	}

	public static void onScriptPackage(EntityPlayer player, NBTTagCompound nbt) {
		IScriptHandler handler;
		if (Thread.currentThread().getName().toLowerCase().indexOf("client")!=-1) { handler = ScriptController.Instance.clientScripts; }
		else { handler = PlayerData.get(player).scriptData; }
		if (!handler.getEnabled()) { return; }
		if (player==null) { player = CustomNpcs.proxy.getPlayer(); }
		EventHooks.onEvent(handler, EnumScriptType.PACKEGE_FROM, new PlayerPackage((IPlayer<?>) NpcAPI.Instance().getIEntity(player), NpcAPI.Instance().getINbt(nbt)));
	}

	public static CustomTeleport onPlayerTeleport(EntityPlayerMP player, BlockPos to, BlockPos portal, int dimId) {
		NpcAPI api = NpcAPI.Instance();
		CustomTeleport event = new PlayerEvent.CustomTeleport((IPlayer<?>) api.getIEntity(player), api.getIPos(portal.getX(), portal.getY(), portal.getZ()), api.getIPos(to.getX(), to.getY(), to.getZ()), dimId);
		if (player==null) { return event; }
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		if (!handler.getEnabled()) { return event; }
		EventHooks.onEvent(handler, EnumScriptType.CUSTOM_TELEPORT, event);
		return event;
	}

	public static CustomNpcTeleport onNpcTeleport(EntityNPCInterface npc, BlockPos to, BlockPos portal, int dimId) {
		NpcAPI api = NpcAPI.Instance();
		CustomNpcTeleport event = new NpcEvent.CustomNpcTeleport((ICustomNpc<?>) api.getIEntity(npc), api.getIPos(portal.getX(), portal.getY(), portal.getZ()), api.getIPos(to.getX(), to.getY(), to.getZ()), dimId);
		if (npc==null) { return event; }
		DataScript handler = npc.script;
		if (!handler.getEnabled()) { return event; }
		EventHooks.onEvent(handler, EnumScriptType.CUSTOM_TELEPORT, event);
		return event;
	}

	public static void onPlayerKeyActive(EntityPlayerMP player, int id) {
		if (player==null) { return; }
		IKeySetting kb = NpcAPI.Instance().getIKeyBinding().getKeySetting(id);
		if (kb==null) { return; }
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		if (!handler.getEnabled()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.KEY_ACTIVE, new PlayerEvent.KeyActive((IPlayer<?>) NpcAPI.Instance().getIEntity(player), kb));
	}

	public static void onNPCStopAnimation(EntityNPCInterface npc, int type, int variant) {
		if (npc.script.isClient()) { return; }
		EventHooks.onEvent(npc.script, EnumScriptType.STOP_ANIMATION, new NpcEvent.StopAnimation(npc.wrappedNPC, type, variant));
	}

	public static void onPlayerOpenGui(EntityPlayerMP player, String newGUI, String oldGUI) {
		if (player==null) { return; }
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		if (!handler.getEnabled()) { return; }
		EventHooks.onEvent(handler, EnumScriptType.GUI_OPEN, new PlayerEvent.OpenGUI((IPlayer<?>) NpcAPI.Instance().getIEntity(player), newGUI, oldGUI));
	}
	
	public static boolean onEvent(IScriptHandler handler, EnumScriptType enumFunction, Event event) {
		if (handler == null || !handler.getEnabled() || event == null || enumFunction==null) { return false; }
		handler.runScript(enumFunction.function, event);
		return WrapperNpcAPI.EVENT_BUS.post(event);
	}

	private static boolean onEvent(ScriptContainer script, EnumScriptType enumFunction, Event event) {
		if (script == null || event == null || enumFunction==null) { return false; }
		script.run(enumFunction.function, event, true);
		return WrapperNpcAPI.EVENT_BUS.post(event);
	}
	
}
