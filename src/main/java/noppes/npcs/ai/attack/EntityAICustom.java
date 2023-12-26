package noppes.npcs.ai.attack;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ObfuscationHelper;

public class EntityAICustom
extends EntityAIBase {
	
	protected final EntityNPCInterface npc;
	protected final int tickRate;
	protected EntityLivingBase target;
	protected int burstCount, tacticalRange;
	protected int rangedTick, meleeTick, delay;
	public boolean hasAttack;
	public boolean isRanged, canSeeToAttack, inMove;
	public double distance, range;
	
	public EntityAICustom(EntityNPCInterface npc) {
		this.npc = npc;
		this.navOverride(true);
		this.tickRate = ObfuscationHelper.getValue(EntityAITasks.class, this.npc.tasks, 5);
		this.distance = -1.0d;
	}
	
	public EntityAICustom(IRangedAttackMob npc) {
		if (!(npc instanceof EntityNPCInterface)) {
			throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
		}
		this.npc = (EntityNPCInterface) npc;
		this.navOverride(true);
		this.tickRate = ObfuscationHelper.getValue(EntityAITasks.class, this.npc.tasks, 5);
		this.distance = -1.0d;
	}

	public void navOverride(boolean nav) {
		this.setMutexBits(nav ? AiMutex.PATHING : (AiMutex.LOOK + AiMutex.PASSIVE));
	}
	
	/* reset every time (2 ticks out of 3)
	 * EntityAITasks.onUpdateTasks()
	 * EntityAITasks.java:91
	 */
	@Override
	public void resetTask() { }

	@Override
	public boolean shouldContinueExecuting() {
        return this.npc != null && !this.npc.isEntityAlive() && !this.npc.isRunHome;
    }
	
	@Override
	public boolean shouldExecute() {
		CustomNpcs.debugData.startDebug("Server", this.npc, "EntityAICustom_shouldExecute");
		this.distance = -1.0d;
		this.canSeeToAttack = false;
		this.hasAttack = false;
		this.target = this.npc.getAttackTarget();
		if (this.npc.aiOwnerNPC != null) {
			if (!this.npc.aiOwnerNPC.isEntityAlive() || this.npc.aiOwnerNPC.getAttackTarget()==null) {
				this.npc.aiOwnerNPC = null;
			}
			else {
				if (this.target != null) { this.npc.setAttackTarget(null); }
				this.npc.getLookHelper().setLookPositionWithEntity(this.npc.aiOwnerNPC, 30.0f, 30.0f);
			}
			CustomNpcs.debugData.endDebug("Server", this.npc, "EntityAICustom_shouldExecute");
			return false;
		}
		if (this.target == null || !this.target.isEntityAlive()) {
			if (this.delay > 0) {
				this.delay--;
				if (this.delay == 0) { this.npc.runBack(); }
			}
			CustomNpcs.debugData.endDebug("Server", this.npc, "EntityAICustom_shouldExecute");
			return false;
		}
		this.delay = 20;
		CustomNpcs.debugData.endDebug("Server", this.npc, "EntityAICustom_shouldExecute");
		return true;
	}

	@Override
	public void startExecuting() {
	}

	@Override
	public void updateTask() {
		CustomNpcs.debugData.startDebug("Server", this.npc, "EntityAICustom_updateTask");
		this.npc.getLookHelper().setLookPositionWithEntity(this.target, 30.0f, 30.0f);
		this.inMove = !this.npc.getNavigator().noPath();
		this.tacticalRange = this.npc.ais.getTacticalRange();
		this.distance = this.npc.getDistance(this.target.posX, this.target.getEntityBoundingBox().minY, this.target.posZ);
		this.isRanged = this.npc.inventory.getProjectile() != null && (this.npc.stats.ranged.getMeleeRange() <= 0 || this.distance > this.npc.stats.ranged.getMeleeRange());
		if (this.isRanged) {
			this.rangedTick = Math.max(this.rangedTick - this.tickRate, 0);
			this.range = (double) this.npc.stats.ranged.getRange();
		} else {
			this.meleeTick = Math.max(this.meleeTick - this.tickRate, 0);
			this.range = (double) this.npc.stats.melee.getRange();
			double minRange = this.npc.width * 2.0f + this.target.width;
			if (minRange > this.range) { this.range = minRange; }
		}
		CustomNpcs.debugData.endDebug("Server", this.npc, "EntityAICustom_updateTask");
	}

	public EntityLivingBase getTarget() { return this.target; }


	protected void tryMoveToTarget() {
		this.npc.getNavigator().tryMoveToEntityLiving(this.target, 1.3d);
	}
	
	protected void tryToCauseDamage() {
		if (this.isRanged) {
			if (this.rangedTick > 0 || this.distance > this.range || !(this.canSeeToAttack || this.npc.stats.ranged.getFireType() == 2)) {
				if (this.rangedTick == 0 && !this.canSeeToAttack) { this.rangedTick = 5; }
				return;
			}
			if (this.burstCount++ <= this.npc.stats.ranged.getBurst()) {
				this.rangedTick = this.npc.stats.ranged.getBurstDelay();
			} else {
				this.burstCount = 0;
				this.hasAttack = true;
				this.rangedTick = this.npc.stats.ranged.getDelayRNG();
			}
			
			if (this.burstCount > 1) {
				boolean indirect = false;
				switch (this.npc.stats.ranged.getFireType()) {
					case 1: {
						indirect = (this.distance > this.range / 2.0);
						break;
					}
					case 2: {
						indirect = !this.npc.getEntitySenses().canSee(this.target);
						break;
					}
				}
				this.npc.attackEntityWithRangedAttack(this.target, indirect ? 1.0f : 0.0f);
				if (this.npc.currentAnimation != 6) {
					this.npc.swingArm(EnumHand.MAIN_HAND);
				}
			}
			return;
		}
		if (this.meleeTick > 0 || this.distance > this.range || !this.canSeeToAttack) {
			if (this.meleeTick == 0 && !this.canSeeToAttack) { this.meleeTick = 5; }
			return;
		}
		this.meleeTick = this.npc.stats.melee.getDelayRNG();
		this.npc.swingArm(EnumHand.MAIN_HAND);
		this.npc.attackEntityAsMob(this.target);
		this.hasAttack = true;
	}

	public void writeToClientNBT(NBTTagCompound compound) { }
	
}
