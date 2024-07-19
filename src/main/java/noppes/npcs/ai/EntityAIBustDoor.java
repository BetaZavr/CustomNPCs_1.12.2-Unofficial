package noppes.npcs.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIDoorInteract;
import net.minecraft.util.EnumHand;

public class EntityAIBustDoor extends EntityAIDoorInteract {

	private int breakingTime;
	private int previousBreakProgress;

	public EntityAIBustDoor(EntityLiving par1EntityLiving) {
		super(par1EntityLiving);
		this.previousBreakProgress = -1;
	}

	public void resetTask() {
		super.resetTask();
		this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, -1);
	}

	public boolean shouldContinueExecuting() {
		double var1 = this.entity.getDistanceSq(this.doorPosition);
		return this.breakingTime <= 240 && !BlockDoor.isOpen(this.entity.world, this.doorPosition)
				&& var1 < 4.0;
	}

	public boolean shouldExecute() {
		return super.shouldExecute() && !BlockDoor.isOpen(this.entity.world, this.doorPosition);
	}

	public void startExecuting() {
		super.startExecuting();
		this.breakingTime = 0;
	}

	public void updateTask() {
		super.updateTask();
		if (this.entity.getRNG().nextInt(20) == 0) {
			this.entity.world.playEvent(null, 1010, this.doorPosition, 0);
			this.entity.swingArm(EnumHand.MAIN_HAND);
		}
		++this.breakingTime;
		int var1 = (int) (this.breakingTime / 240.0f * 10.0f);
		if (var1 != this.previousBreakProgress) {
			this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, var1);
			this.previousBreakProgress = var1;
		}
		if (this.breakingTime == 240) {
			this.entity.world.setBlockToAir(this.doorPosition);
			this.entity.world.playEvent(null, 1012, this.doorPosition, 0);
			this.entity.world.playEvent(null, 2001, this.doorPosition,
					Block.getIdFromBlock(this.doorBlock));
		}
	}
}
