package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.item.ICustomItem;
import noppes.npcs.util.ObfuscationHelper;

public class CustomArmor
extends ItemArmor
implements ICustomItem {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected ItemStack repairItemStack = ItemStack.EMPTY;
	protected int enchantability = 0;
	
	public CustomArmor(ItemArmor.ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, NBTTagCompound nbtItem) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.nbtData = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_"+nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_"+nbtItem.getString("RegistryName"));
		if (nbtItem.hasKey("IsFull3D", 1) && nbtItem.getBoolean("IsFull3D")) { this.setFull3D(); }
		if (nbtItem.getInteger("MaxStackDamage")>1) { this.setMaxDamage(nbtItem.getInteger("MaxStackDamage")); }
		if (nbtItem.hasKey("DamageReduceAmount", 3)) {
			ObfuscationHelper.setValue(ItemArmor.class, this,  nbtItem.getInteger("DamageReduceAmount"), 5);
		}
		if (nbtItem.hasKey("Toughness", 5)) {
			ObfuscationHelper.setValue(ItemArmor.class, this,  nbtItem.getFloat("Toughness"), 6);
		}
		if (nbtItem.hasKey("RepairItem", 10)) { this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem")); }
		else { this.repairItemStack = materialIn.getRepairItemStack(); }
		if (nbtItem.hasKey("Enchantability", 3)) { this.enchantability = nbtItem.getInteger("Enchantability"); }
		this.setCreativeTab((CreativeTabs) CustomItems.tabItems);
	}
	
	public int getItemEnchantability() {
		if (this.enchantability>0) { return this.enchantability; }
		return super.getItemEnchantability();
	}
	
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		ItemStack mat = this.repairItemStack;
		if (this.repairItemStack.isEmpty()) {
			mat = this.getArmorMaterial().getRepairItemStack();
		}
		if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) { return true; }
		return super.getIsRepairable(toRepair, repair);
	}
	
	public static ItemArmor.ArmorMaterial getMaterialArmor(NBTTagCompound nbtItem) {
		String materialName = nbtItem.hasKey("Material", 8) ? nbtItem.getString("Material").toLowerCase() : "leather";
		switch(materialName) {
			case "diamond":
				return ItemArmor.ArmorMaterial.DIAMOND;
			case "chain":
				return ItemArmor.ArmorMaterial.CHAIN;
			case "iron":
				return ItemArmor.ArmorMaterial.IRON;
			case "gold":
				return ItemArmor.ArmorMaterial.GOLD;
			default:
				return ItemArmor.ArmorMaterial.LEATHER;
		}
	}
	
	public static EntityEquipmentSlot getSlotEquipment(NBTTagCompound nbtItem) {
		String slotName = nbtItem.hasKey("EquipmentSlot", 8) ? nbtItem.getString("EquipmentSlot").toLowerCase() : "feet";
		switch(slotName) {
			case "head":
				return EntityEquipmentSlot.HEAD;
			case "chest":
				return EntityEquipmentSlot.CHEST;
			case "legs":
				return EntityEquipmentSlot.LEGS;
			default:
				return EntityEquipmentSlot.FEET;
		}
	}
	
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return CustomNpcs.MODID+":textures/models/armor/"+this.nbtData.getString("RegistryName")+"_layer_"+(slot==EntityEquipmentSlot.LEGS ? "2" : "1")+".png";
    }

	@Override
	public NBTTagCompound getData() { return this.nbtData; }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
	
}
