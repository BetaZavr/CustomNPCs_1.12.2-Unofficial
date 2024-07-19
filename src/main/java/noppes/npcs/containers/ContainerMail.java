package noppes.npcs.containers;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMailData;

import javax.annotation.Nonnull;

public class ContainerMail extends ContainerNpcInterface {

	public static PlayerMail staticmail = new PlayerMail();
	private final boolean canEdit;
    private final boolean canSend;
	public PlayerMail mail;

	public ContainerMail(EntityPlayer player, boolean canEdit, boolean canSend) {
		super(player);
		this.mail = ContainerMail.staticmail;
		ContainerMail.staticmail = new PlayerMail();
		this.canEdit = canEdit;
		this.canSend = canSend;
		player.inventory.openInventory(player);
		for (int k = 0; k < 4; ++k) {
			this.addSlotToContainer(new SlotValid(this.mail, k, 199 + (k % 2) * 18, 190 + (k / 2) * 18, canEdit));
		}
		for (int j = 0; j < 3; ++j) {
			for (int k = 0; k < 9; ++k) {
				this.addSlotToContainer(new Slot(player.inventory, k + j * 9 + 9, 7 + k * 18, 168 + j * 18));
			}
		}
		for (int j = 0; j < 9; ++j) {
			this.addSlotToContainer(new Slot(player.inventory, j, 7 + j * 18, 223));
		}
	}

	public void onContainerClosed(@Nonnull EntityPlayer player) {
		super.onContainerClosed(player);
		if (player.world.isRemote) {
			return;
		}
		if (!this.canEdit) {
			PlayerMailData data = PlayerData.get(player).mailData;
			for (PlayerMail mail : data.playermail) {
				if (mail.timeWhenReceived == this.mail.timeWhenReceived && mail.sender.equals(this.mail.sender)) {
					mail.readNBT(this.mail.writeNBT());
					break;
				}
			}
		} else {
			for (int i = 0; i < 4; i++) {
				Slot slot = this.getSlot(i);
				if (!slot.getHasStack()) {
					continue;
				}
				EntityItem entityitem = new EntityItem(player.world, player.posX, player.posY + 0.16f, player.posZ,
						slot.getStack());
				entityitem.setPickupDelay(1);
				entityitem.setOwner(player.getName());
				player.world.spawnEntity(entityitem);
			}
		}
	}

	@Override
	public @Nonnull ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull EntityPlayer player) {
		if (!this.canEdit && !this.canSend && slotId > -1 && slotId < 4) {
			Slot slot = this.inventorySlots.get(slotId);
			if (slot != null && slot.getHasStack()) {
				return super.slotClick(slotId, 0, ClickType.QUICK_MOVE, player); // take
			}
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int slotId) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotId);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();
			if (slotId < 4) {
				if (!this.mergeItemStack(slotStack, 4, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.canEdit || !this.mergeItemStack(slotStack, 0, 4, false)) {
				return ItemStack.EMPTY;
			}
			if (slotStack.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return stack;
	}

}
