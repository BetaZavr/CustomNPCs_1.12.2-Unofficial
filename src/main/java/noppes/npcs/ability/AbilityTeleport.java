package noppes.npcs.ability;

import noppes.npcs.constants.EnumAbilityType;
import noppes.npcs.entity.EntityNPCInterface;

public class AbilityTeleport extends AbstractAbility implements IAbilityUpdate {
	public AbilityTeleport(EntityNPCInterface entity) {
		super(entity);
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isType(EnumAbilityType type) {
		return type == EnumAbilityType.UPDATE;
	}

	@Override
	public void update() {
	}
}
