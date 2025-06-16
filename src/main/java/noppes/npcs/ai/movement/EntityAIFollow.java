package noppes.npcs.ai.movement;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIFollow
extends EntityAIBase {
	
	private final EntityNPCInterface npc;
	private EntityLivingBase owner;
	public int updateTick;

	public EntityAIFollow(EntityNPCInterface npcIn) {
		updateTick = 0;
		npc = npcIn;
		setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
	}

	public boolean canExcute() {
		return npc.isEntityAlive() && npc.isFollower() && !npc.isAttacking() && (owner = npc.getOwner()) != null && npc.ais.animationType != 1;
	}

	public void resetTask() {
		owner = null;
		npc.getNavigator().clearPath();
	}

	public boolean shouldContinueExecuting() {
		return !npc.getNavigator().noPath() && !npc.isInRange(owner, npc.followRange()) && canExcute();
	}

	public boolean shouldExecute() {
		return canExcute() && !npc.isInRange(owner, npc.followRange());
	}

	public void startExecuting() {
		updateTick = 10;
	}

	public void updateTask() {
		++updateTick;
		if (updateTick < 10) { return; }
		updateTick = 0;
		npc.getLookHelper().setLookPositionWithEntity(owner, 10.0f, npc.getVerticalFaceSpeed());
		double distance = npc.getDistance(owner);
		double speed = 1.0 + distance / 150.0;
		if (speed > 3.0) {
			speed = 3.0;
		}
		if (owner.isSprinting()) { speed += 0.5; }
		if (npc.getNavigator().tryMoveToEntityLiving(owner, speed) || npc.isInRange(owner, 16.0)) { return; }
		npc.tpTo(owner);
	}
}
