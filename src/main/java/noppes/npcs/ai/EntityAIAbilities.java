package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.ability.AbstractAbility;
import noppes.npcs.ability.IAbilityUpdate;
import noppes.npcs.constants.EnumAbilityType;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAbilities extends EntityAIBase {
	private IAbilityUpdate ability;
	private EntityNPCInterface npc;

	public EntityAIAbilities(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public void resetTask() {
		((AbstractAbility) this.ability).endAbility();
		this.ability = null;
	}

	public boolean shouldContinueExecuting() {
		return this.npc.isAttacking() && this.ability.isActive();
	}

	public boolean shouldExecute() {
		if (!this.npc.isAttacking()) {
			return false;
		}
		this.ability = (IAbilityUpdate) this.npc.abilities.getAbility(EnumAbilityType.UPDATE);
		return this.ability != null;
	}

	public void updateTask() {
		this.ability.update();
	}
}
