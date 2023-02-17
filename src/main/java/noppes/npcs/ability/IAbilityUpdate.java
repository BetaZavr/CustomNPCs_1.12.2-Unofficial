package noppes.npcs.ability;

public interface IAbilityUpdate extends IAbility {
	boolean isActive();

	void update();
}
