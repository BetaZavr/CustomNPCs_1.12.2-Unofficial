package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DropSet;

import javax.annotation.Nonnull;

public class ContainerNPCDropSetup extends Container {

	public DropSet inventoryDS;

	public ContainerNPCDropSetup(EntityNPCInterface npc, EntityPlayer player, int dropType, int groupId, int pos) {
		inventoryDS = null;
		if (dropType == 1) {
			DropsTemplate template = DropController.getInstance().templates.get(npc.inventory.saveDropsName);
			if (template != null && template.groups.containsKey(groupId)
					&& template.groups.get(groupId).containsKey(pos)) {
				inventoryDS = template.groups.get(groupId).get(pos);
			}
		} else {
			if (npc.inventory.drops.containsKey(pos)) { inventoryDS = npc.inventory.drops.get(pos); }
		}
		if (inventoryDS == null) { inventoryDS = new DropSet(npc.inventory, null); }
		addSlotToContainer(new Slot(inventoryDS, 0, 202, 135));
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l2 = 0; l2 < 9; ++l2) {
				addSlotToContainer(new Slot(player.inventory, l2 + i1 * 9 + 9, l2 * 18 + 8, 135 + i1 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			addSlotToContainer(new Slot(player.inventory, j1, j1 * 18 + 8, 193));
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}

}
