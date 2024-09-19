package noppes.npcs.mixin.api.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Ingredient.class)
public interface IngredientAPIMixin {

    @Accessor(value="matchingStacks")
    ItemStack[] npcs$getMatchingStacks();

}
