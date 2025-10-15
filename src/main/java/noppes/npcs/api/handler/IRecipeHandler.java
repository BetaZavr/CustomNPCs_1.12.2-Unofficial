package noppes.npcs.api.handler;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.INpcRecipe;

@SuppressWarnings("all")
public interface IRecipeHandler {

	INpcRecipe addRecipe(@ParamName("group") String group, @ParamName("name") String name,
						 @ParamName("isGlobal") boolean isGlobal, @ParamName("isShaped") boolean isShaped, @ParamName("isKnown") boolean isKnown,
						 @ParamName("result") ItemStack result, @ParamName("stacks") ItemStack[][] stacks);

	INpcRecipe addRecipe(@ParamName("group") String group, @ParamName("name") String name,
						 @ParamName("isGlobal") boolean isGlobal, @ParamName("isShaped") boolean isShaped, @ParamName("isKnown") boolean isKnown,
						 @ParamName("result") ItemStack result, @ParamName("objects") Object... objects);

	boolean delete(@ParamName("id") int id);

	boolean delete(@ParamName("isGlobal") boolean isGlobal, @ParamName("group") String group, @ParamName("name") String name);

	INpcRecipe[] getCarpentryData();

	INpcRecipe[] getCarpentryRecipes(@ParamName("group") String group);

	INpcRecipe[] getGlobalData();

	INpcRecipe[] getGlobalRecipes(@ParamName("group") String group);

	INpcRecipe getRecipe(@ParamName("id") int id);

	INpcRecipe getRecipe(@ParamName("isGlobal") boolean isGlobal, @ParamName("group") String group, @ParamName("name") String name);

}
