package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAITransform extends EntityAIBase {

	private final EntityNPCInterface npc;

	public EntityAITransform(EntityNPCInterface npc) {
		this.npc = npc;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	public boolean shouldExecute() {
		boolean isDay = npc.world.getWorldTime() % 24000L < 12000L;
		return !npc.isKilled() && !npc.isAttacking() && npc.transform.editingModus && isDay == npc.transform.isDay;
	}

	public void startExecuting() {
		npc.transform.transform(!npc.transform.isDay);
	}

}
