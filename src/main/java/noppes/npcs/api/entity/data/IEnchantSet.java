package noppes.npcs.api.entity.data;

import net.minecraft.enchantment.Enchantment;

public interface IEnchantSet {
	double getChance();

	String getEnchant();

	int getMaxLevel();

	int getMinLevel();

	void remove();

	void setChance(double chance);

	void setEnchant(Enchantment enchant);

	boolean setEnchant(int id);

	boolean setEnchant(String name);

	void setLevels(int min, int max);

}
