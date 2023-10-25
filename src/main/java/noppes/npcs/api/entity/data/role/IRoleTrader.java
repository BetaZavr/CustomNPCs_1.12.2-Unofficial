package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.api.item.IItemStack;

public interface IRoleTrader
extends INPCRole {
	
	IMarcet getStore();
	
	int getStoreId();

	@Deprecated
	IItemStack getCurrency1(int slot);
	
	@Deprecated
	IItemStack getCurrency2(int slot);

	@Deprecated
	void setMarket(String name);

	@Deprecated
	void set(int slot, IItemStack currency, IItemStack currency2, IItemStack sold);

	@Deprecated
	void remove(int slot);

	@Deprecated
	IItemStack getSold(int slot);

	@Deprecated
	String getMarket();
	
}
