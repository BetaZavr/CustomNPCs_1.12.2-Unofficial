package noppes.npcs.api.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import noppes.npcs.api.item.IItemStack;

public interface IProjectile<T extends EntityThrowable> extends IThrowable<T> {
	void enableEvents();

	int getAccuracy();

	boolean getHasGravity();

	IItemStack getItem();

	void setAccuracy(int p0);

	void setHasGravity(boolean p0);

	void setHeading(double p0, double p1, double p2);

	void setHeading(float p0, float p1);

	void setHeading(IEntity<?> p0);

	void setItem(IItemStack p0);
}
