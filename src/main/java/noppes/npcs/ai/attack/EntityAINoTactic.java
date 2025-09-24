package noppes.npcs.ai.attack;

import net.minecraft.entity.IRangedAttackMob;
import noppes.npcs.util.Util;

public class EntityAINoTactic extends EntityAICustom {

	public EntityAINoTactic(IRangedAttackMob npc) {
		super(npc);
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.isFriend || this.npc.ticksExisted % (this.tickRate * 2) > 3) {
			return;
		}
		if (this.isRanged) {
			this.canSeeToAttack = this.npc.canSee(this.target);
			if (this.canSeeToAttack && this.distance <= this.range) {
				if (this.inMove) {
					this.npc.getNavigator().clearPath();
				}
			} else {
				this.tryMoveToTarget();
			}
		} else {
			this.canSeeToAttack = this.npc.canSee(this.target);
			this.tryMoveToTarget();
		}
		this.tryToCauseDamage();
	}

}
