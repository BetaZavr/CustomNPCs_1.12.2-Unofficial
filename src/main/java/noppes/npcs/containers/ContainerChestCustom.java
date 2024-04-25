package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.blocks.tiles.CustomTileEntityChest;

public class ContainerChestCustom extends Container {

	public BlockPos pos;
	public int height;
	public EntityPlayer player;
	public CustomTileEntityChest customChest, trueChest;

	public ContainerChestCustom(InventoryPlayer playerInventory, CustomTileEntityChest customChest,
			EntityPlayer player) {
		this.pos = customChest.getPos();
		this.player = player;
		this.customChest = customChest;
		this.trueChest = customChest;
		this.trueChest.openInventory(player);
		if (player.world.isRemote) {
			this.customChest = customChest.copy();
		}
		int h = ((int) Math.ceil((double) this.customChest.getSizeInventory() / 9.0d) - 4) * 18;
		int w = 0;
		if (this.customChest.getSizeInventory() > 45) {
			h = 18;
		}
		h -= 6;
		// Inventory
		if (this.customChest.getSizeInventory() > 45) { // Creative
			w = 8;
			this.height = 5 * 18;
			for (int i = 0; i < this.customChest.getSizeInventory(); i++) {
				this.addSlotToContainer(new Slot(this.customChest, i, -5000, -5000));
			}
		} else { // 9x(2 / 5)
			this.height = (int) Math.ceil((double) this.customChest.getSizeInventory() / 9.0d) * 18;
			int u = 0, e = this.customChest.getSizeInventory();
			if (this.customChest.getSizeInventory() % 9 != 0) {
				e -= this.customChest.getSizeInventory() % 9;
			}
			for (int i = 0; i < this.customChest.getSizeInventory(); i++) {
				if (i >= e) {
					u = (int) (((9.0d - ((double) this.customChest.getSizeInventory() % 9.0d)) / 2.0d) * 18.0d);
				}
				this.addSlotToContainer(new Slot(this.customChest, i, 8 + u + (i % 9) * 18,
						18 + (int) Math.floor((double) i / 9.0d) * 18));
			}
		}
		// Player Inventory
		for (int r = 0; r < 3; ++r) {
			for (int p = 0; p < 9; ++p) {
				this.addSlotToContainer(new Slot(playerInventory, p + r * 9 + 9, 8 + w + p * 18, 103 + r * 18 + h));
			}
		}
		for (int p = 0; p < 9; ++p) {
			this.addSlotToContainer(new Slot(playerInventory, p, 8 + w + p * 18, 161 + h));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	public IInventory getLowerChestInventory() {
		return this.trueChest;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		this.trueChest.closeInventory(playerIn);
	}

	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index < this.customChest.inventory.size()) {
				if (!this.mergeItemStack(itemstack1, this.customChest.inventory.size(), this.inventorySlots.size(),
						true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, this.customChest.inventory.size(), false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

}
