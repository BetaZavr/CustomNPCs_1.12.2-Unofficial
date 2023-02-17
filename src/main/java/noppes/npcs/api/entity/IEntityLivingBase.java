package noppes.npcs.api.entity;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.item.IItemStack;

public interface IEntityLivingBase<T extends EntityLivingBase> extends IEntity<T> {
	IMark addMark(int p0);

	void addPotionEffect(int p0, int p1, int p2, boolean p3);

	boolean canSeeEntity(IEntity<?> p0);

	void clearPotionEffects();

	IItemStack getArmor(int p0);

	IEntityLivingBase<?> getAttackTarget();

	float getHealth();

	IEntityLivingBase<?> getLastAttacked();

	int getLastAttackedTime();

	IItemStack getMainhandItem();

	IMark[] getMarks();

	float getMaxHealth();

	T getMCEntity();

	float getMoveForward();

	float getMoveStrafing();

	float getMoveVertical();

	IItemStack getOffhandItem();

	int getPotionEffect(int p0);

	boolean isAttacking();

	boolean isChild();

	void removeMark(IMark p0);

	void setArmor(int p0, IItemStack p1);

	void setAttackTarget(IEntityLivingBase<?> p0);

	void setHealth(float p0);

	void setMainhandItem(IItemStack p0);

	void setMaxHealth(float p0);

	void setMoveForward(float p0);

	void setMoveStrafing(float p0);

	void setMoveVertical(float p0);

	void setOffhandItem(IItemStack p0);

	void swingMainhand();

	void swingOffhand();
}
