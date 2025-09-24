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
		stackLimit = 64;
		items = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	public void addItemStack(ItemStack item) {
        ItemStack mergable;
		while (!(mergable = getMergableItem(item)).isEmpty() && mergable.getCount() > 0) {
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
		int slot = firstFreeSlot();
		if (slot >= 0) {
			items.set(slot, item.copy());
			item.setCount(0);
		}
	}

	public void clear() {
		items.clear();
	}

	public void closeInventory(@Nonnull EntityPlayer player) { }

	public @Nonnull ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(items, index, count); }

	public boolean decrStackSize(ItemStack eating, int decrease) {
		for (int slot = 0; slot < items.size(); ++slot) {
			ItemStack item = items.get(slot);
			if (!item.isEmpty() && eating == item && item.getCount() >= decrease) {
				item.splitStack(decrease);
				if (item.getCount() <= 0) { items.set(slot, ItemStack.EMPTY); }
				return true;
			}
		}
		return false;
	}

	public NpcMiscInventory fill(NpcMiscInventory inv) {
		items.clear();
		for (int i = 0; i < getSizeInventory() && i < inv.getSizeInventory(); i++) { items.set(i, inv.items.get(i)); }
		return this;
	}

	public int firstFreeSlot() {
		for (int i = 0; i < getSizeInventory(); ++i) {
			if ((items.get(i)).isEmpty()) { return i; }
		}
		return -1;
	}

	public int getCountEmpty() {
		int c = 0;
		for (int s = 0; s < getSizeInventory(); ++s) {
			if (items.get(s).isEmpty()) { c++; }
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
		return stackLimit;
	}

	public ItemStack getMergableItem(ItemStack item) {
		for (ItemStack is : items) {
			if (NoppesUtilPlayer.compareItems(item, is, false, false) && is.getCount() < is.getMaxStackSize()) { return is; }
		}
		return ItemStack.EMPTY;
	}

	public @Nonnull String getName() {
		return "Npc Misc Inventory";
	}

	public int getSizeInventory() { return items.size(); }

	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= items.size()) { return ItemStack.EMPTY; }
		return items.get(index);
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("NpcMiscInv", NBTTags.nbtItemStackList(items));
		compound.setInteger("NpcMiscInvSize", items.size());
		return compound;
	}

	public boolean hasCustomName() {
		return true;
	}

	public boolean isEmpty() {
		for (int slot = 0; slot < getSizeInventory(); ++slot) {
			ItemStack item = getStackInSlot(slot);
			if (!NoppesUtilServer.IsItemStackNull(item) && !item.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isFull() {
		for (int slot = 0; slot < getSizeInventory(); ++slot) {
			if (getStackInSlot(slot).isEmpty()) {
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

	public void markDirty() { }

	public void openInventory(@Nonnull EntityPlayer player) { }

	public @Nonnull ItemStack removeStackFromSlot(int slotId) {
		return items.set(slotId, ItemStack.EMPTY);
	}

	public void setField(int id, int value) { }

	public void load(NBTTagCompound compound) { NBTTags.getItemStackList(compound.getTagList("NpcMiscInv", 10), items); }

	public void setSize(int size) {
		if (items.size() == size) { return; }
		NonNullList<ItemStack> newItems = NonNullList.withSize(size, ItemStack.EMPTY);
		for (int slot = 0; slot < items.size() && slot < size; ++slot) {
			if (items.get(slot).isEmpty()) { continue; }
			newItems.set(slot, items.get(slot));
		}
		items = newItems;
	}

	public boolean remove(int slotID) {
		if (slotID < 0 || slotID >= items.size()) { return false; }
		NonNullList<ItemStack> newItems = NonNullList.withSize(items.size() - 1, ItemStack.EMPTY);
		for (int slot = 0; slot < items.size(); ++slot) {
			if (items.get(slot).isEmpty() || slotID == slot) { continue; }
			newItems.set(slot - (slot > slotID ? 1 : 0), items.get(slot));
		}
		items = newItems;
		return true;
	}

	// New from Unofficial (BetaZavr)
	public void setInventorySlotContents(int slotId, @Nonnull ItemStack stack) {
		if (slotId >= getSizeInventory()) { return; }
		items.set(slotId, stack);
	}

}
