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
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class CustomItemPotion extends ItemPotion {

	public CustomItemPotion() {
		super();
		this.setRegistryName(new ResourceLocation("potion"));
		this.setUnlocalizedName("potion");
	}

	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (getCreativeTab() == null) {
			return;
		}
		if (tab == CreativeTabs.BREWING) {
			super.getSubItems(tab, items);
			Util.instance.sort(items);
		}
		if (tab != CustomRegisters.tabItems && tab != CreativeTabs.SEARCH) { return; }
		for (PotionType potiontype : PotionType.REGISTRY) {
			if (potiontype == PotionTypes.EMPTY) {
				continue;
			}
			if (tab == CustomRegisters.tabItems && CustomRegisters.custompotiontypes.containsKey(potiontype)) {
				PotionData data = CustomRegisters.custompotiontypes.get(potiontype);
				if (data.nbtData != null && data.nbtData.hasKey("ShowInCreative", 1) && !data.nbtData.getBoolean("ShowInCreative")) {
					continue;
				}
				ItemStack stack = PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype);
				if (data.nbtData != null && data.nbtData.hasKey("MaxStackSize", 3)) {
					int count = data.nbtData.getInteger("MaxStackSize");
					if (count < 1) {
						count = 1;
					}
					if (count > 64) {
						count = 64;
					}
					stack.getItem().setMaxStackSize(count);
					stack.setCount(count);
				}
				items.add(stack);
            } else if (tab == CreativeTabs.SEARCH || (tab == getCreativeTab() && !CustomRegisters.custompotiontypes.containsKey(potiontype))) {
				items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype));
			}
		}
		if (tab == CustomRegisters.tabItems) { Util.instance.sort(items); }
	}

}
