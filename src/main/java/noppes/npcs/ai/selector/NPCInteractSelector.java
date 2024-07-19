package noppes.npcs.ai.selector;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;

public class NPCInteractSelector implements Predicate<Entity> {

	private final EntityNPCInterface npc;

	public NPCInteractSelector(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public boolean apply(Entity ob) {
		return ob instanceof EntityNPCInterface && this.isEntityApplicable((EntityNPCInterface) ob);
	}

	public boolean isEntityApplicable(EntityNPCInterface entity) {
		return entity != this.npc && this.npc.isEntityAlive() && !entity.isAttacking()
				&& !this.npc.getFaction().isAggressiveToNpc(entity) && this.npc.ais.stopAndInteract;
	}
}
