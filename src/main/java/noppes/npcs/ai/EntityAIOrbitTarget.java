package noppes.npcs.ai;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIOrbitTarget extends EntityAIBase {
	private float angle;
	private boolean canNavigate;
	private boolean decay;
	private float decayRate;
	private int delay;
	private int direction;
	private float distance;
	private double movePosX;
	private double movePosY;
	private double movePosZ;
	private EntityNPCInterface npc;
	private double speed;
	private float targetDistance;
	private EntityLivingBase targetEntity;
	private int tick;

	public EntityAIOrbitTarget(EntityNPCInterface par1EntityCreature, double par2, boolean par5) {
		this.delay = 0;
		this.angle = 0.0f;
		this.direction = 1;
		this.canNavigate = true;
		this.decayRate = 1.0f;
		this.tick = 0;
		this.npc = par1EntityCreature;
		this.speed = par2;
		this.decay = par5;
		this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
	}

	public void resetTask() {
		this.npc.getNavigator().clearPath();
		this.delay = 60;
		if (this.npc.getRangedTask() != null) {
			this.npc.getRangedTask().navOverride(false);
		}
	}

	public boolean shouldContinueExecuting() {
		return this.targetEntity.isEntityAlive() && !this.npc.isInRange(this.targetEntity, this.distance / 2.0f)
				&& this.npc.isInRange(this.targetEntity, this.distance * 1.5) && !this.npc.isInWater()
				&& this.canNavigate;
	}

	public boolean shouldExecute() {
		int delay = this.delay - 1;
		this.delay = delay;
		if (delay > 0) {
			return false;
		}
		this.delay = 10;
		this.targetEntity = this.npc.getAttackTarget();
		if (this.targetEntity == null) {
			return false;
		}
		if (this.decay) {
			this.distance = this.npc.ais.getTacticalRange();
		} else {
			this.distance = this.npc.stats.ranged.getRange();
		}
		return !this.npc.isInRange(this.targetEntity, this.distance / 2.0f)
				&& (this.npc.inventory.getProjectile() != null || this.npc.isInRange(this.targetEntity, this.distance));
	}

	public void startExecuting() {
		this.canNavigate = true;
		Random random = this.npc.getRNG();
		this.direction = ((random.nextInt(10) > 5) ? 1 : -1);
		this.decayRate = random.nextFloat() + this.distance / 16.0f;
		this.targetDistance = this.npc.getDistance(this.targetEntity);
		double d0 = this.npc.posX - this.targetEntity.posX;
		double d2 = this.npc.posZ - this.targetEntity.posZ;
		this.angle = (float) Math.atan2(d2, d0) * 180.0f / 3.141592653589793f;
		if (this.npc.getRangedTask() != null) {
			this.npc.getRangedTask().navOverride(true);
		}
	}

	public void updateTask() {
		this.npc.getLookHelper().setLookPositionWithEntity(this.targetEntity, 30.0f, 30.0f);
		if (this.npc.getNavigator().noPath() && this.tick >= 0 && this.npc.onGround && !this.npc.isInWater()) {
			double d0 = this.targetDistance * MathHelper.cos(this.angle / 180.0f * 3.1415927f);
			double d2 = this.targetDistance * MathHelper.sin(this.angle / 180.0f * 3.1415927f);
			this.movePosX = this.targetEntity.posX + d0;
			this.movePosY = this.targetEntity.getEntityBoundingBox().maxY;
			this.movePosZ = this.targetEntity.posZ + d2;
			this.npc.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
			this.angle += 15.0f * this.direction;
			this.tick = MathHelper.ceil(
					this.npc.getDistance(this.movePosX, this.movePosY, this.movePosZ) / (this.npc.getSpeed() / 20.0f));
			if (this.decay) {
				this.targetDistance -= this.decayRate;
			}
		}
		if (this.tick >= 0) {
			--this.tick;
		}
	}
}
