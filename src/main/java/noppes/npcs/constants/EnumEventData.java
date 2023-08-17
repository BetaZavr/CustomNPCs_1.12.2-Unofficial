package noppes.npcs.constants;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.ILine;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.role.IRoleTransporter.ITransportLocation;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.CustomNPCsEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PackageReceived;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.PlayerEvent.PlayerPackage;
import noppes.npcs.api.event.ProjectileEvent;
import noppes.npcs.api.event.QuestEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.CustomPotionEvent;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogOption;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.util.EventData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.PlayerMail;

public enum EnumEventData {
	
	BlockBroken(new EventData(BlockEvent.BreakEvent.class, 
			BlockEvent.class,
			"event.block.break",
			EnumScriptType.BROKEN.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockClicked(new EventData(BlockEvent.ClickedEvent.class, 
			BlockEvent.class,
			"event.block.clicked",
			EnumScriptType.CLICKED.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockCollide(new EventData(BlockEvent.CollidedEvent.class, 
			BlockEvent.class,
			"event.block.collided",
			EnumScriptType.COLLIDE.function,
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockDoorToggle(new EventData(BlockEvent.DoorToggleEvent.class, 
			BlockEvent.class,
			"event.block.doortoggle",
			EnumScriptType.DOOR_TOGGLE.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockExploded(new EventData(BlockEvent.ExplodedEvent.class, 
			BlockEvent.class,
			"event.block.exploded",
			EnumScriptType.EXPLODED.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockFallenUpon(new EventData(BlockEvent.EntityFallenUponEvent.class, 
			BlockEvent.class,
			"event.block.entityfallenupon",
			EnumScriptType.FALLEN_UPON.function,
			new MetodData(float.class, "distanceFallen", "event.block.float"),
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockHarvested(new EventData(BlockEvent.HarvestedEvent.class, 
			BlockEvent.class,
			"event.block.harvested",
			EnumScriptType.HARVESTED.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockInit(new EventData(BlockEvent.InitEvent.class, 
			BlockEvent.class,
			"event.block.init",
			EnumScriptType.INIT.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockInteract(new EventData(BlockEvent.InteractEvent.class, 
			BlockEvent.class,
			"event.block.interact",
			EnumScriptType.INTERACT.function,
			new MetodData(float.class, "hitX", "event.block.float"),
			new MetodData(float.class, "hitY", "event.block.float"),
			new MetodData(float.class, "hitZ", "event.block.float"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(int.class, "side", "event.block.int"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockNeighborChanged(new EventData(BlockEvent.NeighborChangedEvent.class, 
			BlockEvent.class,
			"event.block.neighborchanged",
			EnumScriptType.NEIGHBOR_CHANGED.function,
			new MetodData(IPos.class, "changedPos", "parameter.pos"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockRainFilled(new EventData(BlockEvent.RainFillEvent.class, 
			BlockEvent.class,
			"event.block.rainfill",
			EnumScriptType.RAIN_FILLED.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockRedstone(new EventData(BlockEvent.RedstoneEvent.class, 
			BlockEvent.class,
			"event.block.redstone",
			EnumScriptType.REDSTONE.function,
			new MetodData(int.class, "power", "event.block.int"),
			new MetodData(int.class, "prevPower", "event.block.int"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockTick(new EventData(BlockEvent.UpdateEvent.class, 
			BlockEvent.class,
			"event.block.update",
			EnumScriptType.TICK.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockTimer(new EventData(BlockEvent.TimerEvent.class, 
			BlockEvent.class,
			"event.block.timer",
			EnumScriptType.TIMER.function,
			new MetodData(int.class, "id", "event.block.int"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomContainerCustomChestClosed(new EventData(CustomContainerEvent.CloseEvent.class, 
			CustomContainerEvent.class,
			"event.customcontainer.close",
			EnumScriptType.CUSTOM_CHEST_CLOSED.function,
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomContainerCustomGuiSlotClicked(new EventData(CustomContainerEvent.SlotClickedEvent.class, 
			CustomContainerEvent.class,
			"event.customcontainer.slotclicked",
			EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function,
			new MetodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MetodData(int.class, "slot", "event.customcontainer.int"),
			new MetodData(IItemStack.class, "slotItem", "parameter.stack"),
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiCustomGuiButton(new EventData(CustomGuiEvent.ButtonEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.button",
			EnumScriptType.CUSTOM_GUI_BUTTON.function,
			new MetodData(int.class, "buttonId", "event.customgui.int"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiCustomGuiClosed(new EventData(CustomGuiEvent.CloseEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.close",
			EnumScriptType.CUSTOM_GUI_CLOSED.function,
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiCustomGuiScroll(new EventData(CustomGuiEvent.ScrollEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.scroll",
			EnumScriptType.CUSTOM_GUI_SCROLL.function,
			new MetodData(boolean.class, "doubleClick", "parameter.boolean"),
			new MetodData(int.class, "scrollId", "event.customgui.int"),
			new MetodData(int.class, "scrollIndex", "event.customgui.int"),
			new MetodData(String[].class, "selection", "String"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiCustomGuiSlot(new EventData(CustomGuiEvent.SlotEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.slot",
			EnumScriptType.CUSTOM_GUI_SLOT.function,
			new MetodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MetodData(int.class, "slotId", "event.customgui.int"),
			new MetodData(IItemStack.class, "stack", "parameter.stack"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiCustomGuiSlotClicked(new EventData(CustomGuiEvent.SlotClickEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.slotclick",
			EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function,
			new MetodData(String.class, "clickType", "event.customgui.string"),
			new MetodData(int.class, "dragType", "event.customgui.int"),
			new MetodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MetodData(int.class, "slotId", "event.customgui.int"),
			new MetodData(IItemStack.class, "stack", "parameter.stack"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogDialog(new EventData(DialogEvent.OpenEvent.class, 
			DialogEvent.class,
			"event.dialog.open",
			EnumScriptType.DIALOG.function,
			new MetodData(IDialog.class, "dialog", "parameter.dialog"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogDialogClose(new EventData(DialogEvent.CloseEvent.class, 
			DialogEvent.class,
			"event.dialog.close",
			EnumScriptType.DIALOG_CLOSE.function,
			new MetodData(IDialog.class, "dialog", "parameter.dialog"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogDialogOption(new EventData(DialogEvent.OptionEvent.class, 
			DialogEvent.class,
			"event.dialog.option",
			EnumScriptType.DIALOG_OPTION.function,
			new MetodData(IDialogOption.class, "option", "parameter.dialog.option"),
			new MetodData(IDialog.class, "dialog", "parameter.dialog"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemAttack(new EventData(ItemEvent.AttackEvent.class, 
			ItemEvent.class,
			"event.item.attack",
			EnumScriptType.ATTACK.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(Object.class, "target", "event.item.object"),
			new MetodData(int.class, "type", "event.item.int"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemInit(new EventData(ItemEvent.InitEvent.class, 
			ItemEvent.class,
			"event.item.init",
			EnumScriptType.INIT.function,
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemInteract(new EventData(ItemEvent.InteractEvent.class, 
			ItemEvent.class,
			"event.item.interact",
			EnumScriptType.INTERACT.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(Object.class, "target", "event.item.object"),
			new MetodData(int.class, "type", "event.item.int"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemPickedUp(new EventData(ItemEvent.PickedUpEvent.class, 
			ItemEvent.class,
			"event.item.pickedup",
			EnumScriptType.PICKEDUP.function,
			new MetodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemSpawn(new EventData(ItemEvent.SpawnEvent.class, 
			ItemEvent.class,
			"event.item.spawn",
			EnumScriptType.SPAWN.function,
			new MetodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemTick(new EventData(ItemEvent.UpdateEvent.class, 
			ItemEvent.class,
			"event.item.update",
			EnumScriptType.TICK.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemTossed(new EventData(ItemEvent.TossedEvent.class, 
			ItemEvent.class,
			"event.item.tossed",
			EnumScriptType.TOSSED.function,
			new MetodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcCollide(new EventData(NpcEvent.CollideEvent.class, 
			NpcEvent.class,
			"event.npc.collide",
			EnumScriptType.COLLIDE.function,
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcDamaged(new EventData(NpcEvent.DamagedEvent.class, 
			NpcEvent.class,
			"event.npc.damaged",
			EnumScriptType.DAMAGED.function,
			new MetodData(boolean.class, "clearTarget", "parameter.boolean"),
			new MetodData(float.class, "damage", "event.npc.float"),
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcDied(new EventData(NpcEvent.DiedEvent.class, 
			NpcEvent.class,
			"event.npc.died",
			EnumScriptType.DIED.function,
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IItemStack[].class, "droppedItems", "IItemStack"),
			new MetodData(int.class, "expDropped", "event.npc.int"),
			new MetodData(ILine.class, "line", "event.npc.iline"),
			new MetodData(IItemStack[].class, "lootedItems", "IItemStack"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(String.class, "type", "event.npc.string"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcInit(new EventData(NpcEvent.InitEvent.class, 
			NpcEvent.class,
			"event.npc.init",
			EnumScriptType.INIT.function,
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcInteract(new EventData(NpcEvent.InteractEvent.class, 
			NpcEvent.class,
			"event.npc.interact",
			EnumScriptType.INTERACT.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcKill(new EventData(NpcEvent.KilledEntityEvent.class, 
			NpcEvent.class,
			"event.npc.killedentity",
			EnumScriptType.KILL.function,
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcMeleeAttack(new EventData(NpcEvent.MeleeAttackEvent.class, 
			NpcEvent.class,
			"event.npc.meleeattack",
			EnumScriptType.ATTACK_MELEE.function,
			new MetodData(float.class, "damage", "event.npc.float"),
			new MetodData(IEntityLivingBase.class, "target", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcRangedLaunched(new EventData(NpcEvent.RangedLaunchedEvent.class, 
			NpcEvent.class,
			"event.npc.rangedlaunched",
			EnumScriptType.RANGED_LAUNCHED.function,
			new MetodData(float.class, "damage", "event.npc.float"),
			new MetodData(List.class, "projectiles", "event.npc.list"),
			new MetodData(IEntityLivingBase.class, "target", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTarget(new EventData(NpcEvent.TargetEvent.class, 
			NpcEvent.class,
			"event.npc.target",
			EnumScriptType.TARGET.function,
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTargetLost(new EventData(NpcEvent.TargetLostEvent.class, 
			NpcEvent.class,
			"event.npc.targetlost",
			EnumScriptType.TARGET_LOST.function,
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTick(new EventData(NpcEvent.UpdateEvent.class, 
			NpcEvent.class,
			"event.npc.update",
			EnumScriptType.TICK.function,
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTimer(new EventData(NpcEvent.TimerEvent.class, 
			NpcEvent.class,
			"event.npc.timer",
			EnumScriptType.TIMER.function,
			new MetodData(int.class, "id", "event.npc.int"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcStopAnimation(new EventData(NpcEvent.StopAnimation.class, 
			NpcEvent.class,
			"event.npc.stop.animation",
			EnumScriptType.STOP_ANIMATION.function,
			new MetodData(int.class, "type", "parameter.animation.type"),
			new MetodData(int.class, "variant", "parameter.animation.variant"),
			new MetodData(IAnimation.class, "animation", "parameter.animation"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerAttack(new EventData(PlayerEvent.AttackEvent.class, 
			PlayerEvent.class,
			"event.player.attack",
			EnumScriptType.ATTACK.function,
			new MetodData(Object.class, "target", "event.player.object"),
			new MetodData(int.class, "type", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerBroken(new EventData(PlayerEvent.BreakEvent.class, 
			PlayerEvent.class,
			"event.player.break",
			EnumScriptType.BROKEN.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(int.class, "exp", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerChat(new EventData(PlayerEvent.ChatEvent.class, 
			PlayerEvent.class,
			"event.player.chat",
			EnumScriptType.CHAT.function,
			new MetodData(String.class, "message", "event.player.string"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerContainerClosed(new EventData(PlayerEvent.ContainerClosed.class, 
			PlayerEvent.class,
			"event.player.containerclosed",
			EnumScriptType.CONTAINER_CLOSED.function,
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerContainerOpen(new EventData(PlayerEvent.ContainerOpen.class, 
			PlayerEvent.class,
			"event.player.containeropen",
			EnumScriptType.CONTAINER_OPEN.function,
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDamaged(new EventData(PlayerEvent.DamagedEvent.class, 
			PlayerEvent.class,
			"event.player.damaged",
			EnumScriptType.DAMAGED.function,
			new MetodData(boolean.class, "clearTarget", "parameter.boolean"),
			new MetodData(float.class, "damage", "event.player.float"),
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDamagedEntity(new EventData(PlayerEvent.DamagedEntityEvent.class, 
			PlayerEvent.class,
			"event.player.damagedentity",
			EnumScriptType.DAMAGED_ENTITY.function,
			new MetodData(float.class, "damage", "event.player.float"),
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "target", "parameter.entity"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDied(new EventData(PlayerEvent.DiedEvent.class, 
			PlayerEvent.class,
			"event.player.died",
			EnumScriptType.DIED.function,
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(String.class, "type", "event.player.string"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerFactionUpdate(new EventData(PlayerEvent.FactionUpdateEvent.class, 
			PlayerEvent.class,
			"event.player.factionupdate",
			EnumScriptType.FACTION_UPDATE.function,
			new MetodData(IFaction.class, "faction", "parameter.faction"),
			new MetodData(boolean.class, "init", "parameter.boolean"),
			new MetodData(int.class, "points", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerInit(new EventData(PlayerEvent.InitEvent.class, 
			PlayerEvent.class,
			"event.init",
			EnumScriptType.INIT.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerInteract(new EventData(PlayerEvent.InteractEvent.class, 
			PlayerEvent.class,
			"event.player.interact",
			EnumScriptType.INTERACT.function,
			new MetodData(Object.class, "target", "event.player.object"),
			new MetodData(int.class, "type", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerItemCrafted(new EventData(PlayerEvent.ItemCrafted.class, 
			PlayerEvent.class,
			"event.player.itemcrafted",
			EnumScriptType.ITEM_CRAFTED.function,
			new MetodData(IItemStack.class, "crafting", "parameter.stack"),
			new MetodData(IInventory.class, "craftMatrix", "parameter.inventory"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerItemFished(new EventData(PlayerEvent.ItemFished.class, 
			PlayerEvent.class,
			"event.player.itemfished",
			EnumScriptType.ITEM_FISHED.function,
			new MetodData(int.class, "rodDamage", "event.player.int"),
			new MetodData(IItemStack[].class, "stacks", "IItemStack"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKeyDown(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.keypressed",
			EnumScriptType.KEY_DOWN.function,
			new MetodData(boolean.class, "isAltPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.boolean"),
			new MetodData(int.class, "key", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKeyPressed(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.keypressed",
			EnumScriptType.KEY_UP.function,
			new MetodData(boolean.class, "isAltPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.boolean"),
			new MetodData(int.class, "key", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKill(new EventData(PlayerEvent.KilledEntityEvent.class, 
			PlayerEvent.class,
			"event.player.killedentity",
			EnumScriptType.KILL.function,
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLevelUp(new EventData(PlayerEvent.LevelUpEvent.class, 
			PlayerEvent.class,
			"event.player.levelup",
			EnumScriptType.LEVEL_UP.function,
			new MetodData(int.class, "change", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLogin(new EventData(PlayerEvent.LoginEvent.class, 
			PlayerEvent.class,
			"event.player.login",
			EnumScriptType.LOGIN.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLogout(new EventData(PlayerEvent.LogoutEvent.class, 
			PlayerEvent.class,
			"event.player.logout",
			EnumScriptType.LOGOUT.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerMouseDown(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.keypressed",
			EnumScriptType.MOUSE_DOWN.function,
			new MetodData(boolean.class, "isAltPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.boolean"),
			new MetodData(int.class, "key", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerMousePressed(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.keypressed",
			EnumScriptType.MOUSE_UP.function,
			new MetodData(boolean.class, "isAltPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.boolean"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.boolean"),
			new MetodData(int.class, "key", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPickUp(new EventData(PlayerEvent.PickUpEvent.class, 
			PlayerEvent.class,
			"event.player.pickup",
			EnumScriptType.PICKUP.function,
			new MetodData(IItemStack.class, "item", "parameter.stack"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPlased(new EventData(PlayerEvent.PlaceEvent.class, 
			PlayerEvent.class,
			"event.player.place",
			EnumScriptType.PLASED.function,
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(int.class, "exp", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerRangedLaunched(new EventData(PlayerEvent.RangedLaunchedEvent.class, 
			PlayerEvent.class,
			"event.player.rangedlaunched",
			EnumScriptType.RANGED_LAUNCHED.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerSoundPlayed(new EventData(PlayerEvent.PlayerSound.class, 
			PlayerEvent.class,
			"event.player.playersound",
			EnumScriptType.SOUND_PLAY.function,
			new MetodData(String.class, "name", "event.player.string"),
			new MetodData(String.class, "resource", "event.player.string"),
			new MetodData(String.class, "category", "event.player.string"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(float.class, "volume", "event.player.float"),
			new MetodData(float.class, "pitch", "event.player.float"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerSoundStoped(new EventData(PlayerEvent.PlayerSound.class, 
			PlayerEvent.class,
			"event.player.playersound",
			EnumScriptType.SOUND_STOP.function,
			new MetodData(String.class, "name", "event.player.string"),
			new MetodData(String.class, "resource", "event.player.string"),
			new MetodData(String.class, "category", "event.player.string"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(float.class, "volume", "event.player.float"),
			new MetodData(float.class, "pitch", "event.player.float"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerTick(new EventData(PlayerEvent.UpdateEvent.class, 
			PlayerEvent.class,
			"event.player.update",
			EnumScriptType.TICK.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerTimer(new EventData(PlayerEvent.TimerEvent.class, 
			PlayerEvent.class,
			"event.player.timer",
			EnumScriptType.TIMER.function,
			new MetodData(int.class, "id", "event.player.int"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerToss(new EventData(PlayerEvent.TossEvent.class, 
			PlayerEvent.class,
			"event.player.toss",
			EnumScriptType.TOSS.function,
			new MetodData(IItemStack.class, "item", "parameter.stack"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ProjectileProjectileImpact(new EventData(ProjectileEvent.ImpactEvent.class, 
			ProjectileEvent.class,
			"event.projectile.impact",
			EnumScriptType.PROJECTILE_IMPACT.function,
			new MetodData(Object.class, "target", "event.projectile.object"),
			new MetodData(int.class, "type", "event.projectile.int"),
			new MetodData(IProjectile.class, "projectile", "parameter.projectile"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ProjectileProjectileTick(new EventData(ProjectileEvent.UpdateEvent.class, 
			ProjectileEvent.class,
			"event.projectile.update",
			EnumScriptType.PROJECTILE_TICK.function,
			new MetodData(IProjectile.class, "projectile", "parameter.projectile"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestCanceled(new EventData(QuestEvent.QuestCanceledEvent.class, 
			QuestEvent.class,
			"event.quest.questcanceled",
			EnumScriptType.QUEST_CANCELED.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "parameter.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestCompleted(new EventData(QuestEvent.QuestCompletedEvent.class, 
			QuestEvent.class,
			"event.quest.questcompleted",
			EnumScriptType.QUEST_COMPLETED.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "parameter.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestStart(new EventData(QuestEvent.QuestStartEvent.class, 
			QuestEvent.class,
			"event.quest.queststart",
			EnumScriptType.QUEST_START.function,
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "parameter.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestTurnIn(new EventData(QuestEvent.QuestTurnedInEvent.class, 
			QuestEvent.class,
			"event.quest.questturnedin",
			EnumScriptType.QUEST_TURNIN.function,
			new MetodData(int.class, "expReward", "event.quest.int"),
			new MetodData(IItemStack[].class, "itemRewards", "IItemStack"),
			new MetodData(FactionOptions.class, "factionOptions", "parameter.faction.options"),
			new MetodData(PlayerMail.class, "mail", "parameter.mail"),
			new MetodData(int.class, "nextQuestId", "event.quest.int"),
			new MetodData(String.class, "command", "event.quest.string"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "parameter.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleBankUnlocked(new EventData(RoleEvent.BankUnlockedEvent.class, 
			RoleEvent.class,
			"event.role.bankunlocked",
			EnumScriptType.ROLE.function,
			new MetodData(int.class, "slot", "event.role.int"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleBankUpgraded(new EventData(RoleEvent.BankUpgradedEvent.class, 
			RoleEvent.class,
			"event.role.bankupgraded",
			EnumScriptType.ROLE.function,
			new MetodData(int.class, "slot", "event.role.int"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleFollowerFinished(new EventData(RoleEvent.FollowerFinishedEvent.class, 
			RoleEvent.class,
			"event.role.followerfinished",
			EnumScriptType.ROLE.function,
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleFollowerHire(new EventData(RoleEvent.FollowerHireEvent.class, 
			RoleEvent.class,
			"event.role.followerhire",
			EnumScriptType.ROLE.function,
			new MetodData(int.class, "days", "event.role.int"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleMailman(new EventData(RoleEvent.MailmanEvent.class, 
			RoleEvent.class,
			"event.role.mailman",
			EnumScriptType.ROLE.function,
			new MetodData(IPlayerMail.class, "mail", "parameter.mail"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTradeFailed(new EventData(RoleEvent.TradeFailedEvent.class, 
			RoleEvent.class,
			"event.role.tradefailed",
			EnumScriptType.ROLE.function,
			new MetodData(IItemStack.class, "currency1", "parameter.stack"),
			new MetodData(IItemStack.class, "currency2", "parameter.stack"),
			new MetodData(IItemStack.class, "receiving", "parameter.stack"),
			new MetodData(IItemStack.class, "sold", "parameter.stack"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTrader(new EventData(RoleEvent.TraderEvent.class, 
			RoleEvent.class,
			"event.role.trader",
			EnumScriptType.ROLE.function,
			new MetodData(IItemStack.class, "currency1", "parameter.stack"),
			new MetodData(IItemStack.class, "currency2", "parameter.stack"),
			new MetodData(IItemStack.class, "sold", "parameter.stack"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTransporterUnlocked(new EventData(RoleEvent.TransporterUnlockedEvent.class, 
			RoleEvent.class,
			"event.role.transporterunlocked",
			EnumScriptType.ROLE.function,
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTransporterUse(new EventData(RoleEvent.TransporterUseEvent.class, 
			RoleEvent.class,
			"event.role.transporteruse",
			EnumScriptType.ROLE.function,
			new MetodData(ITransportLocation.class, "location", "parameter.transport.location"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	WorldScriptCommand(new EventData(WorldEvent.ScriptCommandEvent.class, 
			WorldEvent.class,
			"event.world.scriptcommand",
			EnumScriptType.ROLE.function,
			new MetodData(String[].class, "arguments", "String"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(IWorld.class, "world", "event.world.iworld"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	WorldTrigger(new EventData(WorldEvent.ScriptTriggerEvent.class, 
			WorldEvent.class,
			"event.world.scripttrigger",
			EnumScriptType.SCRIPT_TRIGGER.function,
			new MetodData(Object[].class, "arguments", "Object"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(int.class, "id", "event.world.int"),
			new MetodData(IWorld.class, "world", "event.world.iworld"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	
	
	
	AffectEntity(new EventData(AffectEntity.class, 
			CustomPotionEvent.class,
			"event.potion.affectentity",
			EnumScriptType.POTION_AFFECT.function,
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(IEntity.class, "indirectSource", "parameter.entity"),
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MetodData(double.class, "health", "parameter.health"),
			new MetodData(ICustomElement.class, "potion", "event.potion.type"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	EndEffect(new EventData(EndEffect.class, 
			CustomPotionEvent.class,
			"event.potion.endeffect",
			EnumScriptType.POTION_END.function,
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MetodData(ICustomElement.class, "potion", "event.potion.type"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	IsReadyEvent(new EventData(IsReadyEvent.class, 
			CustomPotionEvent.class,
			"event.potion.isreadyevent",
			EnumScriptType.POTION_IS_READY.function,
			new MetodData(boolean.class, "ready", "parameter.potion.ready"),
			new MetodData(int.class, "duration", "parameter.potion.duration"),
			new MetodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MetodData(ICustomElement.class, "potion", "event.potion.type"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PerformEffect(new EventData(PerformEffect.class, 
			CustomPotionEvent.class,
			"event.potion.performeffect",
			EnumScriptType.POTION_PERFORM.function,
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MetodData(ICustomElement.class, "potion", "event.potion.type"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PackageReceived(new EventData(PackageReceived.class, 
			CustomNPCsEvent.class,
			"event.api.packagereceived",
			EnumScriptType.POTION_PERFORM.function,
			new MetodData(ChannelHandlerContext.class, "channel", "parameter.channel"),
			new MetodData(boolean.class, "side", "parameter.side"),
			new MetodData(Object.class, "message", "parameter.package.message"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPackage(new EventData(PlayerPackage.class, 
			PlayerEvent.class,
			"event.player.playerpackage",
			EnumScriptType.PACKEGE_FROM.function,
			new MetodData(INbt.class, "nbt", "parameter.nbt"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ForgeEvent(new EventData(ForgeEvent.class, 
			CustomNPCsEvent.class,
			"event.forge",
			"any_forge_event",
			new MetodData(Event.class, "event", "parameter.event"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(IWorld.class, "world", "parameter.world"),
			new MetodData(IItemStack.class, "stack", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	SoundTickEvent(new EventData(ForgeEvent.SoundTickEvent.class, 
			ForgeEvent.class,
			"event.forge.soundtick",
			EnumScriptType.SOUND_TICK_EVENT.function,
			new MetodData(String.class, "name", "event.player.string"),
			new MetodData(String.class, "resource", "event.player.string"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(float.class, "volume", "event.player.float"),
			new MetodData(float.class, "pitch", "event.player.float"),
			new MetodData(int.class, "tick", "parameter.music.tick"),
			new MetodData(int.class, "ticks", "parameter.music.ticks"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	);
	
	public EventData ed;
	
	EnumEventData(EventData interfaseData) { this.ed = interfaseData; }

	public static EventData get(String enumName) {
		for (EnumEventData enumED : EnumEventData.values()) { 
			if (enumED.name().equals(enumName)) { return enumED.ed;}
		}
		return null;
	}
	
}
