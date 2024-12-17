package noppes.npcs.ai.attack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.api.mixin.entity.ai.IEntityAITasksMixin;
import noppes.npcs.util.Util;

public class EntityAICustom extends EntityAIBase {

	protected final EntityNPCInterface npc;
	protected final int tickRate;
	protected EntityLivingBase target;

	public boolean hasAttack;
	public boolean startRangedAttack;
	public boolean isRanged;
	public boolean canSeeToAttack;
	public boolean inMove;
	public boolean isFriend;

	protected int burstCount;
	protected int tacticalRange;
	protected int rangedTick;
	protected int meleeTick;
	protected int step;

	public double distance;
	public double range;

	public EntityAICustom(EntityNPCInterface npc) {
		this.npc = npc;
		navOverride(true);
		tickRate = ((IEntityAITasksMixin) this.npc.tasks).npcs$getTickRate();
		step = 0;
		distance = -1.0d;
	}

	public EntityAICustom(IRangedAttackMob npc) {
		if (!(npc instanceof EntityNPCInterface)) {
			throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
		}
		this.npc = (EntityNPCInterface) npc;
		navOverride(true);
		tickRate = ((IEntityAITasksMixin) this.npc.tasks).npcs$getTickRate();
		distance = -1.0d;
	}

	public EntityLivingBase getTarget() { return target; }

	public void navOverride(boolean nav) { setMutexBits(nav ? AiMutex.PATHING : (AiMutex.LOOK + AiMutex.PASSIVE)); }

	/**
	 * resets this AI's work when "shouldContinueExecuting" returns "false"
	 */
	@Override
	public void resetTask() {
		canSeeToAttack = false;
		npc.updateHitbox();
	}

	/**
	 * checks whether this AI can continue to execute -> updateTask
	 */
	@Override
	public boolean shouldContinueExecuting() {
		return npc != null && npc.isEntityAlive() && setTarget();
	}

	private boolean setTarget() {
		target = npc.getAttackTarget();
		if (npc.aiOwnerNPC != null && npc.aiOwnerNPC.isEntityAlive()) {
			EntityLivingBase ownerTarget = npc.aiOwnerNPC.getAttackTarget();
			if (ownerTarget != null && ownerTarget.equals(target)) {
				npc.setAttackTarget(ownerTarget);
			}
			target = npc.getAttackTarget();
		}
		if (target == null || !target.isEntityAlive()) {
			CustomNpcs.debugData.endDebug("Server", npc, "EntityAICustom_shouldExecute");
			startRangedAttack = false;
			return false;
		}
		// target is GM Player reset in EntityNPCInterface.onUpdate()
		isFriend = npc.isFriend(target);
		return target != null;
	}

	/**
	 * checks the possibility of running this AI
	 */
	@Override
	public boolean shouldExecute() {
		CustomNpcs.debugData.startDebug("Server", this.npc, "EntityAICustom_shouldExecute");
		distance = -1.0d;
		canSeeToAttack = false;
		hasAttack = false;
		setTarget();
		CustomNpcs.debugData.endDebug("Server", npc, "EntityAICustom_shouldExecute");
		return setTarget();
	}

	private void setLookPositionWithEntity(Entity target) {
		if (!CustomNpcs.ShowCustomAnimation || !npc.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES)) {
			npc.getLookHelper().setLookPositionWithEntity(target, 7.5f, 3.75f);
		}
	}

	protected void tryMoveToTarget() {
		if (!CustomNpcs.ShowCustomAnimation || !npc.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES)) {
			double baseSpeed = npc.ais.canSprint ? 1.5d : 1.3d;
			if (target.equals(npc.combatHandler.priorityTarget)) { baseSpeed = npc.ais.canSprint ? 1.6d : 1.4d; }
			double dist = npc.getDistance(target.posX, target.posY, target.posZ);
			double speed = (0.75d / (double) npc.stats.aggroRange * dist + 0.5d) * baseSpeed;
			if (speed < 1.3d) { speed = 1.3d; }
			else if (speed > baseSpeed) { speed = baseSpeed; }
			npc.getNavigator().tryMoveToEntityLiving(target, speed);
		}
	}

	protected void tryToCauseDamage() {
		if (isRanged) {
			if (rangedTick > 0 || distance > range || !canSeeToAttack || npc.stats.ranged.getFireType() == 2) {
				if (rangedTick == 0 && !canSeeToAttack) { rangedTick = 5; }
				startRangedAttack = false;
				return;
			}
			startRangedAttack = true;
			return;
		}
		if (meleeTick > 0 || distance > range || !canSeeToAttack) {
			if (meleeTick == 0 && !canSeeToAttack) { meleeTick = 5; }
			return;
		}
		meleeTick = npc.stats.melee.getDelayRNG();
		npc.swingArm(EnumHand.MAIN_HAND);
		npc.attackEntityAsMob(target);
		hasAttack = true;
	}

	public void update() {
		if (!startRangedAttack || target == null || !target.isEntityAlive() || !npc.isEntityAlive()) {
			startRangedAttack = false;
			//this.step = 0; this.burstCount = 0;
			return;
		}
		step++;
		if (step >= tickRate) {
			step = 0;
		}
		if (rangedTick > step) {
			return;
		}

		if (burstCount++ <= npc.stats.ranged.getBurst()) {
			rangedTick = npc.stats.ranged.getBurstDelay();
		} else {
			burstCount = 0;
			hasAttack = true;
			rangedTick = npc.stats.ranged.getDelayRNG();
		}
		if (burstCount > 1) {
			boolean indirect = false;
			switch (npc.stats.ranged.getFireType()) {
				case 1: {
					indirect = (distance > range / 2.0);
					break;
				}
				case 2: {
					indirect = !npc.getEntitySenses().canSee(target);
					break;
				}
			}
			npc.attackEntityWithRangedAttack(target, indirect ? 1.0f : 0.0f);
			if (npc.currentAnimation != 6) {
				npc.swingArm(EnumHand.MAIN_HAND);
			}
			step = 0;
		}
	}

	/**
	 * will run every tick until "shouldContinueExecuting" returns "true"
	 */
	@Override
	public void updateTask() {
		CustomNpcs.debugData.startDebug("Server", npc, "EntityAICustom_updateTask");
		setLookPositionWithEntity(target);
		inMove = !npc.getNavigator().noPath();
		tacticalRange = npc.ais.getTacticalRange();
		distance = npc.getDistance(this.target.posX, this.target.getEntityBoundingBox().minY, this.target.posZ);
		isRanged = npc.inventory.getProjectile() != null && (this.npc.stats.ranged.getMeleeRange() <= 0 || this.distance > this.npc.stats.ranged.getMeleeRange());
		if (isRanged) {
			rangedTick--;
			range = npc.stats.ranged.getRange();
		} else {
			meleeTick--;
			range = npc.stats.melee.getRange();
			double minRange = (npc.width + target.width) / 2.0d;
			if (minRange > range) {
				range = minRange;
			}
		}
		CustomNpcs.debugData.endDebug("Server", this.npc, "EntityAICustom_updateTask");
	}

	public void writeToClientNBT(NBTTagCompound compound) {
	}

}
