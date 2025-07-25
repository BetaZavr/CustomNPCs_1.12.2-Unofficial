package noppes.npcs.ai.target;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIOwnerHurtTarget extends EntityAITarget {
	EntityNPCInterface npc;
	EntityLivingBase theTarget;
	private int timestamp;

	public EntityAIOwnerHurtTarget(EntityNPCInterface npc) {
		super(npc, false);
		this.npc = npc;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	public boolean shouldExecute() {
		CustomNpcs.debugData.start(npc);
		if (!this.npc.isFollower() || this.npc.advanced.roleInterface == null
				|| this.npc.advanced.roleInterface.defendOwner()) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		EntityLivingBase entitylivingbase = this.npc.getOwner();
		if (entitylivingbase == null) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		this.theTarget = entitylivingbase.getLastAttackedEntity();
		int i = entitylivingbase.getLastAttackedEntityTime();
		CustomNpcs.debugData.end(npc);
		return i != this.timestamp && this.isSuitableTarget(this.theTarget, false);
	}

	public void startExecuting() {
		CustomNpcs.debugData.start(npc);
		this.npc.setAttackTarget(this.theTarget);
		EntityLivingBase entitylivingbase = this.npc.getOwner();
		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getLastAttackedEntityTime();
		}
		super.startExecuting();
		CustomNpcs.debugData.end(npc);
	}
}
