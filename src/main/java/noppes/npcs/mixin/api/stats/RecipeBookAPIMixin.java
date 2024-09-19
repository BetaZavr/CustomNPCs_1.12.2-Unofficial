package noppes.npcs.mixin.api.stats;

import net.minecraft.stats.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(value = RecipeBook.class)
public interface RecipeBookAPIMixin {

    @Accessor(value="recipes")
    BitSet npcs$getRecipes();

    @Mutable
    @Accessor(value="recipes")
    void npcs$setRecipes(BitSet newRecipes);

    @Accessor(value="newRecipes")
    BitSet npcs$getNewRecipes();

    @Mutable
    @Accessor(value="newRecipes")
    void npcs$setNewRecipes(BitSet newRecipes);

}
