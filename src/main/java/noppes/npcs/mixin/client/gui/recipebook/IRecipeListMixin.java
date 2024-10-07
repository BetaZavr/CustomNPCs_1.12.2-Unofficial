package noppes.npcs.mixin.client.gui.recipebook;

import noppes.npcs.api.handler.data.INpcRecipe;

public interface IRecipeListMixin {

    boolean npcs$applyRecipe(INpcRecipe recipe, boolean added);

    String npcs$getGroup();

}
