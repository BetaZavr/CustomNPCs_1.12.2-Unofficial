package noppes.npcs.api.entity.data;

import noppes.npcs.api.item.IItemStack;

public interface ICustomDrop {

	IAttributeSet addAttribute(String attributeName);

	IDropNbtSet addDropNbtSet(int type, double chance, String paht, String[] values);

	IEnchantSet addEnchant(int enchantId);

	IEnchantSet addEnchant(String enchantName);

	IItemStack createLoot(double addChance);

	IAttributeSet[] getAttributeSets();

	double getChance();

	float getDamage();

	IDropNbtSet[] getDropNbtSets();

	IEnchantSet[] getEnchantSets();

	IItemStack getItem();

	boolean getLootMode();

	int getMaxAmount();

	int getMinAmount();

	int getQuestID();

	boolean getTiedToLevel();

	void remove();

	void removeAttribute(IAttributeSet attribute);

	void removeDropNbt(IDropNbtSet nbt);

	void removeEnchant(IEnchantSet enchant);

	void resetTo(IItemStack item);

	void setAmount(int min, int max);

	void setChance(double chance);

	void setDamage(float dam);

	void setItem(IItemStack item);

	void setLootMode(boolean lootMode);

	void setQuestID(int id);

	void setTiedToLevel(boolean tiedToLevel);

}
