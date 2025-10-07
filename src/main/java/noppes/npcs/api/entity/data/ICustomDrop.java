package noppes.npcs.api.entity.data;

import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.Availability;

@SuppressWarnings("all")
public interface ICustomDrop {

	IAttributeSet addAttribute(String attributeName);

	IDropNbtSet addDropNbtSet(int type, double chance, String path, String[] values);

	IEnchantSet addEnchant(int enchantId);

	IEnchantSet addEnchant(String enchantName);

	IItemStack createLoot(double addChance);

	IAttributeSet[] getAttributeSets();

	double getChance();

	float getDamage();

	IDropNbtSet[] getDropNbtSets();

	IEnchantSet[] getEnchantSets();

	IItemStack getItem();

	int getLootMode();

	int getMaxAmount();

	int getMinAmount();

    Availability getAvailability();

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

	void setLootMode(int lootMode);

	void setTiedToLevel(boolean tiedToLevel);

}
