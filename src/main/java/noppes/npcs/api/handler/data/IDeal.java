package noppes.npcs.api.handler.data;

import net.minecraft.inventory.IInventory;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.item.IItemStack;

public interface IDeal {

	int getId();
	
	String getName();

	void set(IItemStack product, IItemStack[] currencys);

	void updateNew();
	
	IContainer getCurrency();
	
	IItemStack getProduct();

	int getMoney();
	
	void setMoney(int money);
	
	boolean getIgnoreDamage();

	boolean getIgnoreNBT();
	
	void setIgnoreDamage(boolean bo);

	void setIgnoreNBT(boolean bo);
	
	IAvailability getAvailability();

	int getChance();

	void setChance(int chance);

	int getType();

	void setType(int type);

	int getMinCount();

	int getMaxCount();

	void setCount(int min, int max);

	int getAmount();

	void setAmount(int amount);

	void setProduct(IItemStack product);

	IInventory getMCInventoryProduct();

	IInventory getMCInventoryCurrency();
	
}
