package noppes.npcs.api.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.data.IData;

@SuppressWarnings("all")
public interface IItemStack {

	void addEnchantment(@ParamName("id") int id, @ParamName("level") int level);

	void addEnchantment(@ParamName("name") String name, @ParamName("level") int level);

	boolean compare(@ParamName("name") IItemStack item, @ParamName("name") boolean ignoreNBT);

	IItemStack copy();

	void damageItem(@ParamName("name") int damage, @ParamName("name") IEntityLiving<?> living);

	double getAttackDamage();

	double getAttribute(@ParamName("name") String name);

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

	boolean hasAttribute(@ParamName("name") String name);

	boolean hasCustomName();

	boolean hasEnchant(@ParamName("name") int id);

	boolean hasEnchant(@ParamName("name") String name);

	boolean hasNbt();

	@Deprecated
	boolean isBlock();

	@Deprecated
	boolean isBook();

	boolean isEmpty();

	boolean isEnchanted();

	boolean isWearable();

	boolean removeEnchant(@ParamName("id") int id);

	boolean removeEnchant(@ParamName("name") String name);

	void removeNbt();

	@Deprecated
	void setAttribute(@ParamName("name") String name, @ParamName("value") double value);

	void setAttribute(@ParamName("name") String name, @ParamName("value") double value, @ParamName("slot") int slot);

	void setCustomName(@ParamName("name") String name);

	void setItemDamage(@ParamName("value") int value);

	void setLore(@ParamName("lore") String[] lore);

	void setStackSize(@ParamName("size") int size);

	// New from Unofficial (BetaZavr)
	IEntity<?> getOwner();

	void setOwner(@ParamName("entity") IEntity<?> entity);

}
