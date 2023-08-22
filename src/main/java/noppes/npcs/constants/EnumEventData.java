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
import noppes.npcs.api.entity.data.ILine;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.role.IRoleTransporter.ITransportLocation;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PackageReceived;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.ProjectileEvent;
import noppes.npcs.api.event.QuestEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.api.event.potion.AffectEntity;
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
import noppes.npcs.client.util.MethodData;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.PlayerMail;

public enum EnumEventData {
	
	AffectEntity(new EventData(AffectEntity.class, 
			null,
			"event.potion.affectentity",
			EnumScriptType.POTION_AFFECT.function, 
			new MethodData(IEntity.class, "source", "parameter.entity"),
			new MethodData(IEntity.class, "indirectSource", "parameter.entity"),
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MethodData(double.class, "health", "parameter.health"),
			new MethodData(ICustomElement.class, "potion", "event.potion.type"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockBroken(new EventData(BlockEvent.BreakEvent.class, 
			BlockEvent.class,
			"event.block.break",
			EnumScriptType.BROKEN.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockClicked(new EventData(BlockEvent.ClickedEvent.class, 
			BlockEvent.class,
			"event.block.clicked",
			EnumScriptType.CLICKED.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockCollided(new EventData(BlockEvent.CollidedEvent.class, 
			BlockEvent.class,
			"event.block.collided",
			EnumScriptType.COLLIDE.function, 
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockDoorToggle(new EventData(BlockEvent.DoorToggleEvent.class, 
			BlockEvent.class,
			"event.block.doortoggle",
			EnumScriptType.DOOR_TOGGLE.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockFallenUpon(new EventData(BlockEvent.EntityFallenUponEvent.class, 
			BlockEvent.class,
			"event.block.entityfallenupon",
			EnumScriptType.FALLEN_UPON.function, 
			new MethodData(float.class, "distanceFallen", "event.block.distancefallen"),
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockExploded(new EventData(BlockEvent.ExplodedEvent.class, 
			BlockEvent.class,
			"event.block.exploded",
			EnumScriptType.EXPLODED.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockHarvested(new EventData(BlockEvent.HarvestedEvent.class, 
			BlockEvent.class,
			"event.block.harvested",
			EnumScriptType.HARVESTED.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockInit(new EventData(BlockEvent.InitEvent.class, 
			BlockEvent.class,
			"event.init",
			EnumScriptType.INIT.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockInteract(new EventData(BlockEvent.InteractEvent.class, 
			BlockEvent.class,
			"event.interact",
			EnumScriptType.INTERACT.function, 
			new MethodData(float.class, "hitX", "event.block.hitx"),
			new MethodData(float.class, "hitY", "event.block.hity"),
			new MethodData(float.class, "hitZ", "event.block.hitz"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(int.class, "side", "event.block.side"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockNeighborChanged(new EventData(BlockEvent.NeighborChangedEvent.class, 
			BlockEvent.class,
			"event.block.neighborchanged",
			EnumScriptType.NEIGHBOR_CHANGED.function, 
			new MethodData(IPos.class, "changedPos", "parameter.pos"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockRainFill(new EventData(BlockEvent.RainFillEvent.class, 
			BlockEvent.class,
			"event.block.rainfill",
			EnumScriptType.RAIN_FILLED.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockRedstone(new EventData(BlockEvent.RedstoneEvent.class, 
			BlockEvent.class,
			"event.block.redstone",
			EnumScriptType.REDSTONE.function, 
			new MethodData(int.class, "power", "event.block.power"),
			new MethodData(int.class, "prevPower", "event.block.prevpower"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockTimer(new EventData(BlockEvent.TimerEvent.class, 
			BlockEvent.class,
			"event.timer",
			EnumScriptType.TIMER.function, 
			new MethodData(int.class, "id", "parameter.itimers.id"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	BlockUpdate(new EventData(BlockEvent.UpdateEvent.class, 
			BlockEvent.class,
			"event.update",
			EnumScriptType.TICK.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomContainerClose(new EventData(CustomContainerEvent.CloseEvent.class, 
			CustomContainerEvent.class,
			"event.customcontainer.close",
			EnumScriptType.CUSTOM_CHEST_CLOSED.function, 
			new MethodData(IContainer.class, "container", "parameter.container"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomContainerSlotClicked(new EventData(CustomContainerEvent.SlotClickedEvent.class, 
			CustomContainerEvent.class,
			"event.customcontainer.slotclicked",
			EnumScriptType.CUSTOM_CHEST_CLICKED.function, 
			new MethodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MethodData(int.class, "slot", "parameter.slot"),
			new MethodData(IItemStack.class, "slotItem", "parameter.stack"),
			new MethodData(IContainer.class, "container", "parameter.container"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiButton(new EventData(CustomGuiEvent.ButtonEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.button",
			EnumScriptType.CUSTOM_GUI_BUTTON.function, 
			new MethodData(int.class, "buttonId", "event.customgui.buttonid"),
			new MethodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiClose(new EventData(CustomGuiEvent.CloseEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.close",
			EnumScriptType.CUSTOM_GUI_CLOSED.function, 
			new MethodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiScroll(new EventData(CustomGuiEvent.ScrollEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.scroll",
			EnumScriptType.CUSTOM_GUI_SCROLL.function, 
			new MethodData(boolean.class, "doubleClick", "event.customgui.doubleclick"),
			new MethodData(int.class, "scrollId", "event.customgui.scrollid"),
			new MethodData(int.class, "scrollIndex", "event.customgui.scrollindex"),
			new MethodData(String[].class, "selection", "event.customgui.selection"),
			new MethodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiSlotClick(new EventData(CustomGuiEvent.SlotClickEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.slotclick",
			EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function, 
			new MethodData(String.class, "clickType", "event.customgui.clicktype"),
			new MethodData(int.class, "dragType", "event.customgui.dragtype"),
			new MethodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MethodData(int.class, "slotId", "parameter.slot"),
			new MethodData(IItemStack.class, "stack", "parameter.stack"),
			new MethodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	CustomGuiSlot(new EventData(CustomGuiEvent.SlotEvent.class, 
			CustomGuiEvent.class,
			"event.customgui.slot",
			EnumScriptType.CUSTOM_GUI_SLOT.function, 
			new MethodData(IItemStack.class, "heldItem", "parameter.stack"),
			new MethodData(int.class, "slotId", "parameter.slot"),
			new MethodData(IItemStack.class, "stack", "parameter.stack"),
			new MethodData(ICustomGui.class, "gui", "parameter.customgui"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogClose(new EventData(DialogEvent.CloseEvent.class, 
			DialogEvent.class,
			"event.dialog.close",
			EnumScriptType.DIALOG_CLOSE.function, 
			new MethodData(IDialog.class, "dialog", "parameter.dialog"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogOpen(new EventData(DialogEvent.OpenEvent.class, 
			DialogEvent.class,
			"event.dialog.open",
			EnumScriptType.DIALOG.function, 
			new MethodData(IDialog.class, "dialog", "parameter.dialog"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	DialogOption(new EventData(DialogEvent.OptionEvent.class, 
			DialogEvent.class,
			"event.dialog.option",
			EnumScriptType.DIALOG_OPTION.function, 
			new MethodData(IDialogOption.class, "option", "parameter.dialog.option"),
			new MethodData(IDialog.class, "dialog", "parameter.dialog"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	EndEffect(new EventData(EndEffect.class, 
			null,
			"event.potion.endeffect",
			EnumScriptType.POTION_END.function, 
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MethodData(ICustomElement.class, "potion", "event.potion.type"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	Forge(new EventData(ForgeEvent.class, 
			null,
			"event.forge",
			"any_forge_event", 
			new MethodData(NpcAPI.class, "API", "event.npcapi"),
			new MethodData(Event.class, "event", "parameter.event"),
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IWorld.class, "world", "parameter.world"),
			new MethodData(IPos.class, "pos", "parameter.pos"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(IItemStack.class, "stack", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ForgeSoundTick(new EventData(ForgeEvent.SoundTickEvent.class, 
			ForgeEvent.class,
			"event.forge.soundtick",
			EnumScriptType.SOUND_TICK_EVENT.function, 
			new MethodData(float.class, "milliSeconds", "parameter.event.wait.long"),
			new MethodData(float.class, "totalSecond", "parameter.event.wait.long"),
			new MethodData(String.class, "name", "parameter.sound.name"),
			new MethodData(String.class, "resource", "parameter.sound.res"),
			new MethodData(float.class, "volume", "parameter.sound.volume"),
			new MethodData(float.class, "pitch", "parameter.sound.pitch"),
			new MethodData(NpcAPI.class, "API", "event.npcapi"),
			new MethodData(Event.class, "event", "parameter.event"),
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IWorld.class, "world", "parameter.world"),
			new MethodData(IPos.class, "pos", "parameter.pos"),
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(IItemStack.class, "stack", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	IsReady(new EventData(IsReadyEvent.class, 
			null,
			"event.potion.isreadyevent",
			EnumScriptType.POTION_IS_READY.function, 
			new MethodData(boolean.class, "ready", "parameter.potion.ready"),
			new MethodData(int.class, "duration", "parameter.potion.duration"),
			new MethodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MethodData(ICustomElement.class, "potion", "event.potion.type"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemAttack(new EventData(ItemEvent.AttackEvent.class, 
			ItemEvent.class,
			"event.item.attack",
			EnumScriptType.ATTACK.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(Object.class, "target", "event.item.target"),
			new MethodData(int.class, "type", "parameter.target.type"),
			new MethodData(IItemScripted.class, "item", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemInit(new EventData(ItemEvent.InitEvent.class, 
			ItemEvent.class,
			"event.init",
			EnumScriptType.INIT.function, 
			new MethodData(IItemScripted.class, "item", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemInteract(new EventData(ItemEvent.InteractEvent.class, 
			ItemEvent.class,
			"event.interact",
			EnumScriptType.INTERACT.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(Object.class, "target", "parameter.target"),
			new MethodData(int.class, "type", "parameter.target.type"),
			new MethodData(IItemScripted.class, "item", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemPickedUp(new EventData(ItemEvent.PickedUpEvent.class, 
			ItemEvent.class,
			"event.item.pickedup",
			EnumScriptType.PICKEDUP.function, 
			new MethodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IItemScripted.class, "item", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemSpawn(new EventData(ItemEvent.SpawnEvent.class, 
			ItemEvent.class,
			"event.item.spawn",
			EnumScriptType.SPAWN.function, 
			new MethodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MethodData(IItemScripted.class, "item", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemTossed(new EventData(ItemEvent.TossedEvent.class, 
			ItemEvent.class,
			"event.item.tossed",
			EnumScriptType.TOSSED.function, 
			new MethodData(IEntityItem.class, "entity", "parameter.entity.item"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IItemScripted.class, "item", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ItemUpdate(new EventData(ItemEvent.UpdateEvent.class, 
			ItemEvent.class,
			"event.update",
			EnumScriptType.TICK.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IItemScripted.class, "item", "parameter.stack"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcCollide(new EventData(NpcEvent.CollideEvent.class, 
			NpcEvent.class,
			"event.npc.collide",
			EnumScriptType.COLLIDE.function, 
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcDamaged(new EventData(NpcEvent.DamagedEvent.class, 
			NpcEvent.class,
			"event.damaged",
			EnumScriptType.DAMAGED.function, 
			new MethodData(boolean.class, "clearTarget", "event.cleartarget"),
			new MethodData(float.class, "damage", "parameter.value"),
			new MethodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MethodData(IEntity.class, "source", "parameter.entity"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcDied(new EventData(NpcEvent.DiedEvent.class, 
			NpcEvent.class,
			"event.died",
			EnumScriptType.DIED.function, 
			new MethodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MethodData(IItemStack[].class, "droppedItems", "event.npc.droppeditems"),
			new MethodData(int.class, "expDropped", "parameter.exp"),
			new MethodData(ILine.class, "line", "event.npc.line"),
			new MethodData(IItemStack[].class, "lootedItems", "event.npc.looteditems"),
			new MethodData(IEntity.class, "source", "parameter.entity"),
			new MethodData(String.class, "type", "event.npc.damagetype"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcInit(new EventData(NpcEvent.InitEvent.class, 
			NpcEvent.class,
			"event.init",
			EnumScriptType.INIT.function, 
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcInteract(new EventData(NpcEvent.InteractEvent.class, 
			NpcEvent.class,
			"event.interact",
			EnumScriptType.INTERACT.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcKilledEntity(new EventData(NpcEvent.KilledEntityEvent.class, 
			NpcEvent.class,
			"event.killedentity",
			EnumScriptType.KILL.function, 
			new MethodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcMeleeAttack(new EventData(NpcEvent.MeleeAttackEvent.class, 
			NpcEvent.class,
			"event.npc.meleeattack",
			EnumScriptType.ATTACK_MELEE.function, 
			new MethodData(float.class, "damage", "parameter.value"),
			new MethodData(IEntityLivingBase.class, "target", "parameter.entity"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcRangedLaunched(new EventData(NpcEvent.RangedLaunchedEvent.class, 
			NpcEvent.class,
			"event.rangedlaunched",
			EnumScriptType.RANGED_LAUNCHED.function, 
			new MethodData(float.class, "damage", "parameter.value"),
			new MethodData(List.class, "projectiles", "event.npc.projectiles"),
			new MethodData(IEntityLivingBase.class, "target", "parameter.entity"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTarget(new EventData(NpcEvent.TargetEvent.class, 
			NpcEvent.class,
			"event.npc.target",
			EnumScriptType.TARGET.function, 
			new MethodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTargetLost(new EventData(NpcEvent.TargetLostEvent.class, 
			NpcEvent.class,
			"event.npc.targetlost",
			EnumScriptType.TARGET_LOST.function, 
			new MethodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcTimer(new EventData(NpcEvent.TimerEvent.class, 
			NpcEvent.class,
			"event.timer",
			EnumScriptType.TIMER.function, 
			new MethodData(int.class, "id", "parameter.itimers.id"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	NpcUpdate(new EventData(NpcEvent.UpdateEvent.class, 
			NpcEvent.class,
			"event.update",
			EnumScriptType.TICK.function, 
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PackageReceived(new EventData(PackageReceived.class, 
			null,
			"event.api.packagereceived",
			EnumScriptType.PACKEGE_FROM.function, 
			new MethodData(ChannelHandlerContext.class, "channel", "parameter.channel"),
			new MethodData(boolean.class, "side", "parameter.side"),
			new MethodData(Object.class, "message", "parameter.package.message"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PerformEffect(new EventData(PerformEffect.class, 
			null,
			"event.potion.performeffect",
			EnumScriptType.POTION_PERFORM.function, 
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(int.class, "amplifier", "parameter.potion.amplifier"),
			new MethodData(ICustomElement.class, "potion", "event.potion.type"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerAttack(new EventData(PlayerEvent.AttackEvent.class, 
			PlayerEvent.class,
			"event.player.attack",
			EnumScriptType.ATTACK.function, 
			new MethodData(Object.class, "target", "parameter.target"),
			new MethodData(int.class, "type", "parameter.target.type"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerBroken(new EventData(PlayerEvent.BreakEvent.class, 
			PlayerEvent.class,
			"event.player.break",
			EnumScriptType.BROKEN.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(int.class, "exp", "parameter.exp"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerChat(new EventData(PlayerEvent.ChatEvent.class, 
			PlayerEvent.class,
			"event.player.chat",
			EnumScriptType.CHAT.function, 
			new MethodData(String.class, "message", "parameter.message"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerContainerClosed(new EventData(PlayerEvent.ContainerClosed.class, 
			PlayerEvent.class,
			"event.player.containerclosed",
			EnumScriptType.CONTAINER_CLOSED.function, 
			new MethodData(IContainer.class, "container", "parameter.container"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerContainerOpen(new EventData(PlayerEvent.ContainerOpen.class, 
			PlayerEvent.class,
			"event.player.containeropen",
			EnumScriptType.CONTAINER_OPEN.function, 
			new MethodData(IContainer.class, "container", "parameter.container"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerCustomTeleport(new EventData(PlayerEvent.CustomTeleport.class, 
			PlayerEvent.class,
			"event.customteleport",
			EnumScriptType.CUSTOM_TELEPORT.function, 
			new MethodData(IPos.class, "pos", "parameter.pos"),
			new MethodData(IPos.class, "portal", "parameter.pos"),
			new MethodData(int.class, "dimension", "parameter.dimension.id"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDamagedEntity(new EventData(PlayerEvent.DamagedEntityEvent.class, 
			PlayerEvent.class,
			"event.damagedentity",
			EnumScriptType.DAMAGED_ENTITY.function, 
			new MethodData(float.class, "damage", "parameter.value"),
			new MethodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MethodData(IEntity.class, "target", "parameter.entity"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDamaged(new EventData(PlayerEvent.DamagedEvent.class, 
			PlayerEvent.class,
			"event.damaged",
			EnumScriptType.DAMAGED.function, 
			new MethodData(boolean.class, "clearTarget", "event.cleartarget"),
			new MethodData(float.class, "damage", "parameter.value"),
			new MethodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MethodData(IEntity.class, "source", "parameter.entity"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerDied(new EventData(PlayerEvent.DiedEvent.class, 
			PlayerEvent.class,
			"event.died",
			EnumScriptType.DIED.function, 
			new MethodData(IDamageSource.class, "damageSource", "parameter.damagesource"),
			new MethodData(IEntity.class, "source", "parameter.entity"),
			new MethodData(String.class, "type", "parameter.target.type"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerFactionUpdate(new EventData(PlayerEvent.FactionUpdateEvent.class, 
			PlayerEvent.class,
			"event.player.factionupdate",
			EnumScriptType.FACTION_UPDATE.function, 
			new MethodData(IFaction.class, "faction", "parameter.faction"),
			new MethodData(boolean.class, "init", "parameter.faction.init"),
			new MethodData(int.class, "points", "parameter.faction.points"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerInit(new EventData(PlayerEvent.InitEvent.class, 
			PlayerEvent.class,
			"event.init",
			EnumScriptType.INIT.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerInteract(new EventData(PlayerEvent.InteractEvent.class, 
			PlayerEvent.class,
			"event.interact",
			EnumScriptType.INTERACT.function, 
			new MethodData(Object.class, "target", "parameter.target"),
			new MethodData(int.class, "type", "parameter.target.type"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerItemCrafted(new EventData(PlayerEvent.ItemCrafted.class, 
			PlayerEvent.class,
			"event.player.itemcrafted",
			EnumScriptType.ITEM_CRAFTED.function, 
			new MethodData(IItemStack.class, "crafting", "parameter.stack"),
			new MethodData(IInventory.class, "craftMatrix", "parameter.inventory"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerItemFished(new EventData(PlayerEvent.ItemFished.class, 
			PlayerEvent.class,
			"event.player.itemfished",
			EnumScriptType.ITEM_FISHED.function, 
			new MethodData(int.class, "rodDamage", "event.player.roddamage"),
			new MethodData(IItemStack[].class, "stacks", "parameter.stack"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKeyPressed(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.key.down",
			EnumScriptType.KEY_DOWN.function, 
			new MethodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MethodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MethodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MethodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MethodData(int.class, "key", "parameter.keyboard.key"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKilledEntity(new EventData(PlayerEvent.KilledEntityEvent.class, 
			PlayerEvent.class,
			"event.killedentity",
			EnumScriptType.KILL.function, 
			new MethodData(IEntityLivingBase.class, "entity", "parameter.entity"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLevelUp(new EventData(PlayerEvent.LevelUpEvent.class, 
			PlayerEvent.class,
			"event.player.levelup",
			EnumScriptType.LEVEL_UP.function, 
			new MethodData(int.class, "change", "event.player.level"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLogin(new EventData(PlayerEvent.LoginEvent.class, 
			PlayerEvent.class,
			"event.player.login",
			EnumScriptType.LOGIN.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerLogout(new EventData(PlayerEvent.LogoutEvent.class, 
			PlayerEvent.class,
			"event.player.logout",
			EnumScriptType.LOGOUT.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPickUp(new EventData(PlayerEvent.PickUpEvent.class, 
			PlayerEvent.class,
			"event.player.pickup",
			EnumScriptType.PICKUP.function, 
			new MethodData(IItemStack.class, "item", "parameter.stack"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPlace(new EventData(PlayerEvent.PlaceEvent.class, 
			PlayerEvent.class,
			"event.player.place",
			EnumScriptType.PLASED.function, 
			new MethodData(IBlock.class, "block", "parameter.block"),
			new MethodData(int.class, "exp", "parameter.exp"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerPlayerPackage(new EventData(PlayerEvent.PlayerPackage.class, 
			PlayerEvent.class,
			"event.player.playerpackage",
			EnumScriptType.PACKEGE_FROM.function, 
			new MethodData(INbt.class, "nbt", "parameter.nbt"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerRangedLaunched(new EventData(PlayerEvent.RangedLaunchedEvent.class, 
			PlayerEvent.class,
			"event.rangedlaunched",
			EnumScriptType.RANGED_LAUNCHED.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerTimer(new EventData(PlayerEvent.TimerEvent.class, 
			PlayerEvent.class,
			"event.timer",
			EnumScriptType.TIMER.function, 
			new MethodData(int.class, "id", "parameter.itimers.id"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerToss(new EventData(PlayerEvent.TossEvent.class, 
			PlayerEvent.class,
			"event.player.toss",
			EnumScriptType.TOSS.function, 
			new MethodData(IItemStack.class, "item", "parameter.stack"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerUpdate(new EventData(PlayerEvent.UpdateEvent.class, 
			PlayerEvent.class,
			"event.update",
			EnumScriptType.TICK.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerKeyDown(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.key.down",
			EnumScriptType.KEY_DOWN.function, 
			new MethodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MethodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MethodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MethodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MethodData(int.class, "key", "parameter.keyboard.key"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerMouseDown(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.key.down",
			EnumScriptType.KEY_DOWN.function, 
			new MethodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MethodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MethodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MethodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MethodData(int.class, "key", "parameter.keyboard.key"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerMousePressed(new EventData(PlayerEvent.KeyPressedEvent.class, 
			PlayerEvent.class,
			"event.player.key.down",
			EnumScriptType.KEY_DOWN.function, 
			new MethodData(boolean.class, "isAltPressed", "parameter.isaltpressed"),
			new MethodData(boolean.class, "isCtrlPressed", "parameter.isctrlpressed"),
			new MethodData(boolean.class, "isMetaPressed", "parameter.ismetapressed"),
			new MethodData(boolean.class, "isShiftPressed", "parameter.isshiftpressed"),
			new MethodData(int.class, "key", "parameter.keyboard.key"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerSoundPlay(new EventData(PlayerEvent.PlayerSound.class, 
			PlayerEvent.class,
			"event.player.sound.play",
			EnumScriptType.SOUND_PLAY.function, 
			new MethodData(String.class, "name", "parameter.sound.name"),
			new MethodData(String.class, "resource", "parameter.resource"),
			new MethodData(String.class, "category", "parameter.sound.type"),
			new MethodData(IPos.class, "pos", "parameter.pos"),
			new MethodData(float.class, "volume", "parameter.sound.volume"),
			new MethodData(float.class, "pitch", "parameter.sound.pitch"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	PlayerSoundStop(new EventData(PlayerEvent.PlayerSound.class, 
			PlayerEvent.class,
			"event.player.sound.stop",
			EnumScriptType.SOUND_STOP.function, 
			new MethodData(String.class, "name", "parameter.sound.name"),
			new MethodData(String.class, "resource", "parameter.resource"),
			new MethodData(String.class, "category", "parameter.sound.type"),
			new MethodData(IPos.class, "pos", "parameter.pos"),
			new MethodData(float.class, "volume", "parameter.sound.volume"),
			new MethodData(float.class, "pitch", "parameter.sound.pitch"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ProjectileImpact(new EventData(ProjectileEvent.ImpactEvent.class, 
			ProjectileEvent.class,
			"event.projectile.impact",
			EnumScriptType.PROJECTILE_IMPACT.function, 
			new MethodData(Object.class, "target", "parameter.target"),
			new MethodData(int.class, "type", "parameter.target.type"),
			new MethodData(IProjectile.class, "projectile", "parameter.projectile"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	ProjectileUpdate(new EventData(ProjectileEvent.UpdateEvent.class, 
			ProjectileEvent.class,
			"event.update",
			EnumScriptType.PROJECTILE_TICK.function, 
			new MethodData(IProjectile.class, "projectile", "parameter.projectile"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestCanceled(new EventData(QuestEvent.QuestCanceledEvent.class, 
			QuestEvent.class,
			"event.quest.questcanceled",
			EnumScriptType.QUEST_CANCELED.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IQuest.class, "quest", "parameter.quest"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestCompleted(new EventData(QuestEvent.QuestCompletedEvent.class, 
			QuestEvent.class,
			"event.quest.questcompleted",
			EnumScriptType.QUEST_COMPLETED.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IQuest.class, "quest", "parameter.quest"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestStart(new EventData(QuestEvent.QuestStartEvent.class, 
			QuestEvent.class,
			"event.quest.queststart",
			EnumScriptType.QUEST_START.function, 
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IQuest.class, "quest", "parameter.quest"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	QuestQuestTurnedIn(new EventData(QuestEvent.QuestTurnedInEvent.class, 
			QuestEvent.class,
			"event.quest.questturnedin",
			EnumScriptType.QUEST_TURNIN.function, 
			new MethodData(int.class, "expReward", "parameter.exp"),
			new MethodData(IItemStack[].class, "itemRewards", "event.quest.itemrewards"),
			new MethodData(FactionOptions.class, "factionOptions", "parameter.faction.points"),
			new MethodData(PlayerMail.class, "mail", "event.quest.mail"),
			new MethodData(int.class, "nextQuestId", "event.quest.nextquestid"),
			new MethodData(String.class, "command", "event.quest.command"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(IQuest.class, "quest", "parameter.quest"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleBankUnlocked(new EventData(RoleEvent.BankUnlockedEvent.class, 
			RoleEvent.class,
			"event.role.bankunlocked",
			EnumScriptType.ROLE.function, 
			new MethodData(int.class, "slot", "parameter.slot"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleBankUpgraded(new EventData(RoleEvent.BankUpgradedEvent.class, 
			RoleEvent.class,
			"event.role.bankupgraded",
			EnumScriptType.ROLE.function, 
			new MethodData(int.class, "slot", "parameter.slot"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleFollowerFinished(new EventData(RoleEvent.FollowerFinishedEvent.class, 
			RoleEvent.class,
			"event.role.followerfinished",
			EnumScriptType.ROLE.function, 
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleFollowerHire(new EventData(RoleEvent.FollowerHireEvent.class, 
			RoleEvent.class,
			"event.role.followerhire",
			EnumScriptType.ROLE.function, 
			new MethodData(int.class, "days", "event.role.days"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleMailman(new EventData(RoleEvent.MailmanEvent.class, 
			RoleEvent.class,
			"event.role.mailman",
			EnumScriptType.ROLE.function, 
			new MethodData(IPlayerMail.class, "mail", "parameter.mail"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTradeFailed(new EventData(RoleEvent.TradeFailedEvent.class, 
			RoleEvent.class,
			"event.role.tradefailed",
			EnumScriptType.ROLE.function, 
			new MethodData(IItemStack.class, "currency1", "parameter.stack"),
			new MethodData(IItemStack.class, "currency2", "parameter.stack"),
			new MethodData(IItemStack.class, "receiving", "parameter.stack"),
			new MethodData(IItemStack.class, "sold", "parameter.stack"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTrader(new EventData(RoleEvent.TraderEvent.class, 
			RoleEvent.class,
			"event.role.trader",
			EnumScriptType.ROLE.function, 
			new MethodData(IItemStack.class, "currency1", "parameter.stack"),
			new MethodData(IItemStack.class, "currency2", "parameter.stack"),
			new MethodData(IItemStack.class, "sold", "parameter.stack"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTransporterUnlocked(new EventData(RoleEvent.TransporterUnlockedEvent.class, 
			RoleEvent.class,
			"event.role.transporterunlocked",
			EnumScriptType.ROLE.function, 
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	RoleTransporterUse(new EventData(RoleEvent.TransporterUseEvent.class, 
			RoleEvent.class,
			"event.role.transporteruse",
			EnumScriptType.ROLE.function, 
			new MethodData(ITransportLocation.class, "location", "event.role.location"),
			new MethodData(ICustomNpc.class, "npc", "parameter.npc"),
			new MethodData(IPlayer.class, "player", "parameter.player"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	WorldScriptCommand(new EventData(WorldEvent.ScriptCommandEvent.class, 
			WorldEvent.class,
			"event.world.scriptcommand",
			EnumScriptType.SCRIPT_COMMAND.function, 
			new MethodData(String[].class, "arguments", "parameter.command.arguments"),
			new MethodData(IPos.class, "pos", "parameter.pos"),
			new MethodData(IWorld.class, "world", "parameter.world"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
		)
	),
	WorldScriptTrigger(new EventData(WorldEvent.ScriptTriggerEvent.class, 
			WorldEvent.class,
			"event.world.scripttrigger",
			EnumScriptType.SCRIPT_TRIGGER.function, 
			new MethodData(Object[].class, "arguments", "event.trigger.arguments"),
			new MethodData(IPos.class, "pos", "parameter.pos"),
			new MethodData(IEntity.class, "entity", "parameter.entity"),
			new MethodData(int.class, "id", "parameter.trigger.id"),
			new MethodData(IWorld.class, "world", "parameter.world"),
			new MethodData(NpcAPI.class, "API", "event.npcapi")
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

	public static String get(Class<?> event) {
		for (EnumEventData enumED : EnumEventData.values()) {
			if (enumED.ed!=null && event==enumED.ed.event) { return enumED.ed.func;}
		}
		return "";
	}

	public static EventData get(String parent, String name) {
		for (EnumEventData enumED : EnumEventData.values()) {
			if (parent==null) {
				if (enumED.ed.event.getSimpleName().equals(name)) { return enumED.ed; }
			}
			else if (enumED.ed.event.getSimpleName().equals(name) && enumED.ed.event.getSuperclass().getSimpleName().equals(parent)) { return enumED.ed; }
		}
		return null;
	}
	
}
