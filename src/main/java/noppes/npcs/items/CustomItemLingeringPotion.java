package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;

public class CustomItemLingeringPotion
extends ItemLingeringPotion {

	public CustomItemLingeringPotion() {
		super();
		this.setRegistryName(new ResourceLocation("lingering_potion"));
		this.setUnlocalizedName("lingering_potion");
	}
	
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.getCreativeTab() == null) { return; }
    	for (PotionType potiontype : PotionType.REGISTRY) {
            if (potiontype == PotionTypes.EMPTY) { continue; }
            if (tab == CreativeTabs.SEARCH ||
            		(tab == this.getCreativeTab() && !CustomItems.custompotiontypes.contains(potiontype)) ||
            		(tab == CustomItems.tabItems && CustomItems.custompotiontypes.contains(potiontype))) {
            	items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype));
            }
        }
    }
	
}
