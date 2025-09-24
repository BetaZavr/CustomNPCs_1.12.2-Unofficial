package noppes.npcs.ai.attack;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.nbt.NBTTagCompound;

public class EntityAIStalkTarget extends EntityAICustom {

	public boolean discovered;
	private EntityLivingBase oldTarget;

	public EntityAIStalkTarget(IRangedAttackMob npc) {
		super(npc);
		discovered = false;
	}

	private void setDiscovered(boolean discoveredIn) {
		discovered = discoveredIn;
		if (npc.aiIsSneak == discovered) {
			npc.aiIsSneak = !discovered;
			npc.setSneaking(!discovered);
		}
	}

	@Override
	public boolean shouldExecute() {
		if (super.shouldExecute()) {
			return true;
		}
		if (discovered) {
			oldTarget = null;
			setDiscovered(false);
		}
		return false;
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (isFriend || npc.ticksExisted % (tickRate * 2) > 3) { return; }
		canSeeToAttack = npc.canSee(target);
		if (!discovered && distance < tacticalRange) { setDiscovered(true); }
		if (canSeeToAttack && distance <= range) {
			if (inMove) { npc.getNavigator().clearPath(); }
		}
		else { npc.getNavigator().tryMoveToEntityLiving(target, discovered ? 1.3d : 0.725d); }
		tryToCauseDamage();
		if (!discovered && hasAttack || target.canEntityBeSeen(npc)) { setDiscovered(true); }
		if (!target.equals(oldTarget)) {
			oldTarget = target;
			setDiscovered(discovered);
		}
	}

	@Override
	public void writeToClientNBT(NBTTagCompound compound) {
		compound.setBoolean("aiIsSneak", !discovered);
	}

}
