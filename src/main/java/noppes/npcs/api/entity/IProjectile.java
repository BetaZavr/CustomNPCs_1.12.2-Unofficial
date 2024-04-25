package noppes.npcs.api.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import noppes.npcs.api.item.IItemStack;

public interface IProjectile<T extends EntityThrowable> extends IThrowable<T> {

	void enableEvents();

	int getAccuracy();

	boolean getHasGravity();

	IItemStack getItem();

	void setAccuracy(int accuracy);

	void setHasGravity(boolean bo);

	void setHeading(double x, double y, double z);

	void setHeading(float yaw, float pitch);

	void setHeading(IEntity<?> entity);

	void setItem(IItemStack item);

}
