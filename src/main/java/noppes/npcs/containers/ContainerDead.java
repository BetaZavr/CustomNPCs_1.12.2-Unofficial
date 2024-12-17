package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import javax.annotation.Nonnull;

public class ContainerDead extends Container {

    public final int size;
    public final int pos;
    public final String playerParent;

    public ContainerDead(EntityPlayer player, IInventory inv, String name, int p) {
        size = (int) Math.ceil((double) inv.getSizeInventory() / 9.0d);
        pos = p;
        playerParent = name;
        int h = 54 - size * 18;
        int w = 8;
        // Dead Inventory
        for (int id = 0; id < inv.getSizeInventory(); ++id) {
            int x = id % 9;
            int y = (int) Math.floor((double) id / 9.0d);
            addSlotToContainer(new Slot(inv, id, w + x * 18, h + y * 18));
        }
        h += 6 + size * 18;
        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, w + j * 18, h + i * 18));
            }
        }
        h += 58;
        for (int j = 0; j < 9; ++j) {
            addSlotToContainer(new Slot(player.inventory, j, w + j * 18, h));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) { return true; }

}
