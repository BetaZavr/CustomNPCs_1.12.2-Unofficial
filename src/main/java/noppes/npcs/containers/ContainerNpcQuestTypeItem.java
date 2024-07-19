package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.data.Quest;

import javax.annotation.Nonnull;

public class ContainerNpcQuestTypeItem extends Container {

	public int slotID;

	public ContainerNpcQuestTypeItem(EntityPlayer player, int slotID) { // Change
		Quest quest = NoppesUtilServer.getEditingQuest(player);
		this.slotID = slotID;
		this.addSlotToContainer(new Slot(quest.questInterface.items, 0, 8, 92)); // New
		this.putStackInSlot(0, quest.questInterface.items.getStackInSlot(slotID));
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 8 + l1 * 18, 113 + i1 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 171));
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}

}
