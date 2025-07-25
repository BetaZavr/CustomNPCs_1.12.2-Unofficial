package noppes.npcs.ai.target;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIOwnerHurtByTarget extends EntityAITarget {
	EntityNPCInterface npc;
	EntityLivingBase theOwnerAttacker;
	private int timer;

	public EntityAIOwnerHurtByTarget(EntityNPCInterface npc) {
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
		this.theOwnerAttacker = entitylivingbase.getRevengeTarget();
		int i = entitylivingbase.getRevengeTimer();
		CustomNpcs.debugData.end(npc);
		return i != this.timer && this.isSuitableTarget(this.theOwnerAttacker, false);
	}

	public void startExecuting() {
		CustomNpcs.debugData.start(npc);
		this.taskOwner.setAttackTarget(this.theOwnerAttacker);
		EntityLivingBase entitylivingbase = this.npc.getOwner();
		if (entitylivingbase != null) {
			this.timer = entitylivingbase.getRevengeTimer();
		}
		super.startExecuting();
		CustomNpcs.debugData.end(npc);
	}
}
