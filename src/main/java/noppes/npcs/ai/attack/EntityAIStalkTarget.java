package noppes.npcs.ai.attack;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.Util;

public class EntityAIStalkTarget extends EntityAICustom {

	public boolean discovered;
	private EntityLivingBase oldTarget;

	public EntityAIStalkTarget(IRangedAttackMob npc) {
		super(npc);
		this.discovered = false;
	}

	private void setDiscovered(boolean discovered) {
		this.discovered = discovered;
		if (this.npc.aiIsSneak == this.discovered) {
			this.npc.aiIsSneak = !this.discovered;
			this.npc.updateAiClient();
		}
	}

	@Override
	public boolean shouldExecute() {
		if (super.shouldExecute()) {
			return true;
		}
		if (this.discovered) {
			this.oldTarget = null;
			this.setDiscovered(false);
		}
		return false;
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.isFriend || this.npc.ticksExisted % (this.tickRate * 2) > 3) {
			return;
		}
		if (this.isRanged) {
			this.canSeeToAttack = Util.instance.npcCanSeeTarget(this.npc, this.target, true, true);
		} else {
			this.canSeeToAttack = this.npc.canSee(this.target);
		}

		if (!this.discovered && this.distance < this.tacticalRange) {
			this.setDiscovered(true);
		}
		if (this.canSeeToAttack && this.distance <= this.range) {
			if (this.inMove) {
				this.npc.getNavigator().clearPath();
			}
		} else {
			this.npc.getNavigator().tryMoveToEntityLiving(this.target, this.discovered ? 1.3d : 0.725d);
		}
		this.tryToCauseDamage();
		if (!this.discovered && this.hasAttack
				|| Util.instance.npcCanSeeTarget(this.target, this.npc, true, true)) {
			this.setDiscovered(true);
		}
		if (!this.target.equals(this.oldTarget)) {
			this.oldTarget = this.target;
			this.setDiscovered(this.discovered);
		}
	}

	@Override
	public void writeToClientNBT(NBTTagCompound compound) {
		compound.setBoolean("aiIsSneak", !this.discovered);
	}

}
