package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.item.IItemStack;

public interface INpcRecipe {
	
	void delete();

	IAvailability getAvailability();

	int getHeight();

	int getId();

	boolean getIgnoreDamage();

	boolean getIgnoreNBT();

	String getName();

	IItemStack[][] getRecipe();

	int getWidth();

	boolean isGlobal();

	boolean isKnown();

	boolean isShaped();

	boolean saves();

	void saves(boolean bo);

	void setIgnoreDamage(boolean bo);

	void setIgnoreNBT(boolean bo);

	void setIsGlobal(boolean bo);

	void setKnown(boolean known);
	
	INbt getNbt();
	
	void setNbt(INbt nbt);
	
	String getNpcGroup();

	IItemStack getProduct();

	void copy(INpcRecipe recipe);

	boolean isValid();
	
	boolean equal(INpcRecipe recipe);

}
