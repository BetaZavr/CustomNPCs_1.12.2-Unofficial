package noppes.npcs.client.gui.custom.components;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.EventHooks;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.containers.ContainerCustomGui;

import java.util.Objects;

public class CustomGuiSlot extends Slot {

	protected EntityPlayer player;
	protected IItemSlot slot;

	public CustomGuiSlot(IInventory inventoryIn, int index, IItemSlot slotIn, EntityPlayer playerIn, int cx, int cy) {
		super(inventoryIn, index, slotIn.getPosX() + cx, slotIn.getPosY() + cy);
		player = playerIn;
		slot = slotIn;
	}

	@Override
	public void onSlotChanged() {
		if (!player.world.isRemote) {
			slot.setStack(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(getStack()));
			if (player.openContainer instanceof ContainerCustomGui) {
				IItemStack heldItem = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(player.inventory.getItemStack());
				EventHooks.onCustomGuiSlot((PlayerWrapper<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player),
						((ContainerCustomGui) player.openContainer).customGui, getSlotIndex(),
						slot.getStack(), heldItem);
			}
		}
		super.onSlotChanged();
	}

}
