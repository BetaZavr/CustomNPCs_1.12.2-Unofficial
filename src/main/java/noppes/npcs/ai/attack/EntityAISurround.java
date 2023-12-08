package noppes.npcs.ai.attack;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.util.AdditionalMethods;

public class EntityAISurround
extends EntityAICustom {

	public EntityAISurround(IRangedAttackMob npc) {
		super(npc);
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.npc.ticksExisted % (this.tickRate * 2) > 3) { return; }
		if (this.isRanged) { this.canSeeToAttack = AdditionalMethods.npcCanSeeTarget(this.npc, this.target, true); }
		else { this.canSeeToAttack = this.npc.canSee(this.target); }
		double tr = this.tacticalRange;
		if (tr > this.range) { tr = this.range; }
		if (!this.canSeeToAttack || this.distance > this.range) { this.tryMoveToTarget(); }
		else if (this.distance <= tr * 0.9d || this.distance >= tr * 1.1d) {
			double[] angles = AdditionalMethods.instance.getAngles3D(this.target.posX, this.target.posY, this.target.posZ, this.npc.posX, this.npc.posY, this.npc.posZ);
			double[] pos = AdditionalMethods.instance.getPosition(this.target.posX, this.target.posY, this.target.posZ, angles[0], angles[1], tr);
			Path path = this.npc.getNavigator().getPathToXYZ(pos[0], pos[1], pos[2]);
			if (path != null) { this.npc.getNavigator().setPath(path, 1.3d); }
			else {
				Vec3d targetVec3 = new Vec3d(this.npc.posX - pos[0], this.npc.posY - pos[1], this.npc.posZ - pos[2]);
				Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.npc, 2, 2, targetVec3);
				if (vec != null) {
					this.npc.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1.3d);
				}
			}
		}
		
		this.tryToCauseDamage();
	}
	
}
