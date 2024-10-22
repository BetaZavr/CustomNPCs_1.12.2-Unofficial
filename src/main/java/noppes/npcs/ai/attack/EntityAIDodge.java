package noppes.npcs.ai.attack;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.util.Util;

import java.util.Objects;

public class EntityAIDodge extends EntityAICustom {

	private int[] dodgePos;

	public EntityAIDodge(IRangedAttackMob npc) {
		super(npc);
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.isFriend || this.npc.ticksExisted % (this.tickRate * 2) > 3) {
			return;
		}
		if (this.isRanged) {
			this.canSeeToAttack = Util.instance.npcCanSeeTarget(this.npc, this.target, true, true);
		} else {
			this.canSeeToAttack = this.npc.canSee(this.target);
		}

		if (this.canSeeToAttack && this.distance <= this.range) {
			if (this.inMove) {
				if (this.dodgePos == null) {
					this.npc.getNavigator().clearPath();
				} else {
					PathPoint point = Objects.requireNonNull(this.npc.getNavigator().getPath()).getFinalPathPoint();
					if (point == null || point.x < this.dodgePos[0] - 2 && point.x > this.dodgePos[0] + 2
							|| point.y < this.dodgePos[1] - 2 && point.y > this.dodgePos[1] + 2
							|| point.z < this.dodgePos[2] - 2 && point.z > this.dodgePos[2] + 2) {
						this.dodgePos = null;
						this.npc.getNavigator().clearPath();
					}
				}
			}
		} else {
			if (!this.inMove) {
				this.dodgePos = null;
				this.tryMoveToTarget();
			}
		}
		this.tryToCauseDamage();
		if (hasAttack) {
			double dist = 0.0d;
			int error = 0, attempts = 0;
			this.dodgePos = null;
			Path path = null;
			while ((dist < this.tacticalRange || dist < (this.isRanged ? this.range / 2.0d : this.range)
					|| dist > this.npc.stats.aggroRange) && error < 3 && attempts < 8) {
				attempts++;
				Vec3d vec = RandomPositionGenerator.findRandomTarget(this.npc, this.tacticalRange, 2);
				if (vec == null) {
					error++;
					continue;
				}
				error = 0;
				dist = this.npc.getDistance(vec.x, vec.y, vec.z);
				if ((int) vec.x == this.npc.getPosition().getX() && (int) vec.y == this.npc.getPosition().getY()
						&& (int) vec.x == this.npc.getPosition().getZ()) {
					dist = 0.0d;
				} else {
					path = this.npc.getNavigator().getPathToXYZ(vec.x, vec.y, vec.z);
					if (path == null) {
						dist = 0.0d;
					} else {
						if (this.dodgePos == null) {
							this.dodgePos = new int[] { (int) vec.x, (int) vec.y, (int) vec.z };
						} else {
							this.dodgePos[0] = (int) vec.x;
							this.dodgePos[1] = (int) vec.y;
							this.dodgePos[2] = (int) vec.z;
						}
					}
				}
			}
			if (path != null) {
				this.npc.getNavigator().setPath(path, 1.0d);
			}
		}
	}

}