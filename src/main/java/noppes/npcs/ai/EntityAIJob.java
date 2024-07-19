package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIJob extends EntityAIBase {

	private final EntityNPCInterface npc;

	public EntityAIJob(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public int getMutexBits() {
		if (this.npc.advanced.jobInterface == null) {
			return super.getMutexBits();
		}
		return this.npc.advanced.jobInterface.getMutexBits();
	}

	public void resetTask() {
		if (this.npc.advanced.jobInterface != null) {
			this.npc.advanced.jobInterface.resetTask();
		}
	}

	public boolean shouldContinueExecuting() {
		return !this.npc.isKilled() && this.npc.advanced.jobInterface != null
				&& this.npc.advanced.jobInterface.aiContinueExecute();
	}

	public boolean shouldExecute() {
		return !this.npc.isKilled() && this.npc.advanced.jobInterface != null
				&& this.npc.advanced.jobInterface.aiShouldExecute();
	}

	public void startExecuting() {
		this.npc.advanced.jobInterface.aiStartExecuting();
	}

	public void updateTask() {
		if (this.npc.advanced.jobInterface != null) {
			this.npc.advanced.jobInterface.aiUpdateTask();
		}
	}
}
