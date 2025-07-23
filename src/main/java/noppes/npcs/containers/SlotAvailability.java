package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotAvailability extends Slot {

    private int slotIndex;
    public IInventory inventory;

    public SlotAvailability(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        inventory = inventoryIn;
        slotIndex = index;
    }

    @Override
    public @Nonnull ItemStack getStack() { return inventory.getStackInSlot(slotIndex); }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        inventory.setInventorySlotContents(slotIndex, stack);
        onSlotChanged();
    }

    @Override
    public void onSlotChanged() { inventory.markDirty(); }

    @Override
    public int getSlotStackLimit() { return inventory.getInventoryStackLimit(); }

    @Override
    public @Nonnull ItemStack decrStackSize(int amount) { return inventory.decrStackSize(slotIndex, amount); }

    @Override
    public boolean isHere(@Nonnull IInventory inv, int slotIn) { return inv == inventory && slotIn == slotIndex; }

    @Override
    public int getSlotIndex()
    {
        return slotIndex;
    }

    @Override
    public boolean isSameInventory(@Nonnull Slot other) { return inventory == other.inventory; }

    public void setSlotIndex(int newSlotID, boolean isCheck) {
        if (newSlotID < 0) { newSlotID = 0; }
        if (isCheck && newSlotID >= inventory.getSizeInventory()) { return; }
        slotIndex = newSlotID;
    }

    public void setInventory(IInventory newInventory) {
        if (newInventory == null) { return; }
        inventory = newInventory;
        if (slotIndex >= inventory.getSizeInventory()) { slotIndex = inventory.getSizeInventory() - 1; }
    }

}
