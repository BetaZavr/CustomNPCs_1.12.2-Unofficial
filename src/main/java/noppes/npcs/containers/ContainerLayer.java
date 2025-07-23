package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityCustomNpc;

import javax.annotation.Nonnull;

public class ContainerLayer extends ContainerNpcInterface {

    public final SlotAvailability slot;

    public ContainerLayer(EntityPlayer player, EntityCustomNpc npc) {
        super(player);
        player.inventory.openInventory(player);
        slot = new SlotAvailability(npc.modelData.getLayerInventory(), 0, 0, 0);
        addSlotToContainer(slot);
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                addSlotToContainer(new Slot(player.inventory, k + j * 9 + 9, 0, 0));
            }
        }
        for (int j = 0; j < 9; ++j) {
            addSlotToContainer(new Slot(player.inventory, j, 0, 0));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) { return true; }

    @Override
    public @Nonnull ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull EntityPlayer player) {
        if (slotId >= 0) {
            Slot slot = inventorySlots.get(slotId);
            ItemStack stackInSlot = slot.getStack();
            if (clickTypeIn == ClickType.QUICK_MOVE && dragType == 0) { // shift + LMB
                if (slotId == 0) {
                    if (!mergeItemStack(stackInSlot, 1, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(stackInSlot, 0, 1, true)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if (slotId == 0) {
                slot.putStack(player.inventory.getItemStack());
                player.inventory.setItemStack(stackInSlot);
                return ItemStack.EMPTY;
            }
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) { return ItemStack.EMPTY; }


    @Override
    public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
        InventoryPlayer inventoryPlayer = playerIn.inventory;
        if (!inventoryPlayer.getItemStack().isEmpty()) { inventoryPlayer.setItemStack(ItemStack.EMPTY); }
    }

}
