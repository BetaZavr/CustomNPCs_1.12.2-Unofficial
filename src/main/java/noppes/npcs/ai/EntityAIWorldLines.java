package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIWorldLines extends EntityAIBase {
	private int cooldown;
	private EntityNPCInterface npc;

	public EntityAIWorldLines(EntityNPCInterface npc) {
		this.cooldown = 100;
		this.npc = npc;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	public boolean shouldExecute() {
		if (this.cooldown > 0) { --this.cooldown; }
		return !this.npc.isAttacking() && !this.npc.isKilled() && this.npc.advanced.hasWorldLines() && this.npc.getRNG().nextInt(1800) == 1;
	}

	public void startExecuting() {
		this.cooldown = 100;
		this.npc.saySurrounding(this.npc.advanced.getWorldLine());
	}
}
