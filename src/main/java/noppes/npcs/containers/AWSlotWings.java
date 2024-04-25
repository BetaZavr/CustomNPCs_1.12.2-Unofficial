package noppes.npcs.containers;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.ArmourersWorkshopClientApi;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class AWSlotWings extends Slot {

	public AWSlotWings(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	public int getSlotStackLimit() {
		return 1;
	}

	public boolean isItemValid(ItemStack itemstack) {
		if (ArmourersWorkshopClientApi.getSkinRenderHandler() == null) {
			return false;
		}
		ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(itemstack);
		if (skinDescriptor == null) {
			return false;
		}
		String type = skinDescriptor.getIdentifier().getSkinType().getName();
		return type.equalsIgnoreCase("wings");
	}

}
