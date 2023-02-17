package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIRole extends EntityAIBase {
	private EntityNPCInterface npc;

	public EntityAIRole(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public boolean shouldContinueExecuting() {
		return !this.npc.isKilled() && this.npc.roleInterface != null && this.npc.roleInterface.aiContinueExecute();
	}

	public boolean shouldExecute() {
		return !this.npc.isKilled() && this.npc.roleInterface != null && this.npc.roleInterface.aiShouldExecute();
	}

	public void startExecuting() {
		this.npc.roleInterface.aiStartExecuting();
	}

	public void updateTask() {
		if (this.npc.roleInterface != null) {
			this.npc.roleInterface.aiUpdateTask();
		}
	}
}
