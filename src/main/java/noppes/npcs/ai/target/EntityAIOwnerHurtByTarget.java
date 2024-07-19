package noppes.npcs.ai.target;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
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
		if (!this.npc.isFollower() || this.npc.advanced.roleInterface == null
				|| this.npc.advanced.roleInterface.defendOwner()) {
			return false;
		}
		EntityLivingBase entitylivingbase = this.npc.getOwner();
		if (entitylivingbase == null) {
			return false;
		}
		this.theOwnerAttacker = entitylivingbase.getRevengeTarget();
		int i = entitylivingbase.getRevengeTimer();
		return i != this.timer && this.isSuitableTarget(this.theOwnerAttacker, false);
	}

	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.theOwnerAttacker);
		EntityLivingBase entitylivingbase = this.npc.getOwner();
		if (entitylivingbase != null) {
			this.timer = entitylivingbase.getRevengeTimer();
		}
		super.startExecuting();
	}
}
