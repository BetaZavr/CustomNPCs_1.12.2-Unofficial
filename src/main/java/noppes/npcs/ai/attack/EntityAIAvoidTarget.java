package noppes.npcs.ai.attack;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAvoidTarget extends EntityAICustom {

	private int[] runPos;

	public EntityAIAvoidTarget(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.isFrend || this.npc.ticksExisted % (this.tickRate * 2) > 3) {
			return;
		}
		range = npc.stats.aggroRange / 2;
		tacticalRange = (int) range;
		if (range < 4) {
			range = 4;
		}
		isRanged = distance < range;
		if (!isRanged) {
			return;
		}
		if (this.inMove) {
			if (this.runPos == null) {
				this.npc.getNavigator().clearPath();
			} else {
				PathPoint point = this.npc.getNavigator().getPath().getFinalPathPoint();
				if (point == null || point.x < this.runPos[0] - 2 && point.x > this.runPos[0] + 2
						|| point.y < this.runPos[1] - 2 && point.y > this.runPos[1] + 2
						|| point.z < this.runPos[2] - 2 && point.z > this.runPos[2] + 2) {
					this.runPos = null;
					this.npc.getNavigator().clearPath();
				}
			}
		} else {
			this.runPos = null;
			Path path = null;
			Vec3d vec3d = npc.getPositionEyes(1.0f);

			float yaw = npc.rotationYawHead;
			double xVal = npc.posX - target.posX, zVal = npc.posZ - target.posZ;
			double rad = 180.0d / Math.PI;
			if (xVal == 0.0d) {
				yaw = (float) (target.posZ > npc.posZ ? 180.0d : 0.0d);
			} else if (xVal <= 0.0d) {
				yaw = (float) (90.0d + Math.atan(zVal / xVal) * rad);
			} else {
				yaw = (float) (270.0d + Math.atan(zVal / xVal) * rad);
			}
			yaw %= 360.0d;

			float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
			float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
			float f2 = -MathHelper.cos(-0.0f);
			float f3 = MathHelper.sin(-0.0f);
			Vec3d vec3d2 = new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));

			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * tacticalRange, vec3d2.y * tacticalRange,
					vec3d2.z * tacticalRange);
			RayTraceResult rayTrace = npc.world.rayTraceBlocks(vec3d, vec3d3, npc.ais.avoidsWater, false, true);
			if (rayTrace != null) {
				BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);
				if (npc.getDistance(pos.getX(), pos.getY(), pos.getZ()) >= 4) {
					path = this.npc.getNavigator().getPathToXYZ(pos.getX(), pos.getY(), pos.getZ());
				}
			}
			if (path == null) {
				Vec3d vec = RandomPositionGenerator.findRandomTarget(this.npc, this.tacticalRange, 2);
				if (vec != null) {
					path = this.npc.getNavigator().getPathToXYZ(vec.x, vec.y, vec.z);
				}
			}
			if (path != null) {
				this.npc.getNavigator().setPath(path, 1.3d);
				PathPoint point = path.getFinalPathPoint();
				if (point != null) {
					if (this.runPos == null) {
						this.runPos = new int[] { point.x, point.y, point.z };
					} else {
						this.runPos[0] = (int) point.x;
						this.runPos[1] = (int) point.y;
						this.runPos[2] = (int) point.z;
					}
				}
			}
		}

	}

}
