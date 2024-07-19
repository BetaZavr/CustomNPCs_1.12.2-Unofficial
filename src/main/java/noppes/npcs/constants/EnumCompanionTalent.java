package noppes.npcs.constants;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
public enum EnumCompanionTalent
{
	INVENTORY(Item.getItemFromBlock(Blocks.CRAFTING_TABLE)), 
	ARMOR(Items.IRON_CHESTPLATE),
	SWORD(Items.DIAMOND_SWORD), 
	RANGED(Items.BOW),
	ACROBATS(Items.LEATHER_BOOTS),
	INTEL(Items.BOOK);
	
	public final ItemStack item;
	
	EnumCompanionTalent(Item item) {
		this.item = new ItemStack(item);
	}

}
