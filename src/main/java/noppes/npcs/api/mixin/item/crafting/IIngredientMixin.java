package noppes.npcs.api.mixin.item.crafting;

import net.minecraft.item.ItemStack;

public interface IIngredientMixin {

    ItemStack[] npcs$getRawMatchingStacks();

}
