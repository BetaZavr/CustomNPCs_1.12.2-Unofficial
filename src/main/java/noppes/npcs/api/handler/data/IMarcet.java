package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

public interface IMarcet {

	IItemStack getCurrency(int dealID, int slot);

	String getName();

	IItemStack getProduct(int dealID);

	void remove(int dealID);

	void set(int dealID, IItemStack product, IItemStack[] currencys);

	void setName(String name);
	
}
