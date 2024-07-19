package noppes.npcs.containers;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class ContainerNPCInv extends Container {

	public ContainerNPCInv(EntityNPCInterface npc, EntityPlayer player) {
		this.addSlotToContainer(new SlotNPCArmor(npc.inventory, 0, 9, 22, EntityEquipmentSlot.HEAD));
		this.addSlotToContainer(new SlotNPCArmor(npc.inventory, 1, 9, 40, EntityEquipmentSlot.CHEST));
		this.addSlotToContainer(new SlotNPCArmor(npc.inventory, 2, 9, 58, EntityEquipmentSlot.LEGS));
		this.addSlotToContainer(new SlotNPCArmor(npc.inventory, 3, 9, 76, EntityEquipmentSlot.FEET));
		this.addSlotToContainer(new Slot(npc.inventory, 4, 81, 22));
		this.addSlotToContainer(new Slot(npc.inventory, 5, 81, 40));
		this.addSlotToContainer(new Slot(npc.inventory, 6, 81, 58));
		if (ArmourersWorkshopApi.isAvailable()) {
			this.addSlotToContainer(new AWSlotOutfit(npc.inventory, 7, 27, 4));
			this.addSlotToContainer(new AWSlotWings(npc.inventory, 8, 45, 4));
		}
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l2 = 0; l2 < 9; ++l2) {
				this.addSlotToContainer(new Slot(player.inventory, l2 + i1 * 9 + 9, l2 * 18 + 8, 113 + i1 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(player.inventory, j1, j1 * 18 + 8, 171));
		}
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public @Nonnull ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull EntityPlayer player) {
		if (clickTypeIn == ClickType.QUICK_MOVE && dragType == 0) { // shift + LMB
			if (slotId < 0) {
				return ItemStack.EMPTY;
			}
			Slot slot = this.inventorySlots.get(slotId);
			if (slot == null || !slot.canTakeStack(player) || !slot.getHasStack()) {
				return ItemStack.EMPTY;
			}
			ItemStack stackInClickSlot = slot.getStack();
			if (slotId < 7) { // take off your equipment
				if (player.inventory.addItemStackToInventory(stackInClickSlot)) {
					slot.putStack(ItemStack.EMPTY);
				}
			} else if (stackInClickSlot.getItem() instanceof ItemArmor) {
				Slot armSlot;
				switch (((ItemArmor) stackInClickSlot.getItem()).armorType) {
				case HEAD:
					armSlot = this.inventorySlots.get(0);
					break;
				case CHEST:
					armSlot = this.inventorySlots.get(1);
					break;
				case LEGS:
					armSlot = this.inventorySlots.get(2);
					break;
				case FEET:
					armSlot = this.inventorySlots.get(3);
					break;
				default:
					armSlot = null;
					break;
				}
				if (armSlot != null) {
					ItemStack stackInArmSlot = armSlot.getStack().copy();
					armSlot.putStack(stackInClickSlot);
					slot.putStack(stackInArmSlot);
				}
			} else if (stackInClickSlot.getItem() instanceof ItemSword
					|| stackInClickSlot.getItem() instanceof ItemBow) {
				Slot wpnSlot = this.inventorySlots.get(4);
				if (wpnSlot != null) {
					ItemStack stackInWpnSlot = wpnSlot.getStack().copy();
					wpnSlot.putStack(stackInClickSlot);
					slot.putStack(stackInWpnSlot);
				}
			} else if (stackInClickSlot.getItem() instanceof ItemShield) {
				Slot sldSlot = this.inventorySlots.get(6);
				if (sldSlot != null) {
					ItemStack stackInSldSlot = sldSlot.getStack().copy();
					sldSlot.putStack(stackInClickSlot);
					slot.putStack(stackInSldSlot);
				}
			} else { // any projective
				Slot pjcSlot = this.inventorySlots.get(5);
				if (pjcSlot != null) {
					ItemStack stackInPjcSlot = pjcSlot.getStack().copy();
					pjcSlot.putStack(stackInClickSlot);
					slot.putStack(stackInPjcSlot);
				}
			}
			return ItemStack.EMPTY;
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
		return ItemStack.EMPTY;
	}

}
