package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;

public class CustomFishingRod
extends ItemFishingRod
implements ICustomElement {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected int enchantability = 1;
	protected ItemStack repairItemStack = ItemStack.EMPTY;
	
	public CustomFishingRod(NBTTagCompound nbtItem) {
		super();
		this.nbtData  = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_"+nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_"+nbtItem.getString("RegistryName"));

		if (nbtItem.hasKey("RepairItem", 10)) { this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem")); }
		this.maxStackSize = nbtItem.hasKey("MaxStackSize", 3) ? nbtItem.getInteger("MaxStackSize") : 1;
		if (nbtItem.getInteger("MaxStackDamage")>1) { this.setMaxDamage(nbtItem.getInteger("MaxStackDamage")); }
		if (nbtItem.hasKey("Enchantability", 3)) { this.enchantability = nbtItem.getInteger("Enchantability"); }
		
		this.setCreativeTab((CreativeTabs) CustomItems.tabItems);
	}
	
	public int getItemEnchantability() { return this.enchantability; }

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (this.repairItemStack.isEmpty()) { return super.getIsRepairable(toRepair, repair); }
		ItemStack mat = this.repairItemStack;
		if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) { return true; }
		return super.getIsRepairable(toRepair, repair);
	}
	
	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }

}
