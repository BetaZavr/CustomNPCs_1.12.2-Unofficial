package noppes.npcs;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class NpcMiscInventory
implements IInventory {
	
	public NonNullList<ItemStack> items;
	private int size;
	public int stackLimit;

	public NpcMiscInventory(int size) {
		this.stackLimit = 64;
		this.size = size;
		this.items = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	public boolean addItemStack(ItemStack item) {
		boolean merged = false;
		ItemStack mergable;
		while (!(mergable = this.getMergableItem(item)).isEmpty() && mergable.getCount() > 0) {
			int size = mergable.getMaxStackSize() - mergable.getCount();
			if (size > item.getCount()) {
				mergable.setCount(mergable.getMaxStackSize());
				item.setCount(item.getCount() - size);
				merged = true;
			} else {
				mergable.setCount(mergable.getCount() + item.getCount());
				item.setCount(0);
			}
		}
		if (item.getCount() <= 0) {
			return true;
		}
		int slot = this.firstFreeSlot();
		if (slot >= 0) {
			this.items.set(slot, item.copy());
			item.setCount(0);
			return true;
		}
		return merged;
	}

	public void clear() {
	}

	public void closeInventory(EntityPlayer player) {
	}

	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.items, index, count);
	}

	public boolean decrStackSize(ItemStack eating, int decrease) {
		for (int slot = 0; slot < this.items.size(); ++slot) {
			ItemStack item = this.items.get(slot);
			if (!item.isEmpty() && eating == item && item.getCount() >= decrease) {
				item.splitStack(decrease);
				if (item.getCount() <= 0) {
					this.items.set(slot, ItemStack.EMPTY);
				}
				return true;
			}
		}
		return false;
	}

	public int firstFreeSlot() {
		for (int i = 0; i < this.getSizeInventory(); ++i) {
			if ((this.items.get(i)).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	public ITextComponent getDisplayName() {
		return null;
	}

	public int getField(int id) {
		return 0;
	}

	public int getFieldCount() {
		return 0;
	}

	public int getInventoryStackLimit() {
		return this.stackLimit;
	}

	public ItemStack getMergableItem(ItemStack item) {
		for (ItemStack is : this.items) {
			if (NoppesUtilPlayer.compareItems(item, is, false, false) && is.getCount() < is.getMaxStackSize()) {
				return is;
			}
		}
		return ItemStack.EMPTY;
	}

	public String getName() {
		return "Npc Misc Inventory";
	}

	public int getSizeInventory() {
		return this.size;
	}

	public ItemStack getStackInSlot(int index) {
		return this.items.get(index);
	}

	public NBTTagCompound getToNBT() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("NpcMiscInv", NBTTags.nbtItemStackList(this.items));
		return nbttagcompound;
	}

	public boolean hasCustomName() {
		return true;
	}

	public boolean isEmpty() {
		for (int slot = 0; slot < this.getSizeInventory(); ++slot) {
			ItemStack item = this.getStackInSlot(slot);
			if (!NoppesUtilServer.IsItemStackNull(item) && !item.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	public boolean isUsableByPlayer(EntityPlayer var1) {
		return true;
	}

	public void markDirty() {
	}

	public void openInventory(EntityPlayer player) {
	}

	public ItemStack removeStackFromSlot(int var1) {
		return this.items.set(var1, ItemStack.EMPTY);
	}

	public void setField(int id, int value) {
	}

	public void setFromNBT(NBTTagCompound nbttagcompound) {
		NBTTags.getItemStackList(nbttagcompound.getTagList("NpcMiscInv", 10), this.items);
	}

	public void setInventorySlotContents(int var1, ItemStack var2) {
		if (var1 >= this.getSizeInventory()) {
			return;
		}
		this.items.set(var1, var2);
	}

	public void setSize(int i) {
		this.size = i;
	}

	public boolean isFull() {
		for (int slot = 0; slot < this.getSizeInventory(); ++slot) {
			if (this.getStackInSlot(slot).isEmpty()) { return false; }
		}
		return true;
	}

}
