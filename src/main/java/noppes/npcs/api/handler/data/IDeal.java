package noppes.npcs.api.handler.data;

import net.minecraft.inventory.IInventory;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.item.IItemStack;

public interface IDeal {

	int getAmount();

	IAvailability getAvailability();

	int getChance();

	IContainer getCurrency();

	int getId();

	boolean getIgnoreDamage();

	boolean getIgnoreNBT();

	int getMaxCount();

	IInventory getMCInventoryCurrency();

	IInventory getMCInventoryProduct();

	int getMinCount();

	int getMoney();

	String getName();

	IItemStack getProduct();

	int getType();

	void set(IItemStack product, IItemStack[] currencys);

	void setAmount(int amount);

	void setChance(int chance);

	void setCount(int min, int max);

	void setIgnoreDamage(boolean bo);

	void setIgnoreNBT(boolean bo);

	void setMoney(int money);

	void setProduct(IItemStack product);

	void setType(int type);

	void updateNew();

}
