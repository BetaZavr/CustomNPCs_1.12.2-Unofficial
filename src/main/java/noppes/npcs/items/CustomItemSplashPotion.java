package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomRegisters;

public class CustomItemSplashPotion
extends ItemSplashPotion {
	
	public CustomItemSplashPotion() {
		super();
		this.setRegistryName(new ResourceLocation("splash_potion"));
		this.setUnlocalizedName("splash_potion");
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
