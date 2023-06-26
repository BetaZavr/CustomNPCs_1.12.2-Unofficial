package noppes.npcs.constants;

import java.util.List;

import net.minecraft.inventory.IInventory;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IDamageSource;
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
import noppes.npcs.api.entity.data.ILine;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.role.IRoleTransporter.ITransportLocation;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.ProjectileEvent;
import noppes.npcs.api.event.QuestEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.event.WorldEvent;
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
			"broken",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockClicked(new EventData(BlockEvent.ClickedEvent.class, 
			BlockEvent.class,
			"event.block.clicked",
			"clicked",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockCollide(new EventData(BlockEvent.CollidedEvent.class, 
			BlockEvent.class,
			"event.block.collided",
			"collide",
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockDoorToggle(new EventData(BlockEvent.DoorToggleEvent.class, 
			BlockEvent.class,
			"event.block.doortoggle",
			"doorToggle",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockExploded(new EventData(BlockEvent.ExplodedEvent.class, 
			BlockEvent.class,
			"event.block.exploded",
			"exploded",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockFallenUpon(new EventData(BlockEvent.EntityFallenUponEvent.class, 
			BlockEvent.class,
			"event.block.entityfallenupon",
			"fallenUpon",
			new MetodData(float.class, "distanceFallen", "event.block.distancefallen"),
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockHarvested(new EventData(BlockEvent.HarvestedEvent.class, 
			BlockEvent.class,
			"event.block.harvested",
			"harvested",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockInit(new EventData(BlockEvent.InitEvent.class, 
			BlockEvent.class,
			"event.init",
			"init",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockInteract(new EventData(BlockEvent.InteractEvent.class, 
			BlockEvent.class,
			"event.interact",
			"interact",
			new MetodData(float.class, "hitX", "event.block.hitx"),
			new MetodData(float.class, "hitY", "event.block.hity"),
			new MetodData(float.class, "hitZ", "event.block.hitz"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(int.class, "side", "event.block.side"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockNeighborChanged(new EventData(BlockEvent.NeighborChangedEvent.class, 
			BlockEvent.class,
			"event.block.neighborchanged",
			"neighborChanged",
			new MetodData(IPos.class, "changedPos", "parameter.pos"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockRainFilled(new EventData(BlockEvent.RainFillEvent.class, 
			BlockEvent.class,
			"event.block.rainfill",
			"rainFilled",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockRedstone(new EventData(BlockEvent.RedstoneEvent.class, 
			BlockEvent.class,
			"event.block.redstone",
			"redstone",
			new MetodData(int.class, "power", "event.block.power"),
			new MetodData(int.class, "prevPower", "event.block.prevpower"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockTick(new EventData(BlockEvent.UpdateEvent.class, 
			BlockEvent.class,
			"event.update",
			"tick",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockTimer(new EventData(BlockEvent.TimerEvent.class, 
			BlockEvent.class,
			"event.timer",
			"timer",
			new MetodData(int.class, "id", "parameter.itimers.id"),
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomContainerCustomChestClosed(new EventData(CustomContainerEvent.CloseEvent.class, 
			CustomContainerEvent.class,
			"event.customcontainer.close",
			"customChestClosed",
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomContainerCustomGuiSlotClicked(new EventData(CustomContainerEvent.SlotClickedEvent.class, 
			CustomContainerEvent.class,
			"event.customcontainer.slotclicked",
			"customGuiSlotClicked",
			new MetodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MetodData(int.class, "slot", "parameter.slot"),
			new MetodData(IItemStack.class, "slotItem", "parameter.stack"),
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiButton(new EventData(CustomGuiEvent.ButtonEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.button",
			"customGuiButton",
			new MetodData(int.class, "buttonId", "event.customgui.buttonid"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiClosed(new EventData(CustomGuiEvent.CloseEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.close",
			"customGuiClosed",
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiScroll(new EventData(CustomGuiEvent.ScrollEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.scroll",
			"customGuiScroll",
			new MetodData(boolean.class, "doubleClick", "event.customgui.doubleclick"),
			new MetodData(int.class, "scrollId", "event.customgui.scrollid"),
			new MetodData(int.class, "scrollIndex", "event.customgui.scrollindex"),
			new MetodData(String[].class, "selection", "event.customgui.selection"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiSlot(new EventData(CustomGuiEvent.SlotEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.slot",
			"customGuiSlot",
			new MetodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MetodData(int.class, "slotId", "parameter.slot"),
			new MetodData(IItemStack.class, "stack", "parameter.stack"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiSlotClicked(new EventData(CustomGuiEvent.SlotClickEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.slotclick",
			"customGuiSlotClicked",
			new MetodData(String.class, "clickType", "event.customgui.clicktype"),
			new MetodData(int.class, "dragType", "event.customgui.dragtype"),
			new MetodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MetodData(int.class, "slotId", "parameter.slot"),
			new MetodData(IItemStack.class, "stack", "parameter.stack"),
			new MetodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogDialog(new EventData(DialogEvent.OpenEvent.class, 
			DialogEvent.class,
			"event.dialog.open",
			"dialog",
			new MetodData(IDialog.class, "dialog", "parameter.dialog"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogDialogClose(new EventData(DialogEvent.CloseEvent.class, 
			DialogEvent.class,
			"event.dialog.close",
			"dialogClose",
			new MetodData(IDialog.class, "dialog", "parameter.dialog"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogDialogOption(new EventData(DialogEvent.OptionEvent.class, 
			DialogEvent.class,
			"event.dialog.option",
			"dialogOption",
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
			"attack",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(Object.class, "target", "event.item.target"),
			new MetodData(int.class, "type", "parameter.target.type"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemInit(new EventData(ItemEvent.InitEvent.class, 
			ItemEvent.class,
			"event.init",
			"init",
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemInteract(new EventData(ItemEvent.InteractEvent.class, 
			ItemEvent.class,
			"event.interact",
			"interact",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(Object.class, "target", "parameter.target"),
			new MetodData(int.class, "type", "parameter.target.type"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemPickedUp(new EventData(ItemEvent.PickedUpEvent.class, 
			ItemEvent.class,
			"event.item.pickedup",
			"pickedUp",
			new MetodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemSpawn(new EventData(ItemEvent.SpawnEvent.class, 
			ItemEvent.class,
			"event.item.spawn",
			"spawn",
			new MetodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemTick(new EventData(ItemEvent.UpdateEvent.class, 
			ItemEvent.class,
			"event.update",
			"tick",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemTossed(new EventData(ItemEvent.TossedEvent.class, 
			ItemEvent.class,
			"event.item.tossed",
			"tossed",
			new MetodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IItemScripted.class, "item", "parameter.stack"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcCollide(new EventData(NpcEvent.CollideEvent.class, 
			NpcEvent.class,
			"event.npc.collide",
			"collide",
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcDamaged(new EventData(NpcEvent.DamagedEvent.class, 
			NpcEvent.class,
			"event.damaged",
			"damaged",
			new MetodData(boolean.class, "clearTarget", "event.cleartarget"),
			new MetodData(float.class, "damage", "parameter.value"),
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcDied(new EventData(NpcEvent.DiedEvent.class, 
			NpcEvent.class,
			"event.died",
			"died",
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IItemStack[].class, "droppedItems", "event.npc.droppeditems"),
			new MetodData(int.class, "expDropped", "parameter.exp"),
			new MetodData(ILine.class, "line", "event.npc.line"),
			new MetodData(IItemStack[].class, "lootedItems", "event.npc.looteditems"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(String.class, "type", "event.npc.damagetype"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcInit(new EventData(NpcEvent.InitEvent.class, 
			NpcEvent.class,
			"event.init",
			"init",
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcInteract(new EventData(NpcEvent.InteractEvent.class, 
			NpcEvent.class,
			"event.interact",
			"interact",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcKill(new EventData(NpcEvent.KilledEntityEvent.class, 
			NpcEvent.class,
			"event.killedentity",
			"kill",
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcMeleeAttack(new EventData(NpcEvent.MeleeAttackEvent.class, 
			NpcEvent.class,
			"event.npc.meleeattack",
			"meleeAttack",
			new MetodData(float.class, "damage", "parameter.value"),
			new MetodData(IEntityLivingBase.class, "target", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcRangedLaunched(new EventData(NpcEvent.RangedLaunchedEvent.class, 
			NpcEvent.class,
			"event.rangedlaunched",
			"rangedLaunched",
			new MetodData(float.class, "damage", "parameter.value"),
			new MetodData(List.class, "projectiles", "event.npc.projectiles"),
			new MetodData(IEntityLivingBase.class, "target", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTarget(new EventData(NpcEvent.TargetEvent.class, 
			NpcEvent.class,
			"event.npc.target",
			"target",
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTargetLost(new EventData(NpcEvent.TargetLostEvent.class, 
			NpcEvent.class,
			"event.npc.targetlost",
			"targetLost",
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTick(new EventData(NpcEvent.UpdateEvent.class, 
			NpcEvent.class,
			"event.update",
			"tick",
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTimer(new EventData(NpcEvent.TimerEvent.class, 
			NpcEvent.class,
			"event.timer",
			"timer",
			new MetodData(int.class, "id", "parameter.itimers.id"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerAttack(new EventData(PlayerEvent.AttackEvent.class, 
			PlayerEvent.class,
			"event.player.attack",
			"attack",
			new MetodData(Object.class, "target", "parameter.target"),
			new MetodData(int.class, "type", "parameter.target.type"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerBroken(new EventData(PlayerEvent.BreakEvent.class, 
			PlayerEvent.class,
			"event.player.break",
			"broken",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(int.class, "exp", "parameter.exp"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerChat(new EventData(PlayerEvent.ChatEvent.class, 
			PlayerEvent.class,
			"event.player.chat",
			"chat",
			new MetodData(String.class, "message", "parameter.message"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerContainerClosed(new EventData(PlayerEvent.ContainerClosed.class, 
			PlayerEvent.class,
			"event.player.containerclosed",
			"containerClosed",
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerContainerOpen(new EventData(PlayerEvent.ContainerOpen.class, 
			PlayerEvent.class,
			"event.player.containeropen",
			"containerOpen",
			new MetodData(IContainer.class, "container", "parameter.container"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDamaged(new EventData(PlayerEvent.DamagedEvent.class, 
			PlayerEvent.class,
			"event.damaged",
			"damaged",
			new MetodData(boolean.class, "clearTarget", "event.cleartarget"),
			new MetodData(float.class, "damage", "parameter.value"),
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDamagedEntity(new EventData(PlayerEvent.DamagedEntityEvent.class, 
			PlayerEvent.class,
			"event.damagedentity",
			"damagedEntity",
			new MetodData(float.class, "damage", "parameter.value"),
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "target", "parameter.entity"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDied(new EventData(PlayerEvent.DiedEvent.class, 
			PlayerEvent.class,
			"event.died",
			"died",
			new MetodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MetodData(IEntity.class, "source", "parameter.entity"),
			new MetodData(String.class, "type", "parameter.target.type"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerFactionUpdate(new EventData(PlayerEvent.FactionUpdateEvent.class, 
			PlayerEvent.class,
			"event.player.factionupdate",
			"factionUpdate",
			new MetodData(IFaction.class, "faction", "parameter.faction"),
			new MetodData(boolean.class, "init", "event.player.init"),
			new MetodData(int.class, "points", "parameter.faction.points"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerInit(new EventData(PlayerEvent.InitEvent.class, 
			PlayerEvent.class,
			"event.init",
			"init",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerInteract(new EventData(PlayerEvent.InteractEvent.class, 
			PlayerEvent.class,
			"event.interact",
			"interact",
			new MetodData(Object.class, "target", "parameter.target"),
			new MetodData(int.class, "type", "parameter.target.type"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerItemCrafted(new EventData(PlayerEvent.ItemCrafted.class, 
			PlayerEvent.class,
			"event.player.itemcrafted",
			"itemCrafted",
			new MetodData(IItemStack.class, "crafting", "parameter.stack"),
			new MetodData(IInventory.class, "craftMatrix", "parameter.inventory"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerItemFished(new EventData(PlayerEvent.ItemFished.class, 
			PlayerEvent.class,
			"event.player.itemfished",
			"itemFished",
			new MetodData(int.class, "rodDamage", "event.player.roddamage"),
			new MetodData(IItemStack[].class, "stacks", "parameter.stack"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKeyDown(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.key.down",
			"keyDown",
			new MetodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MetodData(int.class, "key", "parameter.keyboard.key"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKeyPressed(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.key.up",
			"keyPressed",
			new MetodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MetodData(int.class, "key", "parameter.keyboard.key"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKill(new EventData(PlayerEvent.KilledEntityEvent.class, 
			PlayerEvent.class,
			"event.killedentity",
			"kill",
			new MetodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLevelUp(new EventData(PlayerEvent.LevelUpEvent.class, 
			PlayerEvent.class,
			"event.player.levelup",
			"levelUp",
			new MetodData(int.class, "change", "event.player.level"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLogin(new EventData(PlayerEvent.LoginEvent.class, 
			PlayerEvent.class,
			"event.player.login",
			"login",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLogout(new EventData(PlayerEvent.LogoutEvent.class, 
			PlayerEvent.class,
			"event.player.logout",
			"logout",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerMouseDown(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.mouse.down",
			"mouseDown",
			new MetodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MetodData(int.class, "key", "parameter.mouse.key"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerMousePressed(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.mouse.up",
			"mousePressed",
			new MetodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MetodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MetodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MetodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MetodData(int.class, "key", "parameter.mouse.key"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPickUp(new EventData(PlayerEvent.PickUpEvent.class, 
			PlayerEvent.class,
			"event.player.pickup",
			"pickUp",
			new MetodData(IItemStack.class, "item", "parameter.stack"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPlased(new EventData(PlayerEvent.PlaceEvent.class, 
			PlayerEvent.class,
			"event.player.place",
			"plased",
			new MetodData(IBlock.class, "block", "parameter.block"),
			new MetodData(int.class, "exp", "parameter.exp"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerRangedLaunched(new EventData(PlayerEvent.RangedLaunchedEvent.class, 
			PlayerEvent.class,
			"event.rangedlaunched",
			"rangedLaunched",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerSoundPlayed(new EventData(PlayerEvent.PlayerSound.class, 
			PlayerEvent.class,
			"event.player.sound.play",
			"soundPlayed",
			new MetodData(String.class, "name", "parameter.sound.name"),
			new MetodData(String.class, "resource", "parameter.resource"),
			new MetodData(String.class, "category", "parameter.sound.type"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(float.class, "volume", "parameter.sound.volume"),
			new MetodData(float.class, "pitch", "parameter.sound.pitch"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerSoundStoped(new EventData(PlayerEvent.PlayerSound.class, 
			PlayerEvent.class,
			"event.player.sound.stop",
			"soundStoped",
			new MetodData(String.class, "name", "parameter.sound.name"),
			new MetodData(String.class, "resource", "parameter.resource"),
			new MetodData(String.class, "category", "parameter.sound.type"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(float.class, "volume", "parameter.sound.volume"),
			new MetodData(float.class, "pitch", "parameter.sound.pitch"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerTick(new EventData(PlayerEvent.UpdateEvent.class, 
			PlayerEvent.class,
			"event.update",
			"tick",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerTimer(new EventData(PlayerEvent.TimerEvent.class, 
			PlayerEvent.class,
			"event.timer",
			"timer",
			new MetodData(int.class, "id", "parameter.itimers.id"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerToss(new EventData(PlayerEvent.TossEvent.class, 
			PlayerEvent.class,
			"event.player.toss",
			"toss",
			new MetodData(IItemStack.class, "item", "parameter.stack"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ProjectileProjectileImpact(new EventData(ProjectileEvent.ImpactEvent.class, 
			ProjectileEvent.class,
			"event.projectile.impact",
			"projectileImpact",
			new MetodData(Object.class, "target", "parameter.target"),
			new MetodData(int.class, "type", "parameter.target.type"),
			new MetodData(IProjectile.class, "projectile", "parameter.projectile"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ProjectileProjectileTick(new EventData(ProjectileEvent.UpdateEvent.class, 
			ProjectileEvent.class,
			"event.update",
			"projectileTick",
			new MetodData(IProjectile.class, "projectile", "parameter.projectile"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestCanceled(new EventData(QuestEvent.QuestCanceledEvent.class, 
			QuestEvent.class,
			"event.quest.questcanceled",
			"questCanceled",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "event.quest.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestCompleted(new EventData(QuestEvent.QuestCompletedEvent.class, 
			QuestEvent.class,
			"event.quest.questcompleted",
			"questCompleted",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "event.quest.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestStart(new EventData(QuestEvent.QuestStartEvent.class, 
			QuestEvent.class,
			"event.quest.queststart",
			"questStart",
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "event.quest.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestTurnIn(new EventData(QuestEvent.QuestTurnedInEvent.class, 
			QuestEvent.class,
			"event.quest.questturnedin",
			"questTurnIn",
			new MetodData(int.class, "expReward", "parameter.exp"),
			new MetodData(IItemStack[].class, "itemRewards", "event.quest.itemrewards"),
			new MetodData(FactionOptions.class, "factionOptions", "parameter.faction.points"),
			new MetodData(PlayerMail.class, "mail", "event.quest.mail"),
			new MetodData(int.class, "nextQuestId", "event.quest.nextquestid"),
			new MetodData(String.class, "command", "event.quest.command"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(IQuest.class, "quest", "parameter.quest"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleBankUnlocked(new EventData(RoleEvent.BankUnlockedEvent.class, 
			RoleEvent.class,
			"event.role.bankunlocked",
			"BankUnlocked",
			new MetodData(int.class, "slot", "parameter.slot"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleBankUpgraded(new EventData(RoleEvent.BankUpgradedEvent.class, 
			RoleEvent.class,
			"event.role.bankupgraded",
			"BankUpgraded",
			new MetodData(int.class, "slot", "parameter.slot"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleFollowerFinished(new EventData(RoleEvent.FollowerFinishedEvent.class, 
			RoleEvent.class,
			"event.role.followerfinished",
			"FollowerFinished",
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleFollowerHire(new EventData(RoleEvent.FollowerHireEvent.class, 
			RoleEvent.class,
			"event.role.followerhire",
			"FollowerHire",
			new MetodData(int.class, "days", "event.role.days"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleMailman(new EventData(RoleEvent.MailmanEvent.class, 
			RoleEvent.class,
			"event.role.mailman",
			"Mailman",
			new MetodData(IPlayerMail.class, "mail", "parameter.mail"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTradeFailed(new EventData(RoleEvent.TradeFailedEvent.class, 
			RoleEvent.class,
			"event.role.tradefailed",
			"TradeFailed",
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
			"Trader",
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
			"TransporterUnlocked",
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTransporterUse(new EventData(RoleEvent.TransporterUseEvent.class, 
			RoleEvent.class,
			"event.role.transporteruse",
			"TransporterUse",
			new MetodData(ITransportLocation.class, "location", "event.role.location"),
			new MetodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MetodData(IPlayer.class, "player", "parameter.player"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	WorldScriptCommand(new EventData(WorldEvent.ScriptCommandEvent.class, 
			WorldEvent.class,
			"event.world.scriptcommand",
			"scriptCommand",
			new MetodData(String[].class, "arguments", "parameter.command.arguments"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(IWorld.class, "world", "parameter.world"),
			new MetodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	WorldTrigger(new EventData(WorldEvent.ScriptTriggerEvent.class, 
			WorldEvent.class,
			"event.world.scripttrigger",
			"trigger",
			new MetodData(Object[].class, "arguments", "event.trigger.arguments"),
			new MetodData(IPos.class, "pos", "parameter.pos"),
			new MetodData(IEntity.class, "entity", "parameter.entity"),
			new MetodData(int.class, "id", "parameter.trigger.id"),
			new MetodData(IWorld.class, "world", "parameter.world"),
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
