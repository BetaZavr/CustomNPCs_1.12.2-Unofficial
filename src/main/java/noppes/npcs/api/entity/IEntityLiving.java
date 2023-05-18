package noppes.npcs.api.entity;

import net.minecraft.entity.EntityLiving;
import noppes.npcs.api.IPos;

public interface IEntityLiving<T extends EntityLiving>
extends IEntityLivingBase<T> {
	
	void clearNavigation();

	T getMCEntity();

	IPos getNavigationPath();

	boolean isNavigating();

	void jump();

	void navigateTo(double x, double y, double z, double speed);
	
}
