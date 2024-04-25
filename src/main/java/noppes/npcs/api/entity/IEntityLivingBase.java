package noppes.npcs.api.entity;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.entity.data.INpcAttribute;
import noppes.npcs.api.item.IItemStack;

public interface IEntityLivingBase<T extends EntityLivingBase> extends IEntity<T> {

	INpcAttribute addAttribute(INpcAttribute attribute);

	INpcAttribute addAttribute(String attributeName, String displayName, double baseValue, double minValue,
			double maxValue);

	IMark addMark(int type);

	void addPotionEffect(int effect, int duration, int strength, boolean hideParticles);

	boolean canSeeEntity(IEntity<?> entity);

	void clearPotionEffects();

	IItemStack getArmor(int slot);

	IEntityLivingBase<?> getAttackTarget();

	float getHealth();

	INpcAttribute getIAttribute(String attributeName);

	String[] getIAttributeNames();

	INpcAttribute[] getIAttributes();

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

	int getPotionEffect(int effect);

	boolean hasAttribute(INpcAttribute attribute);

	boolean hasAttribute(String attributeName);

	boolean isAttacking();

	boolean isChild();

	boolean removeAttribute(INpcAttribute attribute);

	boolean removeAttribute(String attributeName);

	void removeMark(IMark mark);

	void setArmor(int slot, IItemStack item);

	void setAttackTarget(IEntityLivingBase<?> living);

	void setHealth(float health);

	void setMainhandItem(IItemStack item);

	void setMaxHealth(float health);

	void setMoveForward(float move);

	void setMoveStrafing(float move);

	void setMoveVertical(float move);

	void setOffhandItem(IItemStack item);

	void swingMainhand();

	void swingOffhand();

}
