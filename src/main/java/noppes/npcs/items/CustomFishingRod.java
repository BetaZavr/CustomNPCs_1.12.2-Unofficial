package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.util.AdditionalMethods;

public class CustomFishingRod extends ItemFishingRod implements ICustomElement {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected int enchantability = 1;
	protected ItemStack repairItemStack = ItemStack.EMPTY;

	public CustomFishingRod(NBTTagCompound nbtItem) {
		super();
		this.nbtData = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_" + nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_" + nbtItem.getString("RegistryName"));

		if (nbtItem.hasKey("RepairItem", 10)) {
			this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem"));
		}
		this.maxStackSize = nbtItem.hasKey("MaxStackSize", 3) ? nbtItem.getInteger("MaxStackSize") : 1;
		if (nbtItem.getInteger("MaxStackDamage") > 1) {
			this.setMaxDamage(nbtItem.getInteger("MaxStackDamage"));
		}
		if (nbtItem.hasKey("Enchantability", 3)) {
			this.enchantability = nbtItem.getInteger("Enchantability");
		}

		this.setCreativeTab((CreativeTabs) CustomRegisters.tabItems);
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return NpcAPI.Instance().getINbt(this.nbtData);
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (this.repairItemStack.isEmpty()) {
			return super.getIsRepairable(toRepair, repair);
		}
		ItemStack mat = this.repairItemStack;
		if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) {
			return true;
		}
		return super.getIsRepairable(toRepair, repair);
	}

	public int getItemEnchantability() {
		return this.enchantability;
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabItems && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative")) { return; }
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabItems) { AdditionalMethods.instance.sort(items); }
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("ItemType", 1)) { return (int) this.nbtData.getByte("ItemType"); }
		return 8;
	}

}
