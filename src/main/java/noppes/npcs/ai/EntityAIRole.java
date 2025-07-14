package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIRole extends EntityAIBase {

	private final EntityNPCInterface npc;

	public EntityAIRole(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public boolean shouldContinueExecuting() {
		return !this.npc.isKilled() && this.npc.advanced.roleInterface != null
				&& this.npc.advanced.roleInterface.aiContinueExecute();
	}

	public boolean shouldExecute() {
		return !this.npc.isKilled() && this.npc.advanced.roleInterface != null
				&& this.npc.advanced.roleInterface.aiShouldExecute();
	}

	public void startExecuting() {
		this.npc.advanced.roleInterface.aiStartExecuting();
	}

	public void updateTask() {
		CustomNpcs.debugData.start(npc, this, "updateTask");
		if (this.npc.advanced.roleInterface != null) {
			this.npc.advanced.roleInterface.aiUpdateTask();
		}
		CustomNpcs.debugData.end(npc, this, "updateTask");
	}

}
