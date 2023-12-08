package noppes.npcs.ai;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAvoidTarget extends EntityAIBase {
	private Entity closestLivingEntity;
	private float distanceFromEntity;
	private Path entityPathEntity;
	private PathNavigate entityPathNavigate;
	private float health;
	private EntityNPCInterface npc;
	private Class<?> targetEntityClass;

	public EntityAIAvoidTarget(EntityNPCInterface par1EntityNPC) {
		this.npc = par1EntityNPC;
		this.distanceFromEntity = this.npc.stats.aggroRange;
		this.health = this.npc.getHealth();
		this.entityPathNavigate = par1EntityNPC.getNavigator();
		this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
	}

	public void resetTask() {
		this.closestLivingEntity = null;
		this.npc.setAttackTarget(null);
	}

	public boolean shouldContinueExecuting() {
		return !this.entityPathNavigate.noPath();
	}

	@SuppressWarnings("unchecked")
	public boolean shouldExecute() {
		EntityLivingBase target = this.npc.getAttackTarget();
		if (target == null) {
			return false;
		}
		this.targetEntityClass = target.getClass();
		if (this.targetEntityClass == EntityPlayer.class) {
			this.closestLivingEntity = this.npc.world.getClosestPlayerToEntity(this.npc, this.distanceFromEntity);
			if (this.closestLivingEntity == null) {
				return false;
			}
		} else {
			List<? extends Entity> var1 = this.npc.world.getEntitiesWithinAABB(
					(Class<? extends Entity>) this.targetEntityClass,
					this.npc.getEntityBoundingBox().grow(this.distanceFromEntity, 3.0, this.distanceFromEntity));
			if (var1.isEmpty()) {
				return false;
			}
			this.closestLivingEntity = var1.get(0);
		}
		if (!this.npc.getEntitySenses().canSee(this.closestLivingEntity) && this.npc.ais.directLOS) {
			return false;
		}
		Vec3d var2 = RandomPositionGenerator.findRandomTargetBlockAwayFrom((EntityCreature) this.npc, 16, 7,
				new Vec3d(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ));
		boolean var3 = this.npc.inventory.getProjectile() == null;
		boolean var4 = var3 ? (this.health == this.npc.getHealth())
				: (this.npc.aiAttackTarget != null && !this.npc.aiAttackTarget.hasAttack);
		if (var2 == null) {
			return false;
		}
		if (this.closestLivingEntity.getDistanceSq(var2.x, var2.y, var2.z) < this.closestLivingEntity
				.getDistance(this.npc)) {
			return false;
		}
		if (this.npc.ais.tacticalVariant == 3 && var4) {
			return false;
		}
		this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(var2.x, var2.y, var2.z);
		return this.entityPathEntity != null;
	}

	public void startExecuting() {
		this.entityPathNavigate.setPath(this.entityPathEntity, 1.0);
	}

	public void updateTask() {
		if (this.npc.isInRange(this.closestLivingEntity, 7.0)) {
			this.npc.getNavigator().setSpeed(1.2);
		} else {
			this.npc.getNavigator().setSpeed(1.0);
		}
		if (this.npc.ais.tacticalVariant == 3 && (!this.npc.isInRange(this.closestLivingEntity, this.distanceFromEntity)
				|| this.npc.isInRange(this.closestLivingEntity, this.npc.ais.getTacticalRange()))) {
			this.health = this.npc.getHealth();
		}
	}
}
