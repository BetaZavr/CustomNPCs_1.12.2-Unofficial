package noppes.npcs.ability;

import net.minecraft.entity.EntityLivingBase;

public interface IAbility {

	boolean canRun(EntityLivingBase target);

	void endAbility();

	int getRNG();

	void startCombat();

}
