package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityNPCInterface;

public class ContainerNPCInv extends Container {
	public ContainerNPCInv(EntityNPCInterface npc, EntityPlayer player) {
		this.addSlotToContainer(
				(Slot) new SlotNPCArmor((IInventory) npc.inventory, 0, 9, 22, EntityEquipmentSlot.HEAD));
		this.addSlotToContainer(
				(Slot) new SlotNPCArmor((IInventory) npc.inventory, 1, 9, 40, EntityEquipmentSlot.CHEST));
		this.addSlotToContainer(
				(Slot) new SlotNPCArmor((IInventory) npc.inventory, 2, 9, 58, EntityEquipmentSlot.LEGS));
		this.addSlotToContainer(
				(Slot) new SlotNPCArmor((IInventory) npc.inventory, 3, 9, 76, EntityEquipmentSlot.FEET));
		this.addSlotToContainer(new Slot((IInventory) npc.inventory, 4, 81, 22));
		this.addSlotToContainer(new Slot((IInventory) npc.inventory, 5, 81, 40));
		this.addSlotToContainer(new Slot((IInventory) npc.inventory, 6, 81, 58));
		/*
		 * Changed for (int l = 0; l < 9; ++l) { this.addSlotToContainer(new
		 * Slot((IInventory)npc.inventory, l + 7, 191, 16 + l * 21)); }
		 */
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l2 = 0; l2 < 9; ++l2) {
				this.addSlotToContainer(
						new Slot((IInventory) player.inventory, l2 + i1 * 9 + 9, l2 * 18 + 8, 113 + i1 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, j1, j1 * 18 + 8, 171));
		}
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}
}
