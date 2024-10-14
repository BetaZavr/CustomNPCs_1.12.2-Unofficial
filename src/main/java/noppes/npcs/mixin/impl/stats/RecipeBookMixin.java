package noppes.npcs.mixin.impl.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.mixin.client.stats.IRecipeBookMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

@Mixin(value = RecipeBook.class)
public class RecipeBookMixin implements IRecipeBookMixin {

    @Mutable
    @Final
    @Shadow
    protected BitSet recipes;

    @Mutable
    @Final
    @Shadow
    protected BitSet newRecipes;

    @Deprecated //DO NOT USE
    @Inject(method = "isUnlocked", at = @At("HEAD"), cancellable = true)
    public void npcs$isUnlocked(IRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        int recipeID = CraftingManager.REGISTRY.getIDForObject(recipe);
        if (recipeID == -1) {
            recipeID = ((net.minecraftforge.registries.ForgeRegistry<IRecipe>) net.minecraftforge.fml.common.registry.ForgeRegistries.RECIPES).getID(recipe.getRegistryName());
        }
        if (recipeID == -1) {
            cir.setReturnValue(false);
            LogWriter.error(String.format("Attempted to get the ID for a unknown recipe: %s Name: %s", recipe, recipe.getRegistryName()));
        } else {
            cir.setReturnValue(this.recipes.get(recipeID));
        }
    }

    @Override
    public RecipeBook npcs$copyToNew(boolean isGlobal, EntityPlayer player) {
        RecipeBook newBook = new RecipeBook();
        for (int id = 0; id < recipes.length(); ++id) {
            if (recipes.get(id)) {
                IRecipe irecipe = CraftingManager.REGISTRY.getObjectById(id);
                if (irecipe != null) {
                    //if (irecipe instanceof INpcRecipe && (!((INpcRecipe) irecipe).isValid() || !((Availability) ((INpcRecipe) irecipe).getAvailability()).isAvailable(player))) { continue; }
                    if (irecipe instanceof INpcRecipe && !((INpcRecipe) irecipe).isValid()) { continue; }
                    if (isGlobal) {
                        if (!(irecipe instanceof INpcRecipe) || ((INpcRecipe) irecipe).isGlobal()) { newBook.unlock(irecipe); }
                    } else {
                        if (irecipe instanceof INpcRecipe && !((INpcRecipe) irecipe).isGlobal()) { newBook.unlock(irecipe); }
                    }
                }
            }
        }
        for (int id = 0; id < newRecipes.length(); ++id) {
            if (newRecipes.get(id)) {
                IRecipe irecipe = CraftingManager.REGISTRY.getObjectById(id);
                if (irecipe != null) {
                    if (isGlobal) {
                        if (!(irecipe instanceof INpcRecipe) || ((INpcRecipe) irecipe).isGlobal()) { newBook.markNew(irecipe); }
                    } else {
                        if (irecipe instanceof INpcRecipe && !((INpcRecipe) irecipe).isGlobal()) { newBook.markNew(irecipe); }
                    }
                }
            }
        }
        return newBook;
    }

    @Override
    public boolean npcs$checkRecipes() {
        boolean bo = true;
        BitSet recipesN = new BitSet();
        BitSet newRecipesN = new BitSet();
        for (int id = 0; id < recipes.length(); ++id) {
            if (recipes.get(id)) {
                IRecipe irecipe = CraftingManager.REGISTRY.getObjectById(id);
                if (irecipe != null) { recipesN.set(id); } else { bo = false; }
            }
        }
        for (int id = 0; id < newRecipes.length(); ++id) {
            if (newRecipes.get(id)) {
                IRecipe irecipe = CraftingManager.REGISTRY.getObjectById(id);
                if (irecipe != null) { newRecipesN.set(id); } else { bo = false; }
            }
        }
        if (!bo) {
            recipes = recipesN;
            newRecipes = newRecipesN;
        }
        return bo;
    }

}
