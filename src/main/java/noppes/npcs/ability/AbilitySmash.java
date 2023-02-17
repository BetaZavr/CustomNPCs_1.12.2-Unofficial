package noppes.npcs.ability;

import noppes.npcs.constants.EnumAbilityType;
import noppes.npcs.entity.EntityNPCInterface;

public class AbilitySmash extends AbstractAbility implements IAbilityUpdate {
	public AbilitySmash(EntityNPCInterface entity) {
		super(entity);
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isType(EnumAbilityType type) {
		return type == EnumAbilityType.ATTACKED || type == EnumAbilityType.UPDATE;
	}

	@Override
	public void update() {
	}
}
