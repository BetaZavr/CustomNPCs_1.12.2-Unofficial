package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIStalkTarget extends EntityAIBase {
	private int delay;
	private Vec3d movePosition;
	private EntityNPCInterface npc;
	private boolean overRide;
	private EntityLivingBase targetEntity;
	private int tick;
	private World world;

	public EntityAIStalkTarget(EntityNPCInterface par1EntityCreature) {
		this.tick = 0;
		this.npc = par1EntityCreature;
		this.world = par1EntityCreature.world;
		this.overRide = false;
		this.delay = 0;
		this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
	}

	private Vec3d findSecludedXYZ(int radius, boolean nearest) {
		Vec3d idealPos = null;
		double dist = this.targetEntity.getDistance(this.npc);
		double u = 0.0;
		double v = 0.0;
		double w = 0.0;
		if (this.movePosition != null) {
			u = this.movePosition.x;
			v = this.movePosition.y;
			w = this.movePosition.z;
		}
		for (int y = -2; y <= 2; ++y) {
			double k = MathHelper.floor(this.npc.getEntityBoundingBox().minY + y);
			for (int x = -radius; x <= radius; ++x) {
				double j = MathHelper.floor(this.npc.posX + x) + 0.5;
				for (int z = -radius; z <= radius; ++z) {
					double l = MathHelper.floor(this.npc.posZ + z) + 0.5;
					BlockPos pos = new BlockPos(j, k, l);
					if (this.isOpaque(pos) && !this.isOpaque(pos.up()) && !this.isOpaque(pos.up(2))) {
						Vec3d vec1 = new Vec3d(this.targetEntity.posX,
								this.targetEntity.posY + this.targetEntity.getEyeHeight(), this.targetEntity.posZ);
						Vec3d vec2 = new Vec3d(j, k + this.npc.getEyeHeight(), l);
						RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec1, vec2);
						if (movingobjectposition != null) {
							boolean weight = !nearest || this.targetEntity.getDistanceSq(j, k, l) <= dist;
							if (weight && (j != u || k != v || l != w)) {
								idealPos = new Vec3d(j, k, l);
								if (nearest) {
									dist = this.targetEntity.getDistanceSq(j, k, l);
								}
							}
						}
					}
				}
			}
		}
		return idealPos;
	}

	private Vec3d hideFromTarget() {
		for (int i = 1; i <= 8; ++i) {
			Vec3d vec = this.findSecludedXYZ(i, false);
			if (vec != null) {
				return vec;
			}
		}
		return null;
	}

	private boolean isLookingAway() {
		Vec3d vec3 = this.targetEntity.getLook(1.0f).normalize();
		Vec3d vec4 = new Vec3d(this.npc.posX - this.targetEntity.posX,
				this.npc.getEntityBoundingBox().minY + this.npc.height / 2.0f
						- (this.targetEntity.posY + this.targetEntity.getEyeHeight()),
				this.npc.posZ - this.targetEntity.posZ);
		vec4 = vec4.normalize();
		double d2 = vec3.dotProduct(vec4);
		return d2 < 0.6;
	}

	private boolean isOpaque(BlockPos pos) {
		return this.world.getBlockState(pos).isOpaqueCube();
	}

	public void resetTask() {
		this.npc.getNavigator().clearPath();
		this.npc.resetBackPos(); // New
		if (this.npc.getAttackTarget() == null && this.targetEntity != null) {
			this.npc.setAttackTarget(this.targetEntity);
		}
		if (this.npc.getRangedTask() != null) {
			this.npc.getRangedTask().navOverride(false);
		}
	}

	public boolean shouldExecute() {
		this.targetEntity = this.npc.getAttackTarget();
		return this.targetEntity != null && this.tick-- <= 0
				&& !this.npc.isInRange(this.targetEntity, this.npc.ais.getTacticalRange());
	}

	private Vec3d stalkTarget() {
		for (int i = 8; i >= 1; --i) {
			Vec3d vec = this.findSecludedXYZ(i, true);
			if (vec != null) {
				return vec;
			}
		}
		return null;
	}

	public void startExecuting() {
		if (this.npc.getRangedTask() != null) {
			this.npc.getRangedTask().navOverride(true);
		}
	}

	public void updateTask() {
		this.npc.getLookHelper().setLookPositionWithEntity(this.targetEntity, 30.0f, 30.0f);
		if (this.npc.getNavigator().noPath() || this.overRide) {
			if (this.isLookingAway()) {
				this.movePosition = this.stalkTarget();
				if (this.movePosition != null) {
					this.npc.getNavigator().tryMoveToXYZ(this.movePosition.x, this.movePosition.y, this.movePosition.z,
							1.0);
					this.npc.resetBackPos(); // New
					this.overRide = false;
				} else {
					this.tick = 100;
				}
			} else if (this.npc.canSee(this.targetEntity)) {
				this.movePosition = this.hideFromTarget();
				if (this.movePosition != null) {
					this.npc.getNavigator().tryMoveToXYZ(this.movePosition.x, this.movePosition.y, this.movePosition.z,
							1.33);
					this.npc.resetBackPos(); // New
					this.overRide = false;
				} else {
					this.tick = 100;
				}
			}
		}
		if (this.delay > 0) {
			--this.delay;
		}
		if (!this.isLookingAway() && this.npc.canSee(this.targetEntity) && this.delay == 0) {
			this.overRide = true;
			this.delay = 60;
		}
	}
}
