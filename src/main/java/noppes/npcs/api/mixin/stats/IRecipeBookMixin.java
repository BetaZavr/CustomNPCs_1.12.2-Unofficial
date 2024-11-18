package noppes.npcs.api.mixin.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.RecipeBook;

public interface IRecipeBookMixin {

    RecipeBook npcs$copyToNew(boolean isGlobal, EntityPlayer player);

    boolean npcs$checkRecipes();

}
