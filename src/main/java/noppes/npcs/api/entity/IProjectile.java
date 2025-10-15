package noppes.npcs.api.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IProjectile<T extends EntityThrowable> extends IThrowable<T> {

	void enableEvents();

	int getAccuracy();

	boolean getHasGravity();

	IItemStack getItem();

	void setAccuracy(@ParamName("accuracy") int accuracy);

	void setHasGravity(@ParamName("bo") boolean bo);

	void setHeading(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	void setHeading(@ParamName("yaw") float yaw, @ParamName("pitch") float pitch);

	void setHeading(@ParamName("entity") IEntity<?> entity);

	void setItem(@ParamName("item") IItemStack item);

}
