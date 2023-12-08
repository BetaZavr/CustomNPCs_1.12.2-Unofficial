package noppes.npcs.ai.attack;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.CustomNpcs;
import noppes.npcs.util.AdditionalMethods;

public class EntityAIHitAndRun
extends EntityAICustom {

	private int[] dodgePos;
	
	public EntityAIHitAndRun(IRangedAttackMob npc) {
		super(npc);
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.npc.ticksExisted % (this.tickRate * 2) > 3) { return; }
		if (this.isRanged) { this.canSeeToAttack = AdditionalMethods.npcCanSeeTarget(this.npc, this.target, true); }
		else { this.canSeeToAttack = this.npc.canSee(this.target); }
		if (this.canSeeToAttack && this.distance <= this.range) {
			if (this.inMove) {
				if (this.dodgePos == null) { this.npc.getNavigator().clearPath(); }
				else {
					PathPoint point = this.npc.getNavigator().getPath().getFinalPathPoint();
					if (point == null ||
							point.x < this.dodgePos[0] - 2 && point.x > this.dodgePos[0] + 2 ||
							point.y < this.dodgePos[1] - 2 && point.y > this.dodgePos[1] + 2 ||
							point.z < this.dodgePos[2] - 2 && point.z > this.dodgePos[2] + 2) {
						this.dodgePos = null;
						this.npc.getNavigator().clearPath();
					}
				}
			}
		}
		else {
			if (!this.inMove) {
				this.dodgePos = null;
				this.tryMoveToTarget();
			}
		}
		this.tryToCauseDamage();
		
		if (this.hasAttack) {
			Path path = null;
			double[] pos = null;
			this.dodgePos = null;
			if (!this.isRanged || this.distance < this.tacticalRange) {
				pos = AdditionalMethods.instance.getPosition(this.target.posX, this.target.posY, this.target.posZ, this.target.rotationYaw + 180.0f, this.target.rotationPitch, this.tacticalRange);
				path = this.npc.getNavigator().getPathToXYZ(pos[0], pos[1], pos[2]);
				if (path == null) {
					Vec3d vec = RandomPositionGenerator.findRandomTarget(this.npc, this.tacticalRange, 2);
					if (vec != null) { path = this.npc.getNavigator().getPathToXYZ(vec.x, vec.y, vec.z); }
				}
			}
			if (path == null) {
				double dist = 0.0d;
				int error = 0, attempts = 0;
				while ((dist < this.tacticalRange ||
						dist < (double) (this.isRanged ? this.range / 2.0d : this.range) ||
						dist > this.npc.stats.aggroRange)
						&& error < 3 && attempts < 8) {
					attempts ++;
					Vec3d vec = RandomPositionGenerator.findRandomTarget(this.npc, this.tacticalRange, 2);
					if (vec == null) { error++; continue; }
					error = 0;
					dist = this.npc.getDistance(vec.x, vec.y, vec.z);
					if (this.npc.stats.calmdown) {
						double homeDist = AdditionalMethods.instance.distanceTo(this.npc.getBackPos().getX(), this.npc.getBackPos().getY(), this.npc.getBackPos().getZ(), vec.x, vec.y, vec.z);
						if (homeDist > CustomNpcs.NpcNavRange) { continue; }
					}
					if ((int) vec.x == this.npc.getPosition().getX() &&
							(int) vec.y == this.npc.getPosition().getY() &&
							(int) vec.x == this.npc.getPosition().getZ()) {
						dist = 0.0d;
					}
					else {
						path = this.npc.getNavigator().getPathToXYZ(vec.x, vec.y, vec.z);
						if (path == null) { dist = 0.0d; }
						else {
							if (this.dodgePos == null) { this.dodgePos = new int[] { (int) vec.x, (int) vec.y, (int) vec.z }; }
							else {
								this.dodgePos[0] = (int) vec.x; 
								this.dodgePos[1] = (int) vec.y;
								this.dodgePos[2] = (int) vec.z;
							}
						}
					}
				}
			}
			if (path != null) {
				this.npc.getNavigator().setPath(path, 1.3d);
				PathPoint point = path.getFinalPathPoint();
				if (point != null) {
					if (this.dodgePos == null) { this.dodgePos = new int[] { point.x, point.y, point.z }; }
					else {
						this.dodgePos[0] = (int) point.x; 
						this.dodgePos[1] = (int) point.y;
						this.dodgePos[2] = (int) point.z;
					}
				}
			}
		}
	}
	
}