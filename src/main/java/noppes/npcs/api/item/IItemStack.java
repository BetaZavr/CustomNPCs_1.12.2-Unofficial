package noppes.npcs.api.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.data.IData;

public interface IItemStack {
	
	void addEnchantment(String p0, int p1);

	boolean compare(IItemStack p0, boolean p1);

	IItemStack copy();

	void damageItem(int p0, IEntityLiving<?> p1);

	double getAttackDamage();

	double getAttribute(String p0);

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

	boolean hasAttribute(String p0);

	boolean hasCustomName();

	boolean hasEnchant(String p0);

	boolean hasNbt();

	@Deprecated
	boolean isBlock();

	@Deprecated
	boolean isBook();

	boolean isEmpty();

	boolean isEnchanted();

	boolean isWearable();

	boolean removeEnchant(String p0);

	void removeNbt();

	@Deprecated
	void setAttribute(String p0, double p1);

	void setAttribute(String p0, double p1, int p2);

	void setCustomName(String p0);

	void setItemDamage(int p0);

	void setLore(String[] p0);

	void setStackSize(int p0);
}
