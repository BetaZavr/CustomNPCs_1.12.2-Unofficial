package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;

public class ContainerNPCTransportSetup extends Container {
	
	public TransportLocation location;
	public int catId;

	public ContainerNPCTransportSetup(EntityPlayer player, TransportLocation location, int catId) {
		this.location = location;
		this.catId = catId;
		for (int v = 0; v < 3; ++v) {
			for (int u = 0; u < 3; ++u) {
				this.addSlotToContainer(new Slot(location.inventory, u + v * 3, (location.id<0 ? -5000 : 0) + 215 + u * 18, (location.id<0 ? -5000 : 0) + 20 + v * 18));
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

	public NBTTagCompound saveTransport(TransportCategory category) {
		NBTTagCompound compound = new NBTTagCompound();
		if (category==null) { return compound; }
		for (int i = 0; i < 9; i++) {
			this.location.inventory.setInventorySlotContents(i, this.getSlot(i).getStack());
		}
		category.locations.put(this.location.id, this.location);
		category.writeNBT(compound);
		return compound;
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}

	public void setDeal(Deal deal) {
		/*this.deal = deal;
		this.getSlot(0).putStack(deal!=null ? deal.inventorySold.getStackInSlot(0) : ItemStack.EMPTY);
		for (int v = 0; v < 3; ++v) {
			for (int u = 0; u < 3; ++u) {
				int pos = u + v * 3;
				this.getSlot(pos+1).putStack(deal!=null ? deal.inventoryCurrency.getStackInSlot(pos) : ItemStack.EMPTY);
			}
		}*/
	}

}
