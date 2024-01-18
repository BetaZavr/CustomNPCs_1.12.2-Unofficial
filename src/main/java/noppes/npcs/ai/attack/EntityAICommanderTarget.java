package noppes.npcs.ai.attack;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class EntityAICommanderTarget
extends EntityAICustom {

	public int baseAnimation;
	
	private final List<EntityNPCInterface> npcs;
	private boolean done;
	private int time;
	private double minDist;
	
	public EntityAICommanderTarget(IRangedAttackMob npc) {
		super(npc);
		this.npcs = Lists.<EntityNPCInterface>newArrayList();
		this.done = false;
		this.baseAnimation = this.npc.currentAnimation;
		this.time = 0;
		this.npc.aiOwnerNPC = null;
	}
	@Override
	public boolean shouldExecute() {
		if (super.shouldExecute()) { return true; }
		this.reset();
		return false;
	}
	
	@Override
	public void updateTask() {
		super.updateTask();
		if (this.npc.ticksExisted % (this.tickRate * 2) > 3) { return; }
		if (this.isRanged) { this.canSeeToAttack = AdditionalMethods.npcCanSeeTarget(this.npc, this.target, true); }
		else { this.canSeeToAttack = this.npc.canSee(this.target); }
		
		if (this.done) {
			if (this.canSeeToAttack && this.distance <= this.range) {
				if (this.inMove) { this.npc.getNavigator().clearPath(); }
			}
			else { this.tryMoveToTarget(); }
			this.tryToCauseDamage();
		} else {
			// target is close
			if (this.canSeeToAttack && this.distance <= this.range && this.distance <= this.tacticalRange) {
				this.attack();
				return;
			}
			// collect npc
			if (this.npcs.isEmpty()) {
				AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(this.npc.getPosition()).grow(this.tacticalRange, this.tacticalRange, this.tacticalRange);
				for (EntityNPCInterface n : this.npc.world.getEntitiesWithinAABB(EntityNPCInterface.class, bb)) {
					if (this.npc.equals(n)) { continue; }
					if (this.npc.getFaction().id == n.getFaction().id &&
							n.getAttackTarget() == null &&
							(n.ais.onAttack == 0 || n.ais.onAttack == 2) &&
							n.aiOwnerNPC == null) {
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
				this.time = this.tacticalRange < 5 ? 18 : (int) (4.90909f * (float) this.tacticalRange - 6.54545f); // min 3 sec, range==16 - 11 sec
			}
			else { // checking the distance to friends
				boolean isStart = true;
				for (EntityNPCInterface n : this.npcs) {
					if (n.aiOwnerNPC == null) { n.aiOwnerNPC = this.npc; }
					float dist = this.npc.getDistance(n);
					if (dist > this.minDist) {
						isStart = false;
						n.getNavigator().tryMoveToEntityLiving(this.npc, 1.0d);
					}
					else if (dist < 1.5d) { n.getNavigator().clearPath(); }
				}
				this.time --;
				if (isStart || this.time <= 0) { this.attack(); }
			}
		}
	}

	private void attack() {
		this.done = true;
		this.time = 0;
		if (this.npc.currentAnimation != this.baseAnimation) { this.npc.setCurrentAnimation(this.baseAnimation); }
		for (EntityNPCInterface n : this.npcs) {
			n.aiOwnerNPC = null;
			n.setAttackTarget(this.target);
			if (n.aiAttackTarget instanceof EntityAICommanderTarget) { ((EntityAICommanderTarget) n.aiAttackTarget).done = true; }
		}
		this.npcs.clear();
	}
	
	private void reset() {
		this.done = false;
		this.time = 0;
		if (this.npc.currentAnimation != this.baseAnimation) { this.npc.setCurrentAnimation(this.baseAnimation); }
		for (EntityNPCInterface n : this.npcs) {
			n.aiOwnerNPC = null;
			if (n.ais.returnToStart) {
				n.runBack();
			}
		}
		this.npcs.clear();
	}
	
}
