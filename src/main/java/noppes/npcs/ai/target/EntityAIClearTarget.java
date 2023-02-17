package noppes.npcs.ai.target;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIClearTarget extends EntityAIBase {
	private EntityNPCInterface npc;
	private EntityLivingBase target;

	public EntityAIClearTarget(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public void resetTask() {
		this.npc.getNavigator().clearPath();
	}

	public boolean shouldExecute() {
		this.target = this.npc.getAttackTarget();
		return this.target != null && ((this.npc.getOwner() != null
				&& !this.npc.isInRange(this.npc.getOwner(), this.npc.stats.aggroRange * 2))
				|| this.npc.combatHandler.checkTarget());
	}

	public void startExecuting() {
		this.npc.setAttackTarget(null);
		if (this.target == this.npc.getRevengeTarget()) {
			this.npc.setRevengeTarget(null);
		}
		super.startExecuting();
	}
}
