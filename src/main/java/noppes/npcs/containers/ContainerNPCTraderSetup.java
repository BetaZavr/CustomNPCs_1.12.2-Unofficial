package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;

public class ContainerNPCTraderSetup extends Container {
	
	public Deal deal;
	public Marcet marcet;

	public ContainerNPCTraderSetup(Marcet marcet, int dealPos, EntityPlayer player) {
		this.marcet = marcet;
		this.deal = marcet.data.get(dealPos) != null ? marcet.data.get(dealPos).copy() : new Deal();

		this.addSlotToContainer(new Slot(this.deal.inventorySold, 0, 215, 15));
		for (int v = 0; v < 3; ++v) {
			for (int u = 0; u < 3; ++u) {
				this.addSlotToContainer(new Slot(this.deal.inventoryCurrency, u + v * 3, 215 + u * 18, 47 + v * 18));
			}
		}
		for (int i2 = 0; i2 < 3; ++i2) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(
						new Slot((IInventory) player.inventory, l1 + i2 * 9 + 9, 48 + l1 * 18, 137 + i2 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, j1, 48 + j1 * 18, 195));
		}
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void saveMarcet() {
		if (this.deal!=null) {
			this.deal.inventorySold.setInventorySlotContents(0, this.getSlot(0).getStack());
			for (int i = 1; i < 10; i++) {
				this.deal.inventoryCurrency.setInventorySlotContents(i - 1, this.getSlot(i).getStack());
			}
			this.marcet.data.put(this.deal.id, this.deal);
		}
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}

	public void setDeal(Deal deal) {
		this.deal = deal;
		this.getSlot(0).putStack(deal!=null ? deal.inventorySold.getStackInSlot(0) : ItemStack.EMPTY);
		for (int v = 0; v < 3; ++v) {
			for (int u = 0; u < 3; ++u) {
				int pos = u + v * 3;
				this.getSlot(pos+1).putStack(deal!=null ? deal.inventoryCurrency.getStackInSlot(pos) : ItemStack.EMPTY);
			}
		}
	}

}
