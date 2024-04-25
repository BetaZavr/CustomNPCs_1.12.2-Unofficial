package noppes.npcs.api.event;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
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
import noppes.npcs.controllers.AnimationController;

public class NpcEvent extends CustomNPCsEvent {

	public static class CollideEvent extends NpcEvent {
		public IEntity<?> entity;

		public CollideEvent(ICustomNpc<?> npc, Entity entity) {
			super(npc);
			this.entity = NpcAPI.Instance().getIEntity(entity);
		}
	}

	@Cancelable
	public static class CustomNpcTeleport extends NpcEvent {

		public IPos pos, portal;
		public int dimension;

		public CustomNpcTeleport(ICustomNpc<?> npc, IPos portal, IPos pos, int dimensionID) {
			super(npc);
			this.pos = pos;
			this.portal = portal;
			this.dimension = dimensionID;
		}

	}

	@Cancelable
	public static class DamagedEvent extends NpcEvent {
		public boolean clearTarget;
		public float damage;
		public IDamageSource damageSource;
		public IEntity<?> source;

		public DamagedEvent(ICustomNpc<?> npc, Entity source, float damage, DamageSource damagesource) {
			super(npc);
			this.clearTarget = false;
			this.source = NpcAPI.Instance().getIEntity(source);
			this.damage = damage;
			this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
		}
	}

	public static class DiedEvent extends NpcEvent {
		public IDamageSource damageSource;
		public IItemStack[] droppedItems;
		public int expDropped;
		public ILine line;
		// New
		public IItemStack[] lootedItems;
		public IEntity<?> source;
		public String type;

		public DiedEvent(ICustomNpc<?> npc, DamageSource damagesource, Entity entity) {
			super(npc);
			this.type = damagesource.damageType;
			this.source = NpcAPI.Instance().getIEntity(entity);
			this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
		}
	}

	public static class InitEvent extends NpcEvent {
		public InitEvent(ICustomNpc<?> npc) {
			super(npc);
		}
	}

	@Cancelable
	public static class InteractEvent extends NpcEvent {
		public IPlayer<?> player;

		public InteractEvent(ICustomNpc<?> npc, EntityPlayer player) {
			super(npc);
			this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity(player);
		}
	}

	public static class KilledEntityEvent extends NpcEvent {
		public IEntityLivingBase<?> entity;

		public KilledEntityEvent(ICustomNpc<?> npc, EntityLivingBase entity) {
			super(npc);
			this.entity = (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(entity);
		}
	}

	@Cancelable
	public static class MeleeAttackEvent extends NpcEvent {
		public float damage;
		public IEntityLivingBase<?> target;

		public MeleeAttackEvent(ICustomNpc<?> npc, EntityLivingBase target, float damage) {
			super(npc);
			this.target = (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(target);
			this.damage = damage;
		}
	}

	public static class RangedLaunchedEvent extends NpcEvent {

		public float damage;
		public List<IProjectile<?>> projectiles;
		public IEntityLivingBase<?> target;

		public RangedLaunchedEvent(ICustomNpc<?> npc, EntityLivingBase target, float damage) {
			super(npc);
			this.projectiles = new ArrayList<IProjectile<?>>();
			this.target = (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(target);
			this.damage = damage;
		}
	}

	public static class StopAnimation extends NpcEvent {

		public IAnimation animation;

		public StopAnimation(ICustomNpc<?> npc, int type, int id) {
			super(npc);
			this.animation = AnimationController.getInstance().animations.get(id);
		}

	}

	@Cancelable
	public static class TargetEvent extends NpcEvent {
		public IEntityLivingBase<?> entity;

		public TargetEvent(ICustomNpc<?> npc, EntityLivingBase entity) {
			super(npc);
			this.entity = (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(entity);
		}
	}

	@Cancelable
	public static class TargetLostEvent extends NpcEvent {
		public IEntityLivingBase<?> entity;

		public TargetLostEvent(ICustomNpc<?> npc, EntityLivingBase entity) {
			super(npc);
			this.entity = (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(entity);
		}
	}

	public static class TimerEvent extends NpcEvent {
		public int id;

		public TimerEvent(ICustomNpc<?> npc, int id) {
			super(npc);
			this.id = id;
		}
	}

	public static class UpdateEvent extends NpcEvent {
		public UpdateEvent(ICustomNpc<?> npc) {
			super(npc);
		}
	}

	public ICustomNpc<?> npc;

	public NpcEvent(ICustomNpc<?> npc) {
		this.npc = npc;
	}

}
