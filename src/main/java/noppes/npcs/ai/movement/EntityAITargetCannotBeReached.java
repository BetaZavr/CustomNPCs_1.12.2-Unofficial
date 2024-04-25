package noppes.npcs.ai.movement;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.ai.attack.EntityAICustom;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAITargetCannotBeReached extends EntityAIBase {

	protected final EntityNPCInterface npc;
	protected EntityLivingBase target;
	// private int delay;
	private EntityAICustom aiat;

	public EntityAITargetCannotBeReached(EntityNPCInterface npc) {
		this.npc = npc;
		// this.delay = 0;
		this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
	}

	/*
	 * private Vec3d findHidingSpot() { Vec3d idealPos = null; for (int i = 1; i <=
	 * 8; ++i) { for (int y = -2; y <= 2; ++y) { double k =
	 * MathHelper.floor(this.npc.getEntityBoundingBox().minY + y); for (int x = -i;
	 * x <= i; ++x) { double j = MathHelper.floor(this.npc.posX + x) + 0.5; for (int
	 * z = -i; z <= i; ++z) { double l = MathHelper.floor(this.npc.posZ + z) + 0.5;
	 * if (this.isOpaque((int) j, (int) k, (int) l) && !this.isOpaque((int) j, (int)
	 * k + 1, (int) l) && this.isOpaque((int) j, (int) k + 2, (int) l)) { Vec3d vec1
	 * = new Vec3d(this.target.posX, this.target.posY + this.target.getEyeHeight(),
	 * this.target.posZ); Vec3d vec2 = new Vec3d(j, k + this.npc.getEyeHeight(), l);
	 * RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec1, vec2);
	 * if (movingobjectposition != null && this.shelterX != j && this.shelterY != k
	 * && this.shelterZ != l) { idealPos = new Vec3d(j, k, l); } } } } } if
	 * (idealPos != null) { return idealPos; } } this.delay = 60; return null; }
	 * 
	 * private boolean isOpaque(int x, int y, int z) { return
	 * this.world.getBlockState(new BlockPos(x, y, z)).isOpaqueCube(); }
	 */

	@Override
	public void resetTask() {
		// this.npc.getNavigator().clearPath();
		/*
		 * if (this.npc.getAttackTarget() == null && this.target != null) {
		 * this.npc.setAttackTarget(this.target); } if (!this.npc.isInRange(this.target,
		 * this.distance)) { this.delay = 60; }
		 */
	}

	@Override
	public boolean shouldExecute() {
		this.aiat = this.npc.aiAttackTarget;
		if (this.aiat == null) {
			return false;
		}

		// boolean shouldHide = !this.npc.isInRange(this.target, this.distance);
		// boolean isSeen = this.npc.canSee(this.target);
		// return (!this.npc.getNavigator().noPath() && shouldHide) || (!isSeen &&
		// (shouldHide || this.npc.ais.directLOS));*/
		/*
		 * this.target = this.npc.getAttackTarget(); this.distance =
		 * this.npc.ais.getTacticalRange(); if (this.target == null ||
		 * this.npc.isInRange(this.target, this.distance) ||
		 * !this.npc.canSee(this.target) || this.delay-- > 0) { return false; } Vec3d
		 * vec3 = this.findHidingSpot(); if (vec3 == null) { this.delay = 10; return
		 * false; } this.shelterX = vec3.x; this.shelterY = vec3.y; this.shelterZ =
		 * vec3.z;
		 */
		return false;
	}

	@Override
	public void startExecuting() {
		// this.npc.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY,
		// this.shelterZ, 1.3d);
	}

}
