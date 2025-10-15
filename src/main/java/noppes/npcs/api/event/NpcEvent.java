package noppes.npcs.api.event;

import java.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.ai.CombatHandler;
import noppes.npcs.api.EventName;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.ILine;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.AnimationController;

public class NpcEvent extends CustomNPCsEvent {

	@EventName(EnumScriptType.COLLIDE)
	public static class CollideEvent extends NpcEvent {
		public IEntity<?> entity;

		public CollideEvent(ICustomNpc<?> npc, Entity entityIn) {
			super(npc);
			entity = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entityIn);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.CUSTOM_TELEPORT)
	public static class CustomNpcTeleport extends NpcEvent {

		public IPos pos, portal;
		public int dimension;

		public CustomNpcTeleport(ICustomNpc<?> npc, IPos portalIn, IPos posIn, int dimensionIn) {
			super(npc);
			pos = posIn;
			portal = portalIn;
			dimension = dimensionIn;
		}

	}

	@Cancelable
	@EventName(EnumScriptType.NEED_BLOCK_DAMAGED)
	public static class NeedBlockDamage extends NpcEvent {

		public IDamageSource damageSource;
		public boolean isBlocked;
		public int type;

		public NeedBlockDamage(ICustomNpc<?> npc, DamageSource damagesource, boolean isBlockedIn, int typeIn) {
			super(npc);
			damageSource = Objects.requireNonNull(NpcAPI.Instance()).getIDamageSource(damagesource);
			isBlocked = isBlockedIn;
			type = typeIn;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.DAMAGED)
	public static class DamagedEvent extends NpcEvent {
		public boolean clearTarget;
		public float damage;
		public IDamageSource damageSource;
		public IEntity<?> source;

		public DamagedEvent(ICustomNpc<?> npc, Entity sourceIn, float damageIn, DamageSource damageSourceIn) {
			super(npc);
			clearTarget = false;
			source = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(sourceIn);
			damage = damageIn;
			damageSource = Objects.requireNonNull(NpcAPI.Instance()).getIDamageSource(damageSourceIn);
		}
	}

	@EventName(EnumScriptType.DIED)
	public static class DiedEvent extends NpcEvent {

		public int expDropped = 0;
		public double totalDamage = 0.0d;
		public double totalDamageOnlyPlayers = 0.0d;
		public IDamageSource damageSource;
		public IItemStack[] droppedItems;
		public Map<IEntity<?>, List<IItemStack>> lootedItems;
		public Map<IEntity<?>, List<IItemStack>> inventoryItems;
		public ILine line;
		public IEntity<?> source;
		public String type;
		public final Map<IEntity<?>, Double> damageMap = new HashMap<>();

		public DiedEvent(ICustomNpc<?> npc, DamageSource damagesource, Entity entity, CombatHandler combatHandler) {
			super(npc);
			type = damagesource.damageType;
			source = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
			damageSource = Objects.requireNonNull(NpcAPI.Instance()).getIDamageSource(damagesource);
			for (EntityLivingBase e : combatHandler.aggressors.keySet()) {
				double damage = combatHandler.aggressors.get(e);
				damageMap.put(Objects.requireNonNull(NpcAPI.Instance()).getIEntity(e), damage);
				totalDamage += damage;
				if (e instanceof EntityPlayer) { totalDamageOnlyPlayers = damage; }
			}
		}
		
		public IEntity<?>[] getEntitys() { return damageMap.keySet().toArray(new IEntity<?>[0]); }

		@SuppressWarnings("all")
		public double getDamageFromEntity(IEntity<?> entity) {
			if (damageMap.containsKey(entity)) { return damageMap.get(entity); }
			else if (entity != null) {
				for (IEntity<?> ie : damageMap.keySet()) {
					if (entity.getMCEntity().equals(ie.getMCEntity())) {
						return damageMap.get(ie);
					}
				}
			}
			return 0.0d;
		}
		
	}

	@EventName(EnumScriptType.INIT)
	public static class InitEvent extends NpcEvent {
		public InitEvent(ICustomNpc<?> npc) {
			super(npc);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.INTERACT)
	public static class InteractEvent extends NpcEvent {
		public IPlayer<?> player;

		public InteractEvent(ICustomNpc<?> npc, EntityPlayer playerIn) {
			super(npc);
			player = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(playerIn);
		}
	}

	@EventName(EnumScriptType.KILL)
	public static class KilledEntityEvent extends NpcEvent {
		public IEntityLivingBase<?> entity;

		public KilledEntityEvent(ICustomNpc<?> npc, EntityLivingBase entityIn) {
			super(npc);
			entity = (IEntityLivingBase<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entityIn);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.ATTACK_MELEE)
	public static class MeleeAttackEvent extends NpcEvent {
		public float damage;
		public IEntityLivingBase<?> target;

		public MeleeAttackEvent(ICustomNpc<?> npc, EntityLivingBase targetIn, float damageIn) {
			super(npc);
			target = (IEntityLivingBase<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(targetIn);
			damage = damageIn;
		}
	}

	@EventName(EnumScriptType.RANGED_LAUNCHED)
	public static class RangedLaunchedEvent extends NpcEvent {

		public float damage;
		public List<IProjectile<?>> projectiles = new ArrayList<>();
		public IEntityLivingBase<?> target;

		public RangedLaunchedEvent(ICustomNpc<?> npc, EntityLivingBase targetIn, float damageIn) {
			super(npc);
			target = (IEntityLivingBase<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(targetIn);
			damage = damageIn;
		}
	}

	@EventName(EnumScriptType.STOP_ANIMATION)
	public static class StopAnimation extends NpcEvent {

		public IAnimation animation;
		public int type;

		public StopAnimation(ICustomNpc<?> npc, int typeIn, int id) {
			super(npc);
			type = typeIn;
			animation = AnimationController.getInstance().getAnimation(id);
		}

	}

	@Cancelable
	@EventName(EnumScriptType.TARGET)
	public static class TargetEvent extends NpcEvent {
		public IEntityLivingBase<?> entity;

		public TargetEvent(ICustomNpc<?> npc, EntityLivingBase entityIn) {
			super(npc);
			entity = (IEntityLivingBase<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entityIn);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.TARGET_LOST)
	public static class TargetLostEvent extends NpcEvent {
		public IEntityLivingBase<?> entity;

		public TargetLostEvent(ICustomNpc<?> npc, EntityLivingBase entityIn) {
			super(npc);
			entity = (IEntityLivingBase<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entityIn);
		}
	}

	@EventName(EnumScriptType.TIMER)
	public static class TimerEvent extends NpcEvent {
		public int id;

		public TimerEvent(ICustomNpc<?> npc, int idIn) {
			super(npc);
			id = idIn;
		}
	}

	@EventName(EnumScriptType.TICK)
	public static class UpdateEvent extends NpcEvent {
		public UpdateEvent(ICustomNpc<?> npc) { super(npc); }
	}

	public ICustomNpc<?> npc;
	public NpcEvent(ICustomNpc<?> npcIn) { npc = npcIn; }

}
