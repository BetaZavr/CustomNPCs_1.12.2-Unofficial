package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.NoppesUtilServer;

import javax.annotation.Nonnull;

public class InventoryNPC implements IInventory {

	private final Container con;
	public NonNullList<ItemStack> inventoryContents;
	private final String inventoryTitle;
	private final int slotsCount;

	public InventoryNPC(String s, int i, Container con) {
		this.con = con;
		this.inventoryTitle = s;
		this.slotsCount = i;
		this.inventoryContents = NonNullList.withSize(i, ItemStack.EMPTY);
	}

	public void clear() {
	}

	public void closeInventory(@Nonnull EntityPlayer player) {
	}

	public @Nonnull ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.inventoryContents, index, count);
	}

	public @Nonnull ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	public int getField(int id) {
		return 0;
	}

	public int getFieldCount() {
		return 0;
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public @Nonnull String getName() {
		return this.inventoryTitle;
	}

	public int getSizeInventory() {
		return this.slotsCount;
	}

	public @Nonnull ItemStack getStackInSlot(int i) {
		return this.inventoryContents.get(i);
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

	public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
		return true;
	}

	public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
		return false;
	}

	public void markDirty() {
		this.con.onCraftMatrixChanged(this);
	}

	public void openInventory(@Nonnull EntityPlayer player) {
	}

	public @Nonnull ItemStack removeStackFromSlot(int i) {
		return ItemStackHelper.getAndRemove(this.inventoryContents, i);
	}

	public void setField(int id, int value) {
	}

	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		this.inventoryContents.set(index, stack);
		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
	}
}
