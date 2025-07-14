package noppes.npcs.mixin.client.gui.recipebook;

import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.item.crafting.IRecipe;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.mixin.client.gui.recipebook.IRecipeListMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Objects;

@Mixin(value = RecipeList.class, priority = 499)
public class RecipeListMixin implements IRecipeListMixin {

    @Unique
    private String npcs$groupName = "";

    @Shadow
    private List<IRecipe> recipes;

    @Override
    public boolean npcs$applyRecipe(INpcRecipe recipe, boolean added) {
        for (IRecipe rec : recipes) {
            if (rec instanceof INpcRecipe && ((INpcRecipe) rec).getId() == recipe.getId()) {
                if (!added) { recipes.remove(rec); }
                else { ((INpcRecipe) rec).copy(recipe); }
                return true;
            }
        }
        if (added) {
            recipes.add((IRecipe) recipe);
            return true;
        }
        return false;
    }

    @Override
    public String npcs$getGroup() {
        if (npcs$groupName.isEmpty()) {
            for (IRecipe rec : recipes) {
                if (!rec.getGroup().isEmpty()) {
                    npcs$groupName = rec.getGroup();
                    break;
                }
            }
            if (npcs$groupName.isEmpty() && !recipes.isEmpty()) {
                npcs$groupName = Objects.requireNonNull(recipes.get(0).getRecipeOutput().getItem().getRegistryName()).toString();
            }
        }
        return npcs$groupName;
    }

}
