package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAnimation extends EntityAIBase {
	
	public static int getWalkingAnimationGuiIndex(int animation) {
		if (animation == 4) {
			return 1;
		}
		if (animation == 6) {
			return 2;
		}
		if (animation == 5) {
			return 3;
		}
		if (animation == 7) {
			return 4;
		}
		if (animation == 3) {
			return 5;
		}
		return 0;
	}

	public static boolean isWalkingAnimation(int animation) {
		return getWalkingAnimationGuiIndex(animation) != 0;
	}

	private boolean hasPath;
	private boolean isAtStartpoint;
	private boolean isAttacking;
	private boolean isDead;

	private EntityNPCInterface npc;

	public int temp;

	public EntityAIAnimation(EntityNPCInterface npc) {
		this.isAttacking = false;
		this.isDead = false;
		this.isAtStartpoint = false;
		this.hasPath = false;
		this.temp = 0;
		this.npc = npc;
	}

	private boolean hasNavigation() {
		return this.isAttacking || (this.npc.ais.shouldReturnHome() && !this.isAtStartpoint && !this.npc.isFollower())
				|| this.hasPath;
	}

	public void resetTask() {
	}

	private void setAnimation(int animation) {
		this.npc.setCurrentAnimation(animation);
		this.npc.updateHitbox();
		this.npc.setPosition(this.npc.posX, this.npc.posY, this.npc.posZ);
	}

	public boolean shouldExecute() {
		this.isDead = !this.npc.isEntityAlive();
		if (this.isDead) {
			return this.npc.currentAnimation != 2;
		}
		if (this.npc.stats.ranged.getHasAimAnimation() && this.npc.isAttacking()) {
			return this.npc.currentAnimation != 6;
		}
		this.hasPath = !this.npc.getNavigator().noPath();
		this.isAttacking = this.npc.isAttacking();
		this.isAtStartpoint = (this.npc.ais.shouldReturnHome() && this.npc.isVeryNearAssignedPlace());
		if (this.temp != 0) {
			if (!this.hasNavigation()) {
				return this.npc.currentAnimation != this.temp;
			}
			this.temp = 0;
		}
		if (this.hasNavigation() && !isWalkingAnimation(this.npc.currentAnimation)) {
			return this.npc.currentAnimation != 0;
		}
		return this.npc.currentAnimation != this.npc.ais.animationType;
	}

	public void updateTask() {
		/*if (this.npc.stats.ranged.getHasAimAnimation() && this.npc.isAttacking()) { // shoot anim
			this.setAnimation(6);
			return;
		}*/
		int type = this.npc.ais.animationType;
		if (this.isDead) {
			type = 2;
		} else if (!isWalkingAnimation(this.npc.ais.animationType) && this.hasNavigation()) {
			type = 0;
		} else if (this.temp != 0) {
			if (this.hasNavigation()) {
				this.temp = 0;
			} else {
				type = this.temp;
			}
		}
		this.setAnimation(type);
	}
}
