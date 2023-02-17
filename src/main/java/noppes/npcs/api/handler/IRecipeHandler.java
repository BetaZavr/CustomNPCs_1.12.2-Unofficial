package noppes.npcs.api.handler;

import java.util.List;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.handler.data.INpcRecipe;

public interface IRecipeHandler {

	INpcRecipe getRecipe(String group, String name);
	
	INpcRecipe getRecipe(int id);
	
	INpcRecipe addRecipe(String name, String group, boolean global, boolean known, ItemStack result, int width,
			int height, ItemStack... stacks);

	INpcRecipe addRecipe(String name, String group, boolean global, boolean known, ItemStack result, Object... objects);

	boolean delete(String name, String group);
	
	boolean delete(int id);

	List<INpcRecipe> getCarpentryData();

	List<INpcRecipe> getCarpentryRecipes(String group);

	List<INpcRecipe> getGlobalData();

	List<INpcRecipe> getGlobalRecipes(String group);

}
