package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.reflection.inv.SlotReflection;

public class SlotAvailability extends Slot {

    public SlotAvailability(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, 0, xPosition, yPosition);
    }

    public void setSlotIndex(int slotID) {
        SlotReflection.setSlotIndex(this, slotID);
    }

}
