package noppes.npcs.ai.target;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class EntityAIWatchClosest extends EntityAIBase {

	private final float chance;
	protected Entity closestEntity;
	private int lookTime;
	private final float maxDistance;
	private final EntityNPCInterface npc;
	private final Class<?> watchedClass;

	public EntityAIWatchClosest(EntityNPCInterface npcIn, Class<?> watchedClassIn, float maxDistanceIn) {
		npc = npcIn;
		watchedClass = watchedClassIn;
		maxDistance = maxDistanceIn;
		chance = 0.002f;
		setMutexBits(AiMutex.LOOK);
	}

	@Override
	public void resetTask() {
		closestEntity = null;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !npc.isInteracting() && !npc.isAttacking() && closestEntity.isEntityAlive()
				&& npc.isEntityAlive() && npc.isInRange(closestEntity, maxDistance)
				&& lookTime > 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean shouldExecute() {
		if (npc.getRNG().nextFloat() >= chance || npc.isInteracting()) { return false; }
		if (npc.getAttackTarget() != null) { closestEntity = npc.getAttackTarget(); }
		else {
			if (npc.isMoving() || npc.ais.getStandingType() != 0 && npc.ais.getStandingType() != 2) { return false; }
			if (watchedClass == EntityPlayer.class) { closestEntity = npc.world.getClosestPlayerToEntity(npc, maxDistance); }
			else { closestEntity = npc.world.findNearestEntityWithinAABB((Class<Entity>) watchedClass, npc.getEntityBoundingBox().grow(maxDistance, 3.0, maxDistance), npc); }
		}
		if (closestEntity != null) {
			if (closestEntity instanceof EntityLivingBase) { return Util.instance.npcCanSeeTarget(npc, (EntityLivingBase) closestEntity, false, false); }
			return npc.canSee(closestEntity);
		}
		return false;
	}

	@Override
	public void startExecuting() {
		lookTime = 60 + npc.getRNG().nextInt(60);
	}

	@Override
	public void updateTask() {
		npc.getLookHelper().setLookPosition(closestEntity.posX, closestEntity.posY + closestEntity.getEyeHeight(), closestEntity.posZ, 10.0f, npc.getVerticalFaceSpeed());
		--lookTime;
	}

}
