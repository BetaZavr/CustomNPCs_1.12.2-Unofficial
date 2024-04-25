package noppes.npcs.api.wrapper;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;

public class ContainerWrapper implements IContainer {

	private Container container;
	private IInventory inventory;

	public ContainerWrapper(Container container) {
		this.container = container;
	}

	public ContainerWrapper(IInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int count(IItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
		int count = 0;
		for (int i = 0; i < this.getSize(); ++i) {
			IItemStack toCompare = this.getSlot(i);
			if (NoppesUtilPlayer.compareItems(item.getMCItemStack(), toCompare.getMCItemStack(), ignoreDamage,
					ignoreNBT)) {
				count += toCompare.getStackSize();
			}
		}
		return count;
	}

	@Override
	public IItemStack[] getItems() {
		IItemStack[] items = new IItemStack[this.getSize()];
		for (int i = 0; i < this.getSize(); ++i) {
			items[i] = this.getSlot(i);
		}
		return items;
	}

	@Override
	public Container getMCContainer() {
		return this.container;
	}

	@Override
	public IInventory getMCInventory() {
		return this.inventory;
	}

	@Override
	public int getSize() {
		if (this.inventory != null) {
			return this.inventory.getSizeInventory();
		}
		return this.container.inventorySlots.size();
	}

	@Override
	public IItemStack getSlot(int slot) {
		if (slot < 0 || slot >= this.getSize()) {
			throw new CustomNPCsException("Slot is out of range " + slot, new Object[0]);
		}
		if (this.inventory != null) {
			return NpcAPI.Instance().getIItemStack(this.inventory.getStackInSlot(slot));
		}
		return NpcAPI.Instance().getIItemStack(this.container.getSlot(slot).getStack());
	}

	@Override
	public boolean isEmpty() {
		return this.inventory == null || this.inventory.isEmpty();
	}

	@Override
	public void setSlot(int slot, IItemStack item) {
		if (slot < 0 || slot >= this.getSize()) {
			throw new CustomNPCsException("Slot is out of range " + slot, new Object[0]);
		}
		ItemStack itemstack = (item == null) ? ItemStack.EMPTY : item.getMCItemStack();
		if (this.inventory != null) {
			this.inventory.setInventorySlotContents(slot, itemstack);
		} else {
			this.container.putStackInSlot(slot, itemstack);
			this.container.detectAndSendChanges();
		}
	}
}
