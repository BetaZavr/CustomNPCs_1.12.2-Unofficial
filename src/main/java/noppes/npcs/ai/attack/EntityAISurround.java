package noppes.npcs.ai.attack;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.util.IRayTraceVec;
import noppes.npcs.util.Util;

public class EntityAISurround extends EntityAICustom {

	public EntityAISurround(IRangedAttackMob npc) {
		super(npc);
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.isFriend || this.npc.ticksExisted % (this.tickRate * 2) > 3) {
			return;
		}
		this.canSeeToAttack = this.npc.canSee(this.target);
		double tr = this.tacticalRange;
		if (tr > this.range) {
			tr = this.range;
		}
		if (!this.canSeeToAttack || this.distance > this.range) {
			this.tryMoveToTarget();
		} else if (this.distance <= tr * 0.9d || this.distance >= tr * 1.1d) {
			IRayTraceRotate angles = Util.instance.getAngles3D(this.target.posX, this.target.posY, this.target.posZ, this.npc.posX, this.npc.posY, this.npc.posZ);
			IRayTraceVec pos = Util.instance.getPosition(this.target.posX, this.target.posY, this.target.posZ, angles.getYaw(), angles.getPitch(), tr);
			Path path = this.npc.getNavigator().getPathToXYZ(pos.getX(), pos.getY(), pos.getZ());
			if (path != null) {
				this.npc.getNavigator().setPath(path, 1.3d);
			} else {
				Vec3d targetVec3 = new Vec3d(this.npc.posX - pos.getX(), this.npc.posY - pos.getY(), this.npc.posZ - pos.getZ());
				Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.npc, 2, 2, targetVec3);
				if (vec != null) {
					this.npc.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1.3d);
				}
			}
		}

		this.tryToCauseDamage();
	}

}
