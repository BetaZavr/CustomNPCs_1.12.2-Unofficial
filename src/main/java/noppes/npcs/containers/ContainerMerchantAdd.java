package noppes.npcs.containers;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.NoppesUtilServer;

import javax.annotation.Nonnull;

public class ContainerMerchantAdd extends ContainerNpcInterface {

	private final InventoryBasic merchantInventory;
	private final IMerchant theMerchant;
	private final World world;

	public ContainerMerchantAdd(EntityPlayer player, IMerchant par2IMerchant, World par3World) {
		super(player);
		this.theMerchant = par2IMerchant;
		this.world = par3World;
		this.merchantInventory = new InventoryBasic("", false, 3);
		this.addSlotToContainer(new Slot(this.merchantInventory, 0, 36, 53));
		this.addSlotToContainer(new Slot(this.merchantInventory, 1, 62, 53));
		this.addSlotToContainer(new Slot(this.merchantInventory, 2, 120, 53));
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142));
		}
	}

	public void detectAndSendChanges() {
		super.detectAndSendChanges();
	}

	public void onContainerClosed(@Nonnull EntityPlayer player) {
		super.onContainerClosed(player);
		this.theMerchant.setCustomer(null);
		super.onContainerClosed(player);
		if (!this.world.isRemote) {
			ItemStack itemstack = this.merchantInventory.removeStackFromSlot(0);
			if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
				player.dropItem(itemstack, false);
			}
			itemstack = this.merchantInventory.removeStackFromSlot(1);
			if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
				player.dropItem(itemstack, false);
			}
		}
	}

	public void onCraftMatrixChanged(@Nonnull IInventory inventory) {
		super.onCraftMatrixChanged(inventory);
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int par2) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(par2);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack2 = slot.getStack();
			itemstack = itemstack2.copy();
			if (par2 != 0 && par2 != 1 && par2 != 2) {
				if (par2 < 30) {
					if (!this.mergeItemStack(itemstack2, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (par2 < 39 && !this.mergeItemStack(itemstack2, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack2, 3, 39, false)) {
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

	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2) {
	}
}
