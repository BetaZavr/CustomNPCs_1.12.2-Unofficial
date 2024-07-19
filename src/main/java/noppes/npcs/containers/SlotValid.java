package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotValid extends Slot {

	private final boolean canPutIn;

	public SlotValid(IInventory par1iInventory, int par2, int par3, int par4, boolean bo) {
		super(par1iInventory, par2, par3, par4);
        this.canPutIn = bo;
	}

	public boolean isItemValid(@Nonnull ItemStack stack) {
		return this.canPutIn && this.inventory.isItemValidForSlot(0, stack);
	}
}
