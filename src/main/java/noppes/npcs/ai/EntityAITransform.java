package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAITransform extends EntityAIBase {
	private EntityNPCInterface npc;

	public EntityAITransform(EntityNPCInterface npc) {
		this.npc = npc;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	public boolean shouldExecute() {
		return !this.npc.isKilled() && !this.npc.isAttacking() && !this.npc.transform.editingModus
				&& ((this.npc.world.getWorldTime() % 24000L < 12000L) ? this.npc.transform.isActive
						: (!this.npc.transform.isActive));
	}

	public void startExecuting() {
		this.npc.transform.transform(!this.npc.transform.isActive);
	}
}
