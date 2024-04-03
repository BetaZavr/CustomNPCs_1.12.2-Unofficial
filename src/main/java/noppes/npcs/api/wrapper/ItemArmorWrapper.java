package noppes.npcs.api.wrapper;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import noppes.npcs.api.constants.ItemType;
import noppes.npcs.api.item.IItemArmor;

public class ItemArmorWrapper
extends ItemStackWrapper
implements IItemArmor {
	
	protected ItemArmor armor;

	protected ItemArmorWrapper(ItemStack item) {
		super(item);
		this.armor = (ItemArmor) item.getItem();
	}

	@Override
	public String getArmorMaterial() {
		return this.armor.getArmorMaterial().getName();
	}

	@Override
	public int getArmorSlot() {
		return this.armor.getEquipmentSlot().getSlotIndex();
	}

	@Override
	public int getType() {
		return ItemType.ARMOR.get();
	}

	@Override
	public float getToughness() { return armor.getArmorMaterial().getToughness(); }
	
	@Override
	public int getArmorValue() { return armor.getArmorMaterial().getDamageReductionAmount(armor.getEquipmentSlot()); }
	
}
