package noppes.npcs.ai.target;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class EntityAIWatchClosest extends EntityAIBase {
	private float chance;
	protected Entity closestEntity;
	private int lookTime;
	private float maxDistance;
	private EntityNPCInterface npc;
	private Class<?> watchedClass;

	public EntityAIWatchClosest(EntityNPCInterface par1EntityLiving, Class<?> par2Class, float par3) {
		this.npc = par1EntityLiving;
		this.watchedClass = par2Class;
		this.maxDistance = par3;
		this.chance = 0.002f;
		this.setMutexBits(AiMutex.LOOK);
	}

	@Override
	public void resetTask() {
		this.closestEntity = null;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.npc.isInteracting() && !this.npc.isAttacking() && this.closestEntity.isEntityAlive()
				&& this.npc.isEntityAlive() && this.npc.isInRange(this.closestEntity, this.maxDistance)
				&& this.lookTime > 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean shouldExecute() {
		if (this.npc.getAttackTarget() != null) {
			this.closestEntity = this.npc.getAttackTarget();
		} else if (this.npc.getRNG().nextFloat() >= this.chance || this.npc.isInteracting()) {
			return false;
		}
		if (this.npc.getAttackTarget() == null) {
			if (this.npc.isMoving() || this.npc.ais.getStandingType() != 0 && this.npc.ais.getStandingType() != 2) {
				return false;
			}
			if (this.watchedClass == EntityPlayer.class) {
				this.closestEntity = this.npc.world.getClosestPlayerToEntity(this.npc, this.maxDistance);
			} else {
				this.closestEntity = this.npc.world.findNearestEntityWithinAABB((Class<Entity>) this.watchedClass, this.npc.getEntityBoundingBox().grow(this.maxDistance, 3.0, this.maxDistance), this.npc);
			}
		}
		if (this.closestEntity != null) {
			if (this.closestEntity instanceof EntityLivingBase) {
				return AdditionalMethods.npcCanSeeTarget(this.npc, (EntityLivingBase) this.closestEntity, false, false);
			}
			return this.npc.canSee(this.closestEntity);
		}
		return false;
	}

	@Override
	public void startExecuting() {
		this.lookTime = 60 + this.npc.getRNG().nextInt(60);
	}

	@Override
	public void updateTask() {
		this.npc.getLookHelper().setLookPosition(this.closestEntity.posX,
				this.closestEntity.posY + this.closestEntity.getEyeHeight(), this.closestEntity.posZ, 10.0f,
				this.npc.getVerticalFaceSpeed());
		--this.lookTime;
	}
}
