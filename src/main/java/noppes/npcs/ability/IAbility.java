package noppes.npcs.ability;

import net.minecraft.entity.EntityLivingBase;

public interface IAbility {
	
	boolean canRun(EntityLivingBase target);
	
	int getRNG();
	
	void startCombat();
	
	void endAbility();
	
}
