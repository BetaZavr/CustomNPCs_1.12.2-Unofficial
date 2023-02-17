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

public class EntityAIAmbushTarget extends EntityAIBase {
	private int delay;
	private double distance;
	private double movementSpeed;
	private EntityNPCInterface npc;
	private double shelterX;
	private double shelterY;
	private double shelterZ;
	private EntityLivingBase targetEntity;
	private World world;

	public EntityAIAmbushTarget(EntityNPCInterface par1EntityCreature, double par2) {
		this.delay = 0;
		this.npc = par1EntityCreature;
		this.movementSpeed = par2;
		this.world = par1EntityCreature.world;
		this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
	}

	private Vec3d findHidingSpot() {
		Vec3d idealPos = null;
		for (int i = 1; i <= 8; ++i) {
			for (int y = -2; y <= 2; ++y) {
				double k = MathHelper.floor(this.npc.getEntityBoundingBox().minY + y);
				for (int x = -i; x <= i; ++x) {
					double j = MathHelper.floor(this.npc.posX + x) + 0.5;
					for (int z = -i; z <= i; ++z) {
						double l = MathHelper.floor(this.npc.posZ + z) + 0.5;
						if (this.isOpaque((int) j, (int) k, (int) l) && !this.isOpaque((int) j, (int) k + 1, (int) l)
								&& this.isOpaque((int) j, (int) k + 2, (int) l)) {
							Vec3d vec1 = new Vec3d(this.targetEntity.posX,
									this.targetEntity.posY + this.targetEntity.getEyeHeight(), this.targetEntity.posZ);
							Vec3d vec2 = new Vec3d(j, k + this.npc.getEyeHeight(), l);
							RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec1, vec2);
							if (movingobjectposition != null && this.shelterX != j && this.shelterY != k
									&& this.shelterZ != l) {
								idealPos = new Vec3d(j, k, l);
							}
						}
					}
				}
			}
			if (idealPos != null) {
				return idealPos;
			}
		}
		this.delay = 60;
		return null;
	}

	private boolean isOpaque(int x, int y, int z) {
		return this.world.getBlockState(new BlockPos(x, y, z)).isOpaqueCube();
	}

	public void resetTask() {
		this.npc.getNavigator().clearPath();
		if (this.npc.getAttackTarget() == null && this.targetEntity != null) {
			this.npc.setAttackTarget(this.targetEntity);
		}
		if (!this.npc.isInRange(this.targetEntity, this.distance)) {
			this.delay = 60;
		}
	}

	public boolean shouldContinueExecuting() {
		boolean shouldHide = !this.npc.isInRange(this.targetEntity, this.distance);
		boolean isSeen = this.npc.canSee(this.targetEntity);
		return (!this.npc.getNavigator().noPath() && shouldHide) || (!isSeen && (shouldHide || this.npc.ais.directLOS));
	}

	public boolean shouldExecute() {
		this.targetEntity = this.npc.getAttackTarget();
		this.distance = this.npc.ais.getTacticalRange();
		if (this.targetEntity == null || this.npc.isInRange(this.targetEntity, this.distance)
				|| !this.npc.canSee(this.targetEntity) || this.delay-- > 0) {
			return false;
		}
		Vec3d vec3 = this.findHidingSpot();
		if (vec3 == null) {
			this.delay = 10;
			return false;
		}
		this.shelterX = vec3.x;
		this.shelterY = vec3.y;
		this.shelterZ = vec3.z;
		return true;
	}

	public void startExecuting() {
		this.npc.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
	}

	public void updateTask() {
		this.npc.getLookHelper().setLookPositionWithEntity(this.targetEntity, 30.0f, 30.0f);
	}
}
