package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.item.IItemStack;

public interface INpcRecipe {

	void copy(INpcRecipe recipe);

	void delete();

	boolean equal(INpcRecipe recipe);

	IAvailability getAvailability();

	int getHeight();

	int getId();

	boolean getIgnoreDamage();

	boolean getIgnoreNBT();

	String getName();

	INbt getNbt();

	String getNpcGroup();

	IItemStack getProduct();

	IItemStack[][] getRecipe();

	int getWidth();

	boolean isGlobal();

	boolean isKnown();

	boolean isShaped();

	boolean isValid();

	void setIgnoreDamage(boolean bo);

	void setIgnoreNBT(boolean bo);

	void setKnown(boolean known);

	void setNbt(INbt nbt);

	boolean isRecipeItemsEmpty();

}
