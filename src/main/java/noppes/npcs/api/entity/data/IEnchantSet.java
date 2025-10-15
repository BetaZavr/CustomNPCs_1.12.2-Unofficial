package noppes.npcs.api.entity.data;

import net.minecraft.enchantment.Enchantment;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IEnchantSet {

	double getChance();

	String getEnchant();

	int getMaxLevel();

	int getMinLevel();

	void remove();

	void setChance(@ParamName("chance") double chance);

	void setEnchant(@ParamName("enchant") Enchantment enchant);

	boolean setEnchant(@ParamName("id") int id);

	boolean setEnchant(@ParamName("name") String name);

	void setLevels(@ParamName("min") int min, @ParamName("max") int max);

}
