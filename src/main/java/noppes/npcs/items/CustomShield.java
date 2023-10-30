package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomRegisters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;

public class CustomShield
extends ItemShield
implements ICustomElement {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected ItemStack repairItemStack = ItemStack.EMPTY;
	protected int enchantability = 0;
	protected Item.ToolMaterial material = Item.ToolMaterial.WOOD;
	
	public CustomShield(NBTTagCompound nbtItem) {
		super();
		this.nbtData  = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_"+nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_"+nbtItem.getString("RegistryName"));
		if (nbtItem.getBoolean("IsFull3D")) { this.setFull3D(); }
		if (nbtItem.getInteger("MaxStackDamage")>1) { this.setMaxDamage(nbtItem.getInteger("MaxStackDamage")); }
		if (nbtItem.hasKey("Material", 8)) { this.material = CustomItem.getMaterialTool(nbtItem); }
		if (nbtItem.hasKey("RepairItem", 10)) { this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem")); }
		else if (this.material!=null) { this.repairItemStack = this.material.getRepairItemStack(); }
		if (nbtItem.hasKey("Enchantability", 3)) { this.enchantability = nbtItem.getInteger("Enchantability"); }
		if (nbtItem.hasKey("IsFull3D", 1) && nbtItem.getBoolean("IsFull3D")) { this.setFull3D(); }
		this.setCreativeTab((CreativeTabs) CustomRegisters.tabItems);
	}
	
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab!=CustomRegisters.tabItems || (this.nbtData!=null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative"))) { return; }
		items.add(new ItemStack(this));
	}
	
	public String getItemStackDisplayName(ItemStack stack) {
		return new TextComponentTranslation(this.getUnlocalizedNameInefficiently(stack) + ".name").getFormattedText();
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		ItemStack mat = this.repairItemStack;
		if (this.repairItemStack.isEmpty()) {
			mat = this.material.getRepairItemStack();
		}
		if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) { return true; }
		return super.getIsRepairable(toRepair, repair);
	}

	public int getItemEnchantability() {
		if (this.enchantability>0) { return this.enchantability; }
		return super.getItemEnchantability();
	}
	
	@SideOnly(Side.CLIENT)
    public boolean isFull3D() { return this.bFull3D; }


	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
	
}
