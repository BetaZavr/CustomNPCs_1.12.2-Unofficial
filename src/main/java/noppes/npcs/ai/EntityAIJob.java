package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIJob
 extends EntityAIBase {
	 
	private EntityNPCInterface npc;

	public EntityAIJob(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public int getMutexBits() {
		if (this.npc.jobInterface == null) {
			return super.getMutexBits();
		}
		return this.npc.jobInterface.getMutexBits();
	}

	public void resetTask() {
		if (this.npc.jobInterface != null) {
			this.npc.jobInterface.resetTask();
		}
	}

	public boolean shouldContinueExecuting() {
		return !this.npc.isKilled() && this.npc.jobInterface != null && this.npc.jobInterface.aiContinueExecute();
	}

	public boolean shouldExecute() {
		return !this.npc.isKilled() && this.npc.jobInterface != null && this.npc.jobInterface.aiShouldExecute();
	}

	public void startExecuting() {
		this.npc.jobInterface.aiStartExecuting();
	}

	public void updateTask() {
		if (this.npc.jobInterface != null) {
			this.npc.jobInterface.aiUpdateTask();
		}
	}
}
