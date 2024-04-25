package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilServer;

public class SlotNpcBankCurrency extends Slot {

	public ItemStack stack;

	public SlotNpcBankCurrency(ContainerNPCBank container, IInventory iinventory, int i, int j, int k) {
		super(iinventory, i, j, k);
		this.stack = ItemStack.EMPTY;
	}

	public int getSlotStackLimit() {
		return 64;
	}

	public boolean isItemValid(ItemStack itemstack) {
		return !NoppesUtilServer.IsItemStackNull(itemstack) && (this.stack.getItem() == itemstack.getItem()
				&& (!this.stack.getHasSubtypes() || this.stack.getItemDamage() == itemstack.getItemDamage()));
	}

}
