package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomRegisters;

public class CustomItemTippedArrow
extends ItemTippedArrow {

	
	public CustomItemTippedArrow() {
		super();
		this.setRegistryName(new ResourceLocation("tipped_arrow"));
		this.setUnlocalizedName("tipped_arrow");
	}
	
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.getCreativeTab() == null) { return; }
		for (PotionType potiontype : PotionType.REGISTRY) {
			if (potiontype == PotionTypes.EMPTY) { continue; }
			if (tab == CreativeTabs.SEARCH ||
					(tab == this.getCreativeTab() && !CustomRegisters.custompotiontypes.contains(potiontype)) ||
					(tab == CustomRegisters.tabItems && CustomRegisters.custompotiontypes.contains(potiontype))) {
				items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype));
			}
		}
	}
	
}
