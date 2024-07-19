package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

import javax.annotation.Nonnull;

public class ContainerNPCFollowerSetup extends Container {

    public ContainerNPCFollowerSetup(EntityNPCInterface npc, EntityPlayer player) {
        RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
		for (int i1 = 0; i1 < 3; ++i1) {
			this.addSlotToContainer(new Slot(role.rentalItems, i1, 44, 39 + i1 * 25));
		}
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 8 + l1 * 18, 113 + i1 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 171));
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(i);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack2 = slot.getStack();
			itemstack = itemstack2.copy();
			if (i < 3) {
				if (!this.mergeItemStack(itemstack2, 3, 38, true)) {
					return ItemStack.EMPTY;
				}
			} else if (i < 30) {
				if (!this.mergeItemStack(itemstack2, 30, 38, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i < 38) {
				if (!this.mergeItemStack(itemstack2, 3, 29, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack2, 3, 38, false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack2.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
			if (itemstack2.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, itemstack2);
		}
		return itemstack;
	}
}
