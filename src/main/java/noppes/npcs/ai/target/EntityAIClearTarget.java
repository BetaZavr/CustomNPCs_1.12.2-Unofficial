package noppes.npcs.ai.target;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIClearTarget extends EntityAIBase {

	private final EntityNPCInterface npc;
	private EntityLivingBase target;

	public EntityAIClearTarget(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public void resetTask() {
		this.npc.getNavigator().clearPath();
	}

	public boolean shouldExecute() {
		this.target = this.npc.getAttackTarget();
		return this.target != null && ((npc.getOwner() != null
				&& !npc.isInRange(npc.getOwner(), npc.stats.aggroRange * 2))
				|| npc.combatHandler.checkTarget());
	}

	public void startExecuting() {
		this.npc.setAttackTarget(null);
		if (this.target == this.npc.getRevengeTarget()) {
			this.npc.setRevengeTarget(null);
		}
		super.startExecuting();
	}
}
