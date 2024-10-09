package noppes.npcs.mixin.item.crafting;

import net.minecraft.item.ItemStack;

public interface IIngredientMixin {

    ItemStack[] npcs$getRawMatchingStacks();

}
