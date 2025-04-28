package noppes.npcs;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class NpcMiscInventory implements IInventory {

	public NonNullList<ItemStack> items;
	public int stackLimit;

	public NpcMiscInventory(int size) {
		this.stackLimit = 64;
		this.items = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	public void addItemStack(ItemStack item) {
        ItemStack mergable;
		while (!(mergable = this.getMergableItem(item)).isEmpty() && mergable.getCount() > 0) {
			int size = mergable.getMaxStackSize() - mergable.getCount();
			if (size > item.getCount()) {
				mergable.setCount(mergable.getMaxStackSize());
				item.setCount(item.getCount() - size);
            } else {
				mergable.setCount(mergable.getCount() + item.getCount());
				item.setCount(0);
			}
		}
		if (item.getCount() <= 0) {
			return;
		}
		int slot = this.firstFreeSlot();
		if (slot >= 0) {
			this.items.set(slot, item.copy());
			item.setCount(0);
		}
	}

	public void clear() {
		this.items.clear();
	}

	public void closeInventory(@Nonnull EntityPlayer player) {
	}

	public @Nonnull ItemStack decrStackSize(int index, int count) {
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

	public NpcMiscInventory fill(NpcMiscInventory inv) {
		this.items.clear();
		for (int i = 0; i < this.getSizeInventory() && i < inv.getSizeInventory(); i++) {
			this.items.set(i, inv.items.get(i));
		}
		return this;
	}

	public int firstFreeSlot() {
		for (int i = 0; i < this.getSizeInventory(); ++i) {
			if ((this.items.get(i)).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	public int getCountEmpty() {
		int c = 0;
		for (int s = 0; s < this.getSizeInventory(); ++s) {
			if (this.items.get(s).isEmpty()) {
				c++;
			}
		}
		return c;
	}

	public @Nonnull ITextComponent getDisplayName() {
		return new TextComponentString("Custom Inventory");
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

	public @Nonnull String getName() {
		return "Npc Misc Inventory";
	}

	public int getSizeInventory() {
		return this.items.size();
	}

	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= this.items.size()) { return ItemStack.EMPTY; }
		return this.items.get(index);
	}

	public NBTTagCompound getToNBT() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("NpcMiscInv", NBTTags.nbtItemStackList(this.items));
		nbttagcompound.setInteger("NpcMiscInvSize", this.items.size());
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

	public boolean isFull() {
		for (int slot = 0; slot < this.getSizeInventory(); ++slot) {
			if (this.getStackInSlot(slot).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
		return true;
	}

	public boolean isUsableByPlayer(@Nonnull EntityPlayer var1) {
		return true;
	}

	public void markDirty() {
	}

	public void openInventory(@Nonnull EntityPlayer player) {
	}

	public @Nonnull ItemStack removeStackFromSlot(int slotId) {
		return this.items.set(slotId, ItemStack.EMPTY);
	}

	public void setField(int id, int value) {
	}

	public void setFromNBT(NBTTagCompound nbttagcompound) {
		NBTTags.getItemStackList(nbttagcompound.getTagList("NpcMiscInv", 10), this.items);
	}

	public void setInventorySlotContents(int slotId, @Nonnull ItemStack stack) {
		if (slotId >= getSizeInventory()) { return; }
		items.set(slotId, stack);
	}

	public void setSize(int size) {
		if (this.items.size() == size) {
			return;
		}
		NonNullList<ItemStack> newItems = NonNullList.withSize(size, ItemStack.EMPTY);
		for (int slot = 0; slot < this.items.size() && slot < size; ++slot) {
			if (this.items.get(slot).isEmpty()) {
				continue;
			}
			newItems.add(slot, this.items.get(slot));
		}
		this.items = newItems;
	}

}
