package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.item.IItemStack;

public interface IRoleTrader extends INPCRole {
	/*
	 * IItemStack getSold(int p0);
	 * 
	 * IItemStack getCurrency1(int p0);
	 * 
	 * IItemStack getCurrency2(int p0);
	 * 
	 * void set(int p0, IItemStack p1, IItemStack p2, IItemStack p3);
	 * 
	 * void remove(int p0);
	 * 
	 * void setMarket(String p0);
	 * 
	 * String getMarket();
	 */

	IItemStack getCurrency(int position, int slot);

	String getName();

	// New
	IItemStack getProduct(int position);

	void remove(int position);

	void set(int position, IItemStack product, IItemStack[] currencys);

	void setName(String name);
}
