package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.item.IItemStack;

public interface IRoleTrader extends INPCRole {
	
	IItemStack getCurrency(int position, int slot);

	String getName();

	IItemStack getProduct(int position);

	void remove(int position);

	void set(int position, IItemStack product, IItemStack[] currencys);

	void setName(String name);
}
