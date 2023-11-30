package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIRangedAttack
extends EntityAIBase {
	
	private EntityLivingBase entityTarget;
	private int burstCount;
	private boolean hasFired;
	private int moveTries;
	private boolean navOverride;
	private EntityNPCInterface npc;
	private int rangedTick, meleeTick;

	public EntityAIRangedAttack(IRangedAttackMob npc) {
		if (!(npc instanceof EntityNPCInterface)) {
			throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
		}
		this.moveTries = 0;
		this.burstCount = 0;
		this.hasFired = false;
		this.npc = (EntityNPCInterface) npc;
		this.navOverride = this.npc.ais.tacticalVariant == 0;
		this.rangedTick = this.npc.stats.ranged.getDelayMin() / 2;
		this.meleeTick = 0;
		this.setMutexBits(this.navOverride ? AiMutex.PATHING : (AiMutex.LOOK + AiMutex.PASSIVE));
	}

	public boolean hasFired() {
		return this.hasFired;
	}

	public void navOverride(boolean nav) {
		this.navOverride = nav;
		this.setMutexBits(this.navOverride ? AiMutex.PATHING : (AiMutex.LOOK + AiMutex.PASSIVE));
	}
	
	@Override
	public void resetTask() {
		System.out.println("resetTask");
		this.entityTarget = null;
		this.npc.setAttackTarget(null);
		this.npc.getNavigator().clearPath();
		this.moveTries = 0;
		this.hasFired = false;
		this.rangedTick = this.npc.stats.ranged.getDelayMin() / 2;
	}

	@Override
	public boolean shouldExecute() {
		this.entityTarget = this.npc.getAttackTarget();
		return this.entityTarget != null &&
				this.entityTarget.isEntityAlive() &&
				this.npc.isInRange(this.entityTarget, this.npc.stats.aggroRange) &&
				this.npc.inventory.getProjectile() != null;
	}

	@Override
	public void updateTask() {
		this.npc.getLookHelper().setLookPositionWithEntity(this.entityTarget, 30.0f, 30.0f);
		
		double distance = this.npc.getDistanceSq(this.entityTarget.posX, this.entityTarget.getEntityBoundingBox().minY, this.entityTarget.posZ);

		// Melee attack in distance
		if (this.npc.stats.ranged.getMeleeRange() > 0) {
			double rangeD = this.npc.stats.ranged.getMeleeRange() * this.npc.stats.ranged.getMeleeRange();
			if (distance <= rangeD) {
				double melee = this.npc.stats.melee.getRange() * this.npc.stats.melee.getRange();
				double minRange = this.npc.width * 2.0f * this.npc.width * 2.0f + this.entityTarget.width;
				if (minRange > melee) { melee = minRange; }
				this.meleeTick = Math.max(this.meleeTick - 1, 0);
				if (distance < melee) { // can melee atack
					if (this.npc.ais.tacticalVariant == 0) { this.npc.getNavigator().clearPath(); }
					if (this.npc.canSee(this.entityTarget) && this.meleeTick <= 0) {
						this.meleeTick = this.npc.stats.melee.getDelay();
						this.npc.swingArm(EnumHand.MAIN_HAND);
						this.npc.attackEntityAsMob(this.entityTarget);
					}
					return;
				}
			}
			else if (this.npc.ticksExisted % 20 == 0) {
				System.out.println("CNPCs: ");
				this.npc.getNavigator().tryMoveToEntityLiving(this.entityTarget, this.npc.ais.canSprint ? 1.5d : 1.0d);
			}
		}
		
		// Moved
		boolean canSee = this.npc.canSee(this.entityTarget);
		double range = this.npc.stats.ranged.getRange() * this.npc.stats.ranged.getRange();
		if (!canSee) { ++this.moveTries; } else { this.moveTries = 0; }
		
		if ((distance <= range && this.npc.getNavigator().noPath() && this.npc.ticksExisted % 10 == 0) || this.moveTries >= 10) {
			this.moveTries = 0;
			this.npc.getNavigator().tryMoveToEntityLiving(this.entityTarget, this.npc.ais.canSprint ? 1.5d : 1.0d);
		}
		
		if (this.npc.ais.tacticalVariant != 6 && distance <= range * 2.0d / 3.0d && this.npc.stats.ranged.getMeleeRange() <= 0 && canSee) {
			this.npc.getNavigator().clearPath();
		}
		
		// Attack
		this.rangedTick = Math.max(this.rangedTick - 1, 0);
		if (this.rangedTick <= 0 && distance <= range && (canSee || this.npc.stats.ranged.getFireType() == 2)) {
			
			if (this.burstCount++ <= this.npc.stats.ranged.getBurst()) {
				this.rangedTick = this.npc.stats.ranged.getBurstDelay();
			} else {
				this.burstCount = 0;
				this.hasFired = true;
				this.rangedTick = this.npc.stats.ranged.getDelayRNG();
			}
			
			if (this.burstCount > 1) {
				boolean indirect = false;
				switch (this.npc.stats.ranged.getFireType()) {
					case 1: {
						indirect = (distance > range / 2.0);
						break;
					}
					case 2: {
						indirect = !this.npc.getEntitySenses().canSee(this.entityTarget);
						break;
					}
				}
				this.npc.attackEntityWithRangedAttack(this.entityTarget, indirect ? 1.0f : 0.0f);
				if (this.npc.currentAnimation != 6) {
					this.npc.swingArm(EnumHand.MAIN_HAND);
				}
			}
		}
		else if (!canSee) { this.rangedTick = 5; }
	}

	public EntityLivingBase getTarget() { return this.entityTarget; }
	
}
