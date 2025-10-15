package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.Availability;

@SuppressWarnings("all")
public interface ICustomDrop {

	IAttributeSet addAttribute(@ParamName("attributeName") String attributeName);

	IDropNbtSet addDropNbtSet(@ParamName("type") int type, @ParamName("chance") double chance,
							  @ParamName("path") String path, @ParamName("values") String[] values);

	IEnchantSet addEnchant(@ParamName("enchantId") int enchantId);

	IEnchantSet addEnchant(@ParamName("enchantName") String enchantName);

	IItemStack createLoot(@ParamName("addChance") double addChance);

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

	void removeAttribute(@ParamName("attribute") IAttributeSet attribute);

	void removeDropNbt(@ParamName("nbt") IDropNbtSet nbt);

	void removeEnchant(@ParamName("enchant") IEnchantSet enchant);

	void resetTo(@ParamName("item") IItemStack item);

	void setAmount(@ParamName("min") int min, @ParamName("max") int max);

	void setChance(@ParamName("chance") double chance);

	void setDamage(@ParamName("damage") float damage);

	void setItem(@ParamName("item") IItemStack item);

	void setLootMode(@ParamName("lootMode") int lootMode);

	void setTiedToLevel(@ParamName("tiedToLevel") boolean tiedToLevel);

}
