package noppes.npcs.api.event;

import javax.annotation.Nonnull;

import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.*;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.handler.data.IKeySetting;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;

import java.util.Objects;

public class PlayerEvent extends CustomNPCsEvent {

	@Cancelable
	@EventName(EnumScriptType.ATTACK)
	public static class AttackEvent extends PlayerEvent {
		public Object target;
		public int type;

		public AttackEvent(IPlayer<?> player, int type, Object target) {
			super(player);
			this.type = type;
			this.target = target;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.BROKEN)
	public static class BreakEvent extends PlayerEvent {
		public IBlock block;
		public int exp;

		public BreakEvent(IPlayer<?> player, IBlock block, int exp) {
			super(player);
			this.block = block;
			this.exp = exp;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.CHAT)
	public static class ChatEvent extends PlayerEvent {
		public String message;

		public ChatEvent(IPlayer<?> player, String message) {
			super(player);
			this.message = message;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.SEND_COMMAND)
	public static class CommandEvent extends PlayerEvent {
		public ICommand command;
		public String[] parameters;

		public CommandEvent(IPlayer<?> player, ICommand command, String[] parameters) {
			super(player);
			this.command = command;
			this.parameters = parameters;
		}
	}

	@EventName(EnumScriptType.CONTAINER_CLOSED)
	public static class ContainerClosed extends PlayerEvent {
		public IContainer container;

		public ContainerClosed(IPlayer<?> player, IContainer container) {
			super(player);
			this.container = container;
		}
	}

	@EventName(EnumScriptType.CONTAINER_OPEN)
	public static class ContainerOpen extends PlayerEvent {
		public IContainer container;

		public ContainerOpen(IPlayer<?> player, IContainer container) {
			super(player);
			this.container = container;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.CUSTOM_TELEPORT)
	public static class CustomTeleport extends PlayerEvent {

		public IPos pos, portal;
		public int dimension;

		public CustomTeleport(IPlayer<?> player, IPos portal, IPos pos, int dimensionID) {
			super(player);
			this.pos = pos;
			this.portal = portal;
			this.dimension = dimensionID;
		}

	}

	@Cancelable
	@EventName(EnumScriptType.DAMAGED_ENTITY)
	public static class DamagedEntityEvent extends PlayerEvent {
		public float damage;
		public IDamageSource damageSource;
		public IEntity<?> target;

		public DamagedEntityEvent(IPlayer<?> player, Entity target, float damage, DamageSource damagesource) {
			super(player);
			this.target = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(target);
			this.damage = damage;
			this.damageSource = Objects.requireNonNull(NpcAPI.Instance()).getIDamageSource(damagesource);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.DAMAGED)
	public static class DamagedEvent extends PlayerEvent {

		public boolean clearTarget;
		public float damage;
		public IDamageSource damageSource;
		public IEntity<?> source;

		public DamagedEvent(IPlayer<?> player, Entity source, float damage, DamageSource damagesource) {
			super(player);
			this.clearTarget = false;
			this.source = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(source);
			this.damage = damage;
			this.damageSource = Objects.requireNonNull(NpcAPI.Instance()).getIDamageSource(damagesource);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.DIED)
	public static class DiedEvent extends PlayerEvent {
		public IDamageSource damageSource;
		public IEntity<?> source;
		public String type;

		public DiedEvent(IPlayer<?> player, DamageSource damagesource, Entity entity) {
			super(player);
			this.type = damagesource.damageType;
			this.source = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
			this.damageSource = Objects.requireNonNull(NpcAPI.Instance()).getIDamageSource(damagesource);
		}
	}

	@EventName(EnumScriptType.FACTION_UPDATE)
	public static class FactionUpdateEvent extends PlayerEvent {
		public IFaction faction;
		public boolean init;
		public int points;

		public FactionUpdateEvent(IPlayer<?> player, IFaction faction, int points, boolean init) {
			super(player);
			this.faction = faction;
			this.points = points;
			this.init = init;
		}
	}

	@EventName(EnumScriptType.INIT)
	public static class InitEvent extends PlayerEvent {
		public InitEvent(IPlayer<?> player) {
			super(player);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.INTERACT)
	public static class InteractEvent extends PlayerEvent {

		public Object target;
		public int type;

		public InteractEvent(IPlayer<?> player, int type, Object target) {
			super(player);
			this.type = type;
			this.target = target;
		}

	}

	@EventName(EnumScriptType.ITEM_CRAFTED)
	public static class ItemCrafted extends PlayerEvent {

		public final IItemStack crafting;
		public final IInventory craftMatrix;

		public ItemCrafted(IPlayer<?> player, @Nonnull IItemStack crafting, IInventory craftMatrix) {
			super(player);
			this.crafting = crafting;
			this.craftMatrix = craftMatrix;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.ITEM_FISHED)
	public static class ItemFished extends PlayerEvent {

		public int rodDamage;
		public IItemStack[] stacks;

		public ItemFished(IPlayer<?> player, NonNullList<ItemStack> drops, int rodDamage) {
			super(player);
			this.stacks = new IItemStack[drops.size()];
			for (int i = 0; i < drops.size(); i++) {
				this.stacks[i] = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(drops.get(i));
			}
			this.rodDamage = rodDamage;
		}
	}

	@EventName(EnumScriptType.KEY_ACTIVE)
	public static class KeyActive extends PlayerEvent {

		public IKeySetting key;
		public int id;

		public KeyActive(IPlayer<?> player, IKeySetting kb) {
			super(player);
			this.key = kb;
		}

	}

	@EventName(EnumScriptType.KEY_DOWN)
	public static class KeyPressedEvent extends PlayerEvent {
		public boolean isAltPressed;
		public boolean isCtrlPressed;
		public boolean isMetaPressed;
		public boolean isShiftPressed;
		public int key;

		public KeyPressedEvent(IPlayer<?> player, int key, boolean isCtrlPressed, boolean isAltPressed, boolean isShiftPressed, boolean isMetaPressed) {
			super(player);
			this.key = key;
			this.isCtrlPressed = isCtrlPressed;
			this.isAltPressed = isAltPressed;
			this.isShiftPressed = isShiftPressed;
			this.isMetaPressed = isMetaPressed;
		}
	}

	@EventName(EnumScriptType.MOUSE_MOVE)
	public static class MouseMoveEvent extends PlayerEvent {
		public boolean isAltPressed;
		public boolean isCtrlPressed;
		public boolean isMetaPressed;
		public boolean isShiftPressed;
		public int posX;
		public int posY;
		public int mouseX;
		public int mouseY;
		public int dWheel;

		public MouseMoveEvent(IPlayer<?> player, int x, int y, int dx, int dy, int wheel, boolean isCtrlPressed, boolean isAltPressed, boolean isShiftPressed, boolean isMetaPressed) {
			super(player);
			posX = x;
			posY = y;
			mouseX = dx;
			mouseY = dy;
			dWheel = wheel;
			this.isCtrlPressed = isCtrlPressed;
			this.isAltPressed = isAltPressed;
			this.isShiftPressed = isShiftPressed;
			this.isMetaPressed = isMetaPressed;
		}
	}

	@EventName(EnumScriptType.KILL)
	public static class KilledEntityEvent extends PlayerEvent {
		public IEntityLivingBase<?> entity;

		public KilledEntityEvent(IPlayer<?> player, EntityLivingBase entity) {
			super(player);
			this.entity = (IEntityLivingBase<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
		}
	}

	@EventName(EnumScriptType.LEVEL_UP)
	public static class LevelUpEvent extends PlayerEvent {
		public int change;

		public LevelUpEvent(IPlayer<?> player, int change) {
			super(player);
			this.change = change;
		}
	}

	@EventName(EnumScriptType.LOGIN)
	public static class LoginEvent extends PlayerEvent {
		public LoginEvent(IPlayer<?> player) {
			super(player);
		}
	}

	@EventName(EnumScriptType.LOGOUT)
	public static class LogoutEvent extends PlayerEvent {
		public LogoutEvent(IPlayer<?> player) {
			super(player);
		}
	}

	@EventName(EnumScriptType.GUI_OPEN)
	public static class OpenGUI extends PlayerEvent {

		public String newGUI, oldGUI;

		public OpenGUI(IPlayer<?> player, String n, String o) {
			super(player);
			this.newGUI = n;
			this.oldGUI = o;
		}

	}

	@Cancelable
	@EventName(EnumScriptType.PICKUP)
	public static class PickUpEvent extends PlayerEvent {
		public IItemStack item;

		public PickUpEvent(IPlayer<?> player, IItemStack item) {
			super(player);
			this.item = item;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.PLEASED)
	public static class PlaceEvent extends PlayerEvent {

		public IBlock block;
		public int exp;

		public PlaceEvent(IPlayer<?> player, IBlock block) {
			super(player);
			this.block = block;
		}

	}

	@EventName(EnumScriptType.PACKAGE_FROM)
	public static class PlayerPackage extends PlayerEvent {

		public INbt nbt;

		public PlayerPackage(IPlayer<?> player, INbt nbt) {
			super(player);
			this.nbt = nbt;
		}

	}

	@EventName(EnumScriptType.SOUND_STOP)
	public static class PlayerSound extends PlayerEvent {

		public String name;
		public String resource;
		public String category;
		public IPos pos;
		public float volume;
		public float pitch;

		public PlayerSound(IPlayer<?> player, String resource, String name, String category, float x, float y, float z, float volume, float pitch) {
			super(player);
			this.name = name;
			this.resource = resource;
			this.category = category;
			this.pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(x, y, z);
			this.volume = volume;
			this.pitch = pitch;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.RANGED_LAUNCHED)
	public static class RangedLaunchedEvent extends PlayerEvent {
		public RangedLaunchedEvent(IPlayer<?> player) {
			super(player);
		}
	}

	@EventName(EnumScriptType.TIMER)
	public static class TimerEvent extends PlayerEvent {
		public int id;

		public TimerEvent(IPlayer<?> player, int id) {
			super(player);
			this.id = id;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.TOSS)
	public static class TossEvent extends PlayerEvent {
		public IItemStack item;

		public TossEvent(IPlayer<?> player, IItemStack item) {
			super(player);
			this.item = item;
		}
	}

	@EventName(EnumScriptType.TICK)
	public static class UpdateEvent extends PlayerEvent {
		public UpdateEvent(IPlayer<?> player) {
			super(player);
		}
	}

	public IPlayer<?> player;

	public PlayerEvent(IPlayer<?> player) {
		this.player = player;
	}

}
