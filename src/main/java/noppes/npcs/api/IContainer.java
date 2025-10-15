package noppes.npcs.api;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IContainer {

	int count(@ParamName("item") IItemStack item, @ParamName("ignoreDamage") boolean ignoreDamage, @ParamName("ignoreNBT") boolean ignoreNBT);

	IItemStack[] getItems();

	Container getMCContainer();

	IInventory getMCInventory();

	int getSize();

	IItemStack getSlot(@ParamName("slotId") int slotId);

	boolean isEmpty();

	void setSlot(@ParamName("slotId") int slotId, @ParamName("item") IItemStack item);

}
