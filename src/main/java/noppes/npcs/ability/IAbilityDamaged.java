package noppes.npcs.ability;

import noppes.npcs.api.event.NpcEvent;

public interface IAbilityDamaged extends IAbility {
	void handleEvent(NpcEvent.DamagedEvent p0);
}
