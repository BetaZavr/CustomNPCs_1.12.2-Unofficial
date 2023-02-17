package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIPounceTarget extends EntityAIBase {
	private float leapSpeed;
	private EntityLivingBase leapTarget;
	private EntityNPCInterface npc;

	public EntityAIPounceTarget(EntityNPCInterface leapingEntity) {
		this.leapSpeed = 1.3f;
		this.npc = leapingEntity;
		this.setMutexBits(4);
	}

	public float getAngleForXYZ(double varX, double varY, double varZ, double horiDist) {
		float g = 0.1f;
		float var1 = this.leapSpeed * this.leapSpeed;
		double var2 = g * horiDist;
		double var3 = g * horiDist * horiDist + 2.0 * varY * var1;
		double var4 = var1 * var1 - g * var3;
		if (var4 < 0.0) {
			return 90.0f;
		}
		float var5 = var1 - MathHelper.sqrt(var4);
		float var6 = (float) Math.atan2(var5, var2) * 180.0f / 3.141592653589793f;
		return var6;
	}

	public boolean shouldContinueExecuting() {
		return !this.npc.onGround;
	}

	public boolean shouldExecute() {
		if (!this.npc.onGround) {
			return false;
		}
		this.leapTarget = this.npc.getAttackTarget();
		return this.leapTarget != null && this.npc.getEntitySenses().canSee(this.leapTarget)
				&& !this.npc.isInRange(this.leapTarget, 4.0) && this.npc.isInRange(this.leapTarget, 8.0)
				&& this.npc.getRNG().nextInt(5) == 0;
	}

	public void startExecuting() {
		double varX = this.leapTarget.posX - this.npc.posX;
		double varY = this.leapTarget.getEntityBoundingBox().minY - this.npc.getEntityBoundingBox().minY;
		double varZ = this.leapTarget.posZ - this.npc.posZ;
		float varF = MathHelper.sqrt(varX * varX + varZ * varZ);
		float angle = this.getAngleForXYZ(varX, varY, varZ, varF);
		float yaw = (float) Math.atan2(varX, varZ) * 180.0f / 3.141592653589793f;
		this.npc.motionX = MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(angle / 180.0f * 3.1415927f);
		this.npc.motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(angle / 180.0f * 3.1415927f);
		this.npc.motionY = MathHelper.sin((angle + 1.0f) / 180.0f * 3.1415927f);
		EntityNPCInterface npc = this.npc;
		npc.motionX *= this.leapSpeed;
		EntityNPCInterface npc2 = this.npc;
		npc2.motionZ *= this.leapSpeed;
		EntityNPCInterface npc3 = this.npc;
		npc3.motionY *= this.leapSpeed;
	}
}
