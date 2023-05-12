package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

public class ContainerNPCCompanion extends ContainerNpcInterface {
	public InventoryNPC currencyMatrix;
	public RoleCompanion role;

	public ContainerNPCCompanion(EntityNPCInterface npc, EntityPlayer player) {
		super(player);
		this.role = (RoleCompanion) npc.advanced.roleInterface;
		for (int k = 0; k < 3; ++k) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlotToContainer(
						new Slot((IInventory) player.inventory, j1 + k * 9 + 9, 6 + j1 * 18, 87 + k * 18));
			}
		}
		for (int l = 0; l < 9; ++l) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, l, 6 + l * 18, 145));
		}
		if (this.role.talents.containsKey(EnumCompanionTalent.INVENTORY)) {
			for (int size = (this.role.getTalentLevel(EnumCompanionTalent.INVENTORY) + 1) * 2, i = 0; i < size; ++i) {
				this.addSlotToContainer(
						new Slot((IInventory) this.role.inventory, i, 114 + i % 3 * 18, 8 + i / 3 * 18));
			}
		}
		if (this.role.getTalentLevel(EnumCompanionTalent.ARMOR) > 0) {
			this.addSlotToContainer((Slot) new SlotCompanionArmor(this.role, (IInventory) npc.inventory, 0, 6, 8,
					EntityEquipmentSlot.HEAD));
			this.addSlotToContainer((Slot) new SlotCompanionArmor(this.role, (IInventory) npc.inventory, 1, 6, 26,
					EntityEquipmentSlot.CHEST));
			this.addSlotToContainer((Slot) new SlotCompanionArmor(this.role, (IInventory) npc.inventory, 2, 6, 44,
					EntityEquipmentSlot.LEGS));
			this.addSlotToContainer((Slot) new SlotCompanionArmor(this.role, (IInventory) npc.inventory, 3, 6, 62,
					EntityEquipmentSlot.FEET));
		}
		if (this.role.getTalentLevel(EnumCompanionTalent.SWORD) > 0) {
			this.addSlotToContainer((Slot) new SlotCompanionWeapon(this.role, (IInventory) npc.inventory, 4, 79, 17));
		}
	}

	public void onContainerClosed(EntityPlayer entityplayer) {
		super.onContainerClosed(entityplayer);
		this.role.setStats();
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}
}
