package noppes.npcs.api.handler;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.handler.data.INpcRecipe;

public interface IRecipeHandler {

	INpcRecipe addRecipe(String group, String name, boolean global, boolean known, ItemStack result, int width,
			int height, ItemStack[] stacks);

	INpcRecipe addRecipe(String group, String name, boolean global, boolean known, ItemStack result, Object[] objects);

	boolean delete(int id);

	boolean delete(String group, String name);

	INpcRecipe[] getCarpentryData();

	INpcRecipe[] getCarpentryRecipes(String group);

	INpcRecipe[] getGlobalData();

	INpcRecipe[] getGlobalRecipes(String group);

	INpcRecipe getRecipe(int id);

	INpcRecipe getRecipe(String group, String name);

}
