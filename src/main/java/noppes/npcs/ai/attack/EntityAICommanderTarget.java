package noppes.npcs.ai.attack;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.pathfinding.Path;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class EntityAICommanderTarget extends EntityAICustom {

	public int baseAnimation;

	private final List<EntityNPCInterface> npcs = new ArrayList<>();
	private boolean done = false;
	private int time = 0;
	private double minDist;

	public EntityAICommanderTarget(IRangedAttackMob npc) {
		super(npc);
		this.baseAnimation = this.npc.currentAnimation;
		this.npc.aiOwnerNPC = null;
	}

	private void attack() {
		done = true;
		time = 0;
		if (npc.currentAnimation != baseAnimation) {
			npc.setCurrentAnimation(baseAnimation);
		}
		for (EntityNPCInterface n : this.npcs) {
			n.aiOwnerNPC = null;
			n.setAttackTarget(target);
			if (n.aiAttackTarget instanceof EntityAICommanderTarget) {
				((EntityAICommanderTarget) n.aiAttackTarget).done = true;
			}
		}
		npcs.clear();
	}

	private void reset() {
		this.done = false;
		this.time = 0;
		if (this.npc.currentAnimation != this.baseAnimation) {
			this.npc.setCurrentAnimation(this.baseAnimation);
		}
		for (EntityNPCInterface n : this.npcs) {
			n.aiOwnerNPC = null;
			if (n.ais.returnToStart) {
				n.getNavigator().tryMoveToXYZ(n.getStartXPos(), n.getStartYPos(), n.getStartZPos(), 1.3d);
			}
		}
		this.npcs.clear();
	}

	@Override
	public boolean shouldExecute() {
		if (super.shouldExecute()) {
			return true;
		}
		this.reset();
		return false;
	}

	@Override
	public void updateTask() {
		super.updateTask();
		if (this.isFriend || this.npc.ticksExisted % (this.tickRate * 2) > 3) {
			return;
		}
		this.canSeeToAttack = this.npc.canSee(this.target);
		if (this.done) {
			if (this.canSeeToAttack && this.distance <= this.range) {
				if (this.inMove) {
					this.npc.getNavigator().clearPath();
				}
			} else {
				this.tryMoveToTarget();
			}
			this.tryToCauseDamage();
		} else {
			// target is close
			if (this.canSeeToAttack && this.distance <= this.range && this.distance <= this.tacticalRange) {
				this.attack();
				return;
			}
			// collect npc
			if (this.npcs.isEmpty()) {
				for (EntityNPCInterface n : Util.instance.getEntitiesWithinDist(EntityNPCInterface.class, npc.world, npc, tacticalRange)) {
					if (this.npc.equals(n)) {
						continue;
					}
					if (this.npc.getFaction().id == n.getFaction().id && n.getAttackTarget() == null
							&& (n.ais.onAttack == 0 || n.ais.onAttack == 2) && n.aiOwnerNPC == null) {
						Path path = n.getNavigator().getPathToPos(this.npc.getPosition());
						if (path != null) {
							this.npcs.add(n);
							n.getNavigator().setPath(path, 1.0d);
							n.aiOwnerNPC = this.npc;
						}
					}
				}
				if (this.npcs.isEmpty()) { // no friends
					this.attack();
					return;
				}
				this.npc.setCurrentAnimation(4);
				this.minDist = this.npcs.size() < 5 ? 3.0d : 0.4d * this.npcs.size() + 1.0d;
				this.time = this.tacticalRange < 5 ? 18 : (int) (4.90909f * (float) this.tacticalRange - 6.54545f); // min
																													// 3
																													// sec,
																													// range==16
																													// -
																													// 11
																													// sec
			} else { // checking the distance to friends
				boolean isStart = true;
				for (EntityNPCInterface n : this.npcs) {
					if (n.aiOwnerNPC == null) {
						n.aiOwnerNPC = this.npc;
					}
					float dist = this.npc.getDistance(n);
					if (dist > this.minDist) {
						isStart = false;
						n.getNavigator().tryMoveToEntityLiving(this.npc, 1.0d);
					} else if (dist < 1.5d) {
						n.getNavigator().clearPath();
					}
				}
				this.time--;
				if (isStart || this.time <= 0) {
					this.attack();
				}
			}
		}
	}

}
