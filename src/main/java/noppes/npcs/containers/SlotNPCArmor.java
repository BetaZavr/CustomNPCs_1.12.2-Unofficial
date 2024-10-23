package noppes.npcs.containers;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

class SlotNPCArmor extends Slot {

	EntityEquipmentSlot armorType;

	SlotNPCArmor(IInventory iinventory, int i, int j, int k, EntityEquipmentSlot l) {
		super(iinventory, i, j, k);
		this.armorType = l;
	}

	public int getSlotStackLimit() {
		return 1;
	}

	@SideOnly(Side.CLIENT)
	public String getSlotTexture() {
		return ItemArmor.EMPTY_SLOT_NAMES[this.armorType.getIndex()];
	}

	public boolean isItemValid(@Nonnull ItemStack itemstack) {
		if (itemstack.getItem() instanceof ItemArmor) {
			return ((ItemArmor) itemstack.getItem()).armorType == armorType;
		}
		if (itemstack.getItem() instanceof ItemBlock || itemstack.getItem() instanceof ItemSword || itemstack.getItem() instanceof ItemBow) {
			return armorType == EntityEquipmentSlot.HEAD;
		}
		if (ArmourersWorkshopApi.isAvailable() && ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(itemstack)) {
			ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(itemstack);
			return skinDescriptor.getIdentifier().getSkinType().getName().equalsIgnoreCase(armorType.getName());
		}
		return false;
	}
}
