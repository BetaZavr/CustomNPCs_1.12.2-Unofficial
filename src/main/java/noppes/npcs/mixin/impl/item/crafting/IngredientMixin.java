package noppes.npcs.mixin.impl.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import noppes.npcs.mixin.item.crafting.IIngredientMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Ingredient.class)
public class IngredientMixin implements IIngredientMixin {

    /*
     * matchingStacks - source items for customizing ingredients
     * matchingStacksExploded - ingredient items used in crafting
     */
    @Final
    @Shadow
    private ItemStack[] matchingStacks;

    @Override
    public ItemStack[] npcs$getRawMatchingStacks() { return matchingStacks; }

}
