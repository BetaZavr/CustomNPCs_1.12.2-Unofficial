package noppes.npcs.constants;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum EnumCompanionTalent {
	INVENTORY(Item.getItemFromBlock(Blocks.CRAFTING_TABLE)), ARMOR((Item) Items.IRON_CHESTPLATE), SWORD(
			Items.DIAMOND_SWORD), RANGED((Item) Items.BOW), ACROBATS((Item) Items.LEATHER_BOOTS), INTEL(Items.BOOK);

	public ItemStack item;

	private EnumCompanionTalent(Item item) {
		this.item = new ItemStack(item);
	}
}
