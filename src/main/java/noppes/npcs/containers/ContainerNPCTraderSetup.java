package noppes.npcs.containers;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;

public class ContainerNPCTraderSetup
extends Container {
	
	public Deal deal;
	public Marcet marcet;

	public ContainerNPCTraderSetup(Marcet marcet, Deal deal, EntityPlayer player) {
		this.marcet = marcet;
		this.deal = (deal==null || (player instanceof EntityPlayerMP) ? deal : deal.copy());
		if (deal!=null) {
			this.addSlotToContainer(new Slot(this.deal.getMCInventoryProduct(), 0, 120, 29)); // 215
			for (int v = 0; v < 3; ++v) {
				for (int u = 0; u < 3; ++u) {
					this.addSlotToContainer(new Slot(this.deal.getMCInventoryCurrency(), u + v * 3, 102 + u * 18, 66 + v * 18));
				}
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
		if (this.deal==null) { return; }
		ItemStack product = this.getSlot(0).getStack();
		List<ItemStack> list = Lists.<ItemStack>newArrayList();
		for (int i = 1; i < 10; i++) {
			if (this.getSlot(i).getStack().isEmpty()) { continue; }
			list.add(this.getSlot(i).getStack());
		}
		this.deal.set(product, list.toArray(new ItemStack[list.size()]));
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}

}
