package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomRegisters;
import noppes.npcs.potions.PotionData;

public class CustomItemPotion
extends ItemPotion {
	
	public CustomItemPotion() {
		super();
		this.setRegistryName(new ResourceLocation("potion"));
		this.setUnlocalizedName("potion");
	}
	
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.getCreativeTab() == null) { return; }
    	for (PotionType potiontype : PotionType.REGISTRY) {
            if (potiontype == PotionTypes.EMPTY) { continue; }
            if (tab == CustomRegisters.tabItems && CustomRegisters.custompotiontypes.containsKey(potiontype)) {
            	PotionData data = CustomRegisters.custompotiontypes.get(potiontype);
        		if (data.nbtData!=null && data.nbtData.hasKey("ShowInCreative", 1) && !data.nbtData.getBoolean("ShowInCreative")) { continue; }
            	items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype));
            	continue;
            }
            else if (tab == CreativeTabs.SEARCH || (tab == this.getCreativeTab() && !CustomRegisters.custompotiontypes.containsKey(potiontype))) {
            	items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype));
            }
        }
    }
	
}
