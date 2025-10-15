package noppes.npcs.api.entity;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.entity.data.INpcAttribute;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IEntityLivingBase<T extends EntityLivingBase> extends IEntity<T> {

	INpcAttribute addAttribute(@ParamName("attribute") INpcAttribute attribute);

	INpcAttribute addAttribute(@ParamName("attributeName") String attributeName, @ParamName("displayName") String displayName,
							   @ParamName("baseValue") double baseValue, @ParamName("minValue") double minValue, @ParamName("maxValue") double maxValue);

	IMark addMark(@ParamName("type") int type);

	void addPotionEffect(@ParamName("entity") int effect, @ParamName("duration") int duration,
						 @ParamName("strength") int strength, @ParamName("hideParticles") boolean hideParticles);

	boolean canSeeEntity(@ParamName("entity") IEntity<?> entity);

	void clearPotionEffects();

	IItemStack getArmor(@ParamName("slot") int slot);

	IEntityLivingBase<?> getAttackTarget();

	float getHealth();

	INpcAttribute getIAttribute(@ParamName("attributeName") String attributeName);

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

	int getPotionEffect(@ParamName("effect") int effect);

	boolean hasAttribute(@ParamName("attribute") INpcAttribute attribute);

	boolean hasAttribute(@ParamName("attributeName") String attributeName);

	boolean isAttacking();

	boolean isChild();

	boolean removeAttribute(@ParamName("attribute") INpcAttribute attribute);

	boolean removeAttribute(@ParamName("attributeName") String attributeName);

	void removeMark(@ParamName("mark") IMark mark);

	void setArmor(@ParamName("slot") int slot, @ParamName("item") IItemStack item);

	void setAttackTarget(@ParamName("living") IEntityLivingBase<?> living);

	void setHealth(@ParamName("health") float health);

	void setMainhandItem(@ParamName("item") IItemStack item);

	void setMaxHealth(@ParamName("health") float health);

	void setMoveForward(@ParamName("move") float move);

	void setMoveStrafing(@ParamName("move") float move);

	void setMoveVertical(@ParamName("move") float move);

	void setOffhandItem(@ParamName("item") IItemStack item);

	void swingMainhand();

	void swingOffhand();

}
