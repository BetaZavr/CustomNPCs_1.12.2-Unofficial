package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

import javax.annotation.Nonnull;

public class ContainerNPCFollowerHire extends ContainerNpcInterface {

	public RoleFollower role;
	public int type;

	public ContainerNPCFollowerHire(EntityNPCInterface npc, EntityPlayer player, int type) {
		super(player);
		this.type = type;
		this.role = (RoleFollower) npc.advanced.roleInterface;
		int offSet = type == 0 ? 0 : 58;
		int size = this.role.inventory.getSizeInventory();
		if (size > 0) {
			int s = (size == 2 || size == 4) ? 2 : 3;
			boolean bo = false;
			for (int y = 0; y < s; ++y) {
				for (int x = 0; x < s; ++x) {
					bo = (x + y * s) >= size;
					if (bo) {
						break;
					}
					this.addSlotToContainer(new Slot(this.role.inventory, x + y * s, 174 + x * 18, 142 + y * 18));
				}
				if (bo) {
					break;
				}
			}
		}
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				this.addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 + offSet));
			}
		}
		for (int x = 0; x < 9; ++x) {
			this.addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, 142 + offSet));
		}
	}

	public void onContainerClosed(@Nonnull EntityPlayer entityplayer) {
		super.onContainerClosed(entityplayer);
	}

	@Override
	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index < this.role.inventory.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack1, this.role.inventory.getSizeInventory(), this.inventorySlots.size(),
						true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, this.role.inventory.getSizeInventory(), false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

}
