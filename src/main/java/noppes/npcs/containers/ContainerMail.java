package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMailData;

public class ContainerMail extends ContainerNpcInterface {
	public static PlayerMail staticmail = new PlayerMail();
	private boolean canEdit;
	public PlayerMail mail;

	public ContainerMail(EntityPlayer player, boolean canEdit, boolean canSend) {
		super(player);
		this.mail = new PlayerMail();
		this.mail = ContainerMail.staticmail;
		ContainerMail.staticmail = new PlayerMail();
		this.canEdit = canEdit;
		player.inventory.openInventory(player);
		for (int k = 0; k < 4; ++k) {
			this.addSlotToContainer((Slot) new SlotValid((IInventory) this.mail, k, 179 + k * 24, 138, canEdit));
		}
		for (int j = 0; j < 3; ++j) {
			for (int k = 0; k < 9; ++k) {
				this.addSlotToContainer(
						new Slot((IInventory) player.inventory, k + j * 9 + 9, 28 + k * 18, 175 + j * 18));
			}
		}
		for (int j = 0; j < 9; ++j) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, j, 28 + j * 18, 230));
		}
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!this.canEdit && !player.world.isRemote) {
			PlayerMailData data = PlayerData.get(player).mailData;
			for (PlayerMail mail : data.playermail) {
				if (mail.time == this.mail.time && mail.sender.equals(this.mail.sender)) {
					mail.readNBT(this.mail.writeNBT());
					break;
				}
			}
		}
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(par2);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack2 = slot.getStack();
			itemstack = itemstack2.copy();
			if (par2 < 4) {
				if (!this.mergeItemStack(itemstack2, 4, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.canEdit || !this.mergeItemStack(itemstack2, 0, 4, false)) {
				return null;
			}
			if (itemstack2.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

}
