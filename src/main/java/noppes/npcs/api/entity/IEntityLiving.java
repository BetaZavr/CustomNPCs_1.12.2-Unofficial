package noppes.npcs.api.entity;

import net.minecraft.entity.EntityLiving;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IEntityLiving<T extends EntityLiving> extends IEntityLivingBase<T> {

	void clearNavigation();

	T getMCEntity();

	IPos getNavigationPath();

	boolean isNavigating();

	void jump();

	void navigateTo(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z, @ParamName("speed") double speed);

	void navigateTo(@ParamName("attribute") Integer[][] posses, @ParamName("attribute") double speed);

}
