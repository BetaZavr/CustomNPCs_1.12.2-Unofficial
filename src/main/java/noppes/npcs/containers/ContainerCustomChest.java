package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.PlayerData;

public class ContainerCustomChest extends ContainerNpcInterface {
	
	private IContainer container;
	private InventoryBasic craftingMatrix;
	public int rows;

	public ContainerCustomChest(EntityPlayer player, int rows) {
		super(player);
		this.rows = rows;
		if (!player.world.isRemote) {
			this.container = NpcAPI.Instance().getIContainer(this);
		}
		this.craftingMatrix = new InventoryBasic("crafting", false, rows * 9);
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, j1, j1 * 18 + 8, 89 + rows * 18));
		}
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(new Slot((IInventory) player.inventory, l1 + i1 * 9 + 9, l1 * 18 + 8,
						31 + rows * 18 + i1 * 18));
			}
		}
		for (int k = 0; k < rows; ++k) {
			for (int m = 0; m < 9; ++m) {
				this.addSlotToContainer(new Slot((IInventory) this.craftingMatrix, m + k * 9, 8 + m * 18, 18 + k * 18));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	public boolean canMergeSlot(ItemStack stack, Slot slotId) {
		return slotId.inventory == this.player.inventory;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.world.isRemote) {
			PlayerData data = PlayerData.get(player);
			CustomContainerEvent.CloseEvent event = new CustomContainerEvent.CloseEvent(data.scriptData.getPlayer(),
					this.container);
			EventHooks.onCustomChestClosed(event);
		}
	}

	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
		if (clickType == ClickType.QUICK_MOVE) {
			return ItemStack.EMPTY;
		}
		if (slotId < 36) {
			return super.slotClick(slotId, dragType, clickType, player);
		}
		if (clickType != ClickType.PICKUP || dragType != 0 || !(player instanceof EntityPlayerMP)
				|| this.container == null) {
			return ItemStack.EMPTY;
		}
		Slot slot = this.inventorySlots.get(slotId);
		if (slot == null) {
			return ItemStack.EMPTY;
		}
		PlayerData data = PlayerData.get(player);
		IItemStack item = NpcAPI.Instance().getIItemStack(slot.getStack());
		IItemStack heldItem = NpcAPI.Instance().getIItemStack(player.inventory.getItemStack());
		CustomContainerEvent.SlotClickedEvent event = new CustomContainerEvent.SlotClickedEvent(
				data.scriptData.getPlayer(), this.container, slotId, item, heldItem);
		EventHooks.onCustomChestClicked(event);
		player.inventory.setItemStack((event.heldItem == null) ? ItemStack.EMPTY : event.heldItem.getMCItemStack());
		((EntityPlayerMP) player).updateHeldItem();
		this.putStackInSlot(slotId, (event.slotItem == null) ? ItemStack.EMPTY : event.slotItem.getMCItemStack());
		this.detectAndSendChanges();
		return ItemStack.EMPTY;
	}
}
