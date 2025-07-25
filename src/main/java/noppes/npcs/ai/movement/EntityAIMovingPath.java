package noppes.npcs.ai.movement;

import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIMovingPath extends EntityAIBase {
	
	private final EntityNPCInterface npc;
	private int[] pos;
	private int retries;

	public EntityAIMovingPath(EntityNPCInterface iNpc) {
		this.retries = 0;
		this.npc = iNpc;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	public boolean shouldContinueExecuting() {
		if ((this.npc.isAttacking() && this.npc.ais.getRetaliateType() != 3) || this.npc.isInteracting()) {
			this.npc.ais.decreaseMovingPath();
			return false;
		}
		if (!this.npc.getNavigator().noPath()) {
			return true;
		}
		this.npc.getNavigator().clearPath();
		if (this.npc.getDistanceSq(this.pos[0], this.pos[1], this.pos[2]) < 3.0) {
			return false;
		}
		if (this.retries++ < 3) {
			this.startExecuting();
			return true;
		}
		return false;
	}

	public boolean shouldExecute() {
		CustomNpcs.debugData.start(npc);
		if ((this.npc.isAttacking() && this.npc.ais.getRetaliateType() != 3) || this.npc.isInteracting()
				|| (this.npc.getRNG().nextInt(40) != 0 && this.npc.ais.movingPause)
				|| !this.npc.getNavigator().noPath()) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		List<int[]> list = this.npc.ais.getMovingPath();
		if (list.size() < 2) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		this.npc.ais.incrementMovingPath();
		this.pos = this.npc.ais.getCurrentMovingPath();
		this.retries = 0;
		CustomNpcs.debugData.end(npc);
		return true;
	}

	public void startExecuting() {
		CustomNpcs.debugData.start(npc);
		this.npc.getNavigator().tryMoveToXYZ(this.pos[0] + 0.5, this.pos[1], this.pos[2] + 0.5, 1.0d);
		CustomNpcs.debugData.end(npc);
	}
}
