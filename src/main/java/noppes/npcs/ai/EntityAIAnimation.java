package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.CustomNpcs;
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

	public static boolean notWalkingAnimation(int animation) {
		return getWalkingAnimationGuiIndex(animation) == 0;
	}

	private boolean hasPath;
	private boolean isAtStartPoint;
	private boolean isAttacking;
	private boolean isDead;

	private final EntityNPCInterface npc;

	public int tempAnimation;

	public EntityAIAnimation(EntityNPCInterface npc) {
		isAttacking = false;
		isDead = false;
		isAtStartPoint = false;
		hasPath = false;
		tempAnimation = 0;
		this.npc = npc;
	}

	private boolean hasNavigation() {
		return isAttacking || (npc.ais.shouldReturnHome() && !isAtStartPoint && !npc.isFollower()) || hasPath;
	}

    private void setAnimation(int animation) {
		npc.setCurrentAnimation(animation);
		npc.updateHitbox();
		npc.setPosition(npc.posX, npc.posY, npc.posZ);
		npc.updateAnimationClient();
	}

	public boolean shouldExecute() {
		CustomNpcs.debugData.start(npc, this, "shouldExecute");
		isDead = !npc.isEntityAlive();
		if (isDead) {
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return npc.currentAnimation != 2;
		}
		if (npc.stats.ranged.getHasAimAnimation() && npc.isAttacking()) {
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return npc.currentAnimation != 6;
		}
		hasPath = !npc.getNavigator().noPath();
		isAttacking = npc.isAttacking();
		isAtStartPoint = (npc.ais.shouldReturnHome() && this.npc.isVeryNearAssignedPlace());
		if (tempAnimation != 0) {
			if (!hasNavigation()) {
				CustomNpcs.debugData.end(npc, this, "shouldExecute");
				return npc.currentAnimation != tempAnimation;
			}
			tempAnimation = 0;
		}
		if (hasNavigation() && notWalkingAnimation(npc.currentAnimation)) {
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return npc.currentAnimation != 0;
		}
		CustomNpcs.debugData.end(npc, this, "shouldExecute");
		return npc.currentAnimation != npc.ais.animationType;
	}

	public void updateTask() {
		CustomNpcs.debugData.start(npc, this, "updateTask");
		int type = npc.ais.animationType;
		if (isDead) {
			type = 2;
		} else if (notWalkingAnimation(npc.ais.animationType) && hasNavigation()) {
			type = 0;
		} else if (tempAnimation != 0) {
			if (this.hasNavigation()) {
				tempAnimation = 0;
			} else {
				type = tempAnimation;
			}
		}
		// if (this.npc.stats.ranged.getHasAimAnimation() && this.npc.isAttacking()) { type = 6; } // <- AI target
		CustomNpcs.debugData.end(npc, this, "updateTask");
		setAnimation(type);
	}
}
