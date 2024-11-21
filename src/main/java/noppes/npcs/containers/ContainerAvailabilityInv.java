package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CommonProxy;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.data.Availability;

import javax.annotation.Nonnull;

public class ContainerAvailabilityInv extends Container {

    public final Availability availability;
    public final NpcMiscInventory inv;
    public final SlotAvailability slot;

    public ContainerAvailabilityInv(EntityPlayer player) {
        Availability aParent = CommonProxy.availabilityStacks.get(player);
        availability = new Availability();
        NBTTagCompound compound = new NBTTagCompound();
        aParent.writeToNBT(compound);
        availability.readFromNBT(compound);

        inv = availability.stacks;
        slot = new SlotAvailability(inv, 0, 8, 89);
        addSlotToContainer(slot);

        for (int i1 = 0; i1 < 3; ++i1) {
            for (int l2 = 0; l2 < 9; ++l2) {
                addSlotToContainer(new Slot(player.inventory, l2 + i1 * 9 + 9, l2 * 18 + 8, 113 + i1 * 18));
            }
        }
        for (int j1 = 0; j1 < 9; ++j1) {
            addSlotToContainer(new Slot(player.inventory, j1, j1 * 18 + 8, 171));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
        return true;
    }

}
