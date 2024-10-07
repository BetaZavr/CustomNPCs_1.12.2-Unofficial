package noppes.npcs.mixin.impl.client.gui.recipebook;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.mixin.client.gui.recipebook.IRecipeListMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;

@Mixin(value = RecipeList.class)
public class RecipeListMixin implements IRecipeListMixin {

    @Unique
    private String npcs$groupName = "";

    @Shadow
    private List<IRecipe> recipes;
    @Final
    @Shadow
    private BitSet craftable;
    @Final
    @Shadow
    private BitSet canFit;

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
