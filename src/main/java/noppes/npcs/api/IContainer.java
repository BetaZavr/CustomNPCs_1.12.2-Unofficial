package noppes.npcs.api;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import noppes.npcs.api.item.IItemStack;

public interface IContainer {
	
	int count(IItemStack item, boolean ignoreDamage, boolean ignoreNBT);

	IItemStack[] getItems();

	Container getMCContainer();

	IInventory getMCInventory();

	int getSize();

	IItemStack getSlot(int slot);

	void setSlot(int slot, IItemStack item);
	
}
