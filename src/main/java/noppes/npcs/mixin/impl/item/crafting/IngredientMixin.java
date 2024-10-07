package noppes.npcs.mixin.impl.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import noppes.npcs.mixin.item.crafting.IIngredientMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Ingredient.class)
public class IngredientMixin implements IIngredientMixin {

    @Final
    @Shadow(aliases = "matchingStacks")
    private ItemStack[] matchingStacks;

    @Override
    public ItemStack[] npcs$getMatchingStacks() { return matchingStacks; }

}
