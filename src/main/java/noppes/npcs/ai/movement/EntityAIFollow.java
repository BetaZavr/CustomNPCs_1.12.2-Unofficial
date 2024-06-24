package noppes.npcs.ai.movement;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIFollow
extends EntityAIBase {
	
	private EntityNPCInterface npc;
	private EntityLivingBase owner;
	public int updateTick;

	public EntityAIFollow(EntityNPCInterface npc) {
		this.updateTick = 0;
		this.npc = npc;
		this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
	}

	public boolean canExcute() {
		return this.npc.isEntityAlive() && this.npc.isFollower() && !this.npc.isAttacking()
				&& (this.owner = this.npc.getOwner()) != null && this.npc.ais.animationType != 1;
	}

	public void resetTask() {
		this.owner = null;
		this.npc.getNavigator().clearPath();
	}

	public boolean shouldContinueExecuting() {
		return !this.npc.getNavigator().noPath() && !this.npc.isInRange(this.owner, 2.0) && this.canExcute();
	}

	public boolean shouldExecute() {
		return this.canExcute() && !this.npc.isInRange(this.owner, this.npc.followRange());
	}

	public void startExecuting() {
		this.updateTick = 10;
	}

	public void updateTask() {
		++this.updateTick;
		if (this.updateTick < 10) {
			return;
		}
		this.updateTick = 0;
		this.npc.getLookHelper().setLookPositionWithEntity(this.owner, 10.0f, this.npc.getVerticalFaceSpeed());
		double distance = this.npc.getDistance(this.owner);
		double speed = 1.0 + distance / 150.0;
		if (speed > 3.0) {
			speed = 3.0;
		}
		if (this.owner.isSprinting()) {
			speed += 0.5;
		}
		if (this.npc.getNavigator().tryMoveToEntityLiving(this.owner, speed) || this.npc.isInRange(this.owner, 16.0)) {
			return;
		}
		this.npc.tpTo(this.owner);
	}
}
