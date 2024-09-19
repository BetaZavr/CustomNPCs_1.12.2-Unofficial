package noppes.npcs.mixin.stats;

import net.minecraft.stats.RecipeBook;
import noppes.npcs.mixin.api.stats.RecipeBookAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.BitSet;

@Mixin(value = RecipeBook.class)
public class RecipeBookMixin implements RecipeBookAPIMixin {

    @Mutable
    @Final
    @Shadow(aliases = "recipes")
    protected BitSet recipes = new BitSet();

    @Mutable
    @Final
    @Shadow(aliases = "newRecipes")
    protected BitSet newRecipes = new BitSet();

    @Override
    public BitSet npcs$getRecipes() { return recipes; }

    @Override
    public void npcs$setRecipes(BitSet newRecipes) {
        if (newRecipes == null) { return; }
        recipes = newRecipes;
    }

    @Override
    public BitSet npcs$getNewRecipes() { return newRecipes; }

    @Override
    public void npcs$setNewRecipes(BitSet recipes) {
        if (recipes == null) { return; }
        newRecipes = recipes ;
    }
}
