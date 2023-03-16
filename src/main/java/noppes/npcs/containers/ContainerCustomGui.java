package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.gui.custom.components.CustomGuiSlot;
import noppes.npcs.util.CustomNPCsScheduler;

public class ContainerCustomGui extends Container {
	// New
	public IContainer container;
	public CustomGuiWrapper customGui;
	public int cx;
	public int cy;
	public IInventory guiInventory;
	int slotCount;

	public ContainerCustomGui(EntityPlayer player, IInventory inventory) { // Changed
		this.slotCount = 0;
		this.guiInventory = inventory;
		// New
		this.cx = 0;
		this.cy = 0;
		this.container = NpcAPI.Instance().getIContainer(this);
	}

	void addPlayerInventory(EntityPlayer player, int x, int y) {
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlotToContainer(
						new Slot((IInventory) player.inventory, col + row * 9 + 9, x + col * 18, y + row * 18));
			}
		}
		for (int row = 0; row < 9; ++row) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, row, x + row * 18, y + 58));
		}
	}

	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	// New
	public Slot getSlot(int slotId) {
		if (slotId >= this.inventorySlots.size()) {
			return new Slot(this.guiInventory, 0, 0, 0);
		}
		return this.inventorySlots.get(slotId);
	}

	public void setGui(CustomGuiWrapper gui, EntityPlayer player) {
		this.customGui = gui;
		// coorector position
		if (this.customGui != null) {
			this.cx = -40 + (256 - this.customGui.getWidth()) / 2;
			this.cy = -45 + (256 - this.customGui.getHeight()) / 2;
		}
		this.slotCount = 0; // New
		for (IItemSlot slot : this.customGui.getSlots()) {
			int index = this.slotCount++;
			this.addSlotToContainer((Slot) new CustomGuiSlot(this.guiInventory, index, slot, player, this.cx, this.cy)); // Changed
			this.guiInventory.setInventorySlotContents(index, slot.getStack().getMCItemStack());
		}
		if (this.customGui.getShowPlayerInv()) {
			this.addPlayerInventory(player, this.cx + this.customGui.getPlayerInvX(),
					this.cy + this.customGui.getPlayerInvY());
		}
	}

	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (slotId < 0) {
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		if (!player.world.isRemote && slotId >= 0) {
			IItemStack heldItem = NpcAPI.Instance().getIItemStack(player.inventory.getItemStack());
			if (!EventHooks.onCustomGuiSlotClicked((PlayerWrapper<?>) NpcAPI.Instance().getIEntity(player),
					((ContainerCustomGui) player.openContainer).customGui, slotId, dragType, clickTypeIn.toString(),
					heldItem)) {
				ItemStack item = super.slotClick(slotId, dragType, clickTypeIn, player);
				EntityPlayerMP p = (EntityPlayerMP) player;
				CustomNPCsScheduler.runTack(() -> p.sendContainerToPlayer((Container) this), 10);
				return item;
			}
		}
		return ItemStack.EMPTY;
	}

	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack2 = slot.getStack();
			itemstack = itemstack2.copy();
			if (index < this.guiInventory.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack2, this.guiInventory.getSizeInventory(), this.inventorySlots.size(),
						true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack2, 0, this.guiInventory.getSizeInventory(), false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack2.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

}
