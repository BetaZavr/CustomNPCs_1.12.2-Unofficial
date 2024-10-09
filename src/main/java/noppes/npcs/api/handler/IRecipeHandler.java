package noppes.npcs.api.handler;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.handler.data.INpcRecipe;

public interface IRecipeHandler {

	INpcRecipe addRecipe(String group, String name, boolean isGlobal, boolean isShaped, boolean isKnown, ItemStack result, ItemStack[][] stacks);

	INpcRecipe addRecipe(String group, String name, boolean isGlobal, boolean isShaped, boolean isKnown, ItemStack result, Object[] objects);

	boolean delete(int id);

	boolean delete(boolean isGlobal, String group, String name);

	INpcRecipe[] getCarpentryData();

	INpcRecipe[] getCarpentryRecipes(String group);

	INpcRecipe[] getGlobalData();

	INpcRecipe[] getGlobalRecipes(String group);

	INpcRecipe getRecipe(int id);

	INpcRecipe getRecipe(boolean isGlobal, String group, String name);

}
