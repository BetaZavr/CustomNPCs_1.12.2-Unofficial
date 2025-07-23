package noppes.npcs.api.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.data.IData;

@SuppressWarnings("all")
public interface IItemStack {

	void addEnchantment(int id, int level);

	void addEnchantment(String name, int level);

	boolean compare(IItemStack item, boolean ignoreNBT);

	IItemStack copy();

	void damageItem(int damage, IEntityLiving<?> living);

	double getAttackDamage();

	double getAttribute(String name);

	String getDisplayName();

	int getFoodLevel();

	int getItemDamage();

	String getItemName();

	INbt getItemNbt();

	String[] getLore();

	int getMaxItemDamage();

	int getMaxStackSize();

	ItemStack getMCItemStack();

	String getName();

	INbt getNbt();

	int getStackSize();

	IData getStoreddata();

	IData getTempdata();

	int getType();

	boolean hasAttribute(String name);

	boolean hasCustomName();

	boolean hasEnchant(int id);

	boolean hasEnchant(String name);

	boolean hasNbt();

	@Deprecated
	boolean isBlock();

	@Deprecated
	boolean isBook();

	boolean isEmpty();

	boolean isEnchanted();

	boolean isWearable();

	boolean removeEnchant(int id);

	boolean removeEnchant(String name);

	void removeNbt();

	@Deprecated
	void setAttribute(String name, double value);

	void setAttribute(String name, double value, int slot);

	void setCustomName(String name);

	void setItemDamage(int value);

	void setLore(String[] lore);

	void setStackSize(int size);

}
