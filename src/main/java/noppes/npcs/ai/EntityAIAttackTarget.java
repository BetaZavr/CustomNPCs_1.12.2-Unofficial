package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAttackTarget
extends EntityAIBase {
	
	private int attackTick;
	private int delayCounter;
	private Path entityPathEntity;
	private EntityLivingBase entityTarget;
	private boolean navOverride;
	private EntityNPCInterface npc;

	public EntityAIAttackTarget(EntityNPCInterface npc) {
		this.navOverride = false;
		this.attackTick = 0;
		this.npc = npc;
		this.navOverride = npc.ais.tacticalVariant == 6;
		this.setMutexBits(this.navOverride ? AiMutex.PATHING : (AiMutex.LOOK + AiMutex.PASSIVE));
	}

	public void navOverride(boolean nav) {
		this.navOverride = nav;
		this.setMutexBits(this.navOverride ? AiMutex.PATHING : (AiMutex.LOOK + AiMutex.PASSIVE));
	}

	public void resetTask() {
		this.entityPathEntity = null;
		this.entityTarget = null;
		this.npc.getNavigator().clearPath();
	}

	public boolean shouldContinueExecuting() {
		this.entityTarget = this.npc.getAttackTarget();
		if (this.entityTarget == null) { this.entityTarget = this.npc.getRevengeTarget(); }
		if (this.entityTarget == null || !this.entityTarget.isEntityAlive()) {
			return false;
		}
		if (!this.npc.isInRange(this.entityTarget, this.npc.stats.aggroRange)) {
			return false;
		}
		int melee = this.npc.stats.ranged.getMeleeRange();
		return (melee <= 0 || this.npc.isInRange(this.entityTarget, melee))
				&& this.npc.isWithinHomeDistanceFromPosition(new BlockPos(this.entityTarget));
	}

	public boolean shouldExecute() {
		EntityLivingBase target = this.npc.getAttackTarget();
		if (target == null || !target.isEntityAlive()) { return false; }
		int ranged = this.npc.stats.ranged.getMeleeRange();
		if (this.npc.inventory.getProjectile()!=null && (ranged <= 0 || !this.npc.isInRange(target, ranged))) {
			return false;
		}
		this.entityTarget = target;
		this.entityPathEntity = this.npc.getNavigator().getPathToEntityLiving(target);
		return this.entityPathEntity != null;
	}

	public void startExecuting() {
		if (!this.navOverride) { this.npc.getNavigator().setPath(this.entityPathEntity, 1.3); }
		this.delayCounter = 0;
	}

	public void updateTask() {
		this.npc.getLookHelper().setLookPositionWithEntity(this.entityTarget, 30.0f, 30.0f);
		double y = this.entityTarget.posY;
		if (this.entityTarget.getEntityBoundingBox() != null) { y = this.entityTarget.getEntityBoundingBox().minY; }
		double distance = this.npc.getDistanceSq(this.entityTarget.posX, y, this.entityTarget.posZ);
		double range = this.npc.stats.melee.getRange() * this.npc.stats.melee.getRange() + this.entityTarget.width;
		if (!this.navOverride && --this.delayCounter <= 0) {
			this.delayCounter = 4 + this.npc.getRNG().nextInt(7);
			if (distance > range / 2.0d) { this.npc.getNavigator().tryMoveToEntityLiving(this.entityTarget, 1.3); }
			else if (this.npc.getNavigator().getPath()!=null) { this.npc.getNavigator().clearPath(); }
		}
		this.attackTick = Math.max(this.attackTick - 1, 0);
		double minRange = this.npc.width * 2.0f * this.npc.width * 2.0f + this.entityTarget.width;
		if (minRange > range) {
			range = minRange;
		}
		if (distance <= range && (this.npc.canSee(this.entityTarget) || distance < minRange) && this.attackTick <= 0) {
			this.attackTick = this.npc.stats.melee.getDelay();
			this.npc.swingArm(EnumHand.MAIN_HAND);
			this.npc.attackEntityAsMob(this.entityTarget);
		}
	}

	public EntityLivingBase getTarget() { return this.entityTarget; }
	
}
