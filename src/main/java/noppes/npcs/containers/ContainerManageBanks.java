package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.data.Bank;

import javax.annotation.Nonnull;

public class ContainerManageBanks extends Container {

	public static Object bank = null;

    public ContainerManageBanks(EntityPlayer player) {
        IInventory inv = new NpcMiscInventory(2);
		this.addSlotToContainer(new Slot(inv, 0, -5000, -5000));
		this.addSlotToContainer(new Slot(inv, 1, -5000, -5000));
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, j * 18 + 8, 113 + i * 18));
			}
		}
		for (int j = 0; j < 9; ++j) {
			this.addSlotToContainer(new Slot(player.inventory, j, j * 18 + 8, 171));
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	// Server
	public void setBank(Bank bank, int ceil) {
		if (!bank.ceilSettings.containsKey(ceil)) {
			return;
		}
		this.getSlot(0).putStack(bank.ceilSettings.get(ceil).openStack);
		this.getSlot(1).putStack(bank.ceilSettings.get(ceil).upgradeStack);
		this.detectAndSendChanges();
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}

}
