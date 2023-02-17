package noppes.npcs.api.handler.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface INpcRecipe {
	
	void delete();

	IAvailability getAvailability();

	int getHeight();

	int getId();

	boolean getIgnoreDamage();

	boolean getIgnoreNBT();

	String getName();

	ItemStack[][] getRecipe();

	ItemStack getResult();

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
	
	NBTTagCompound writeNBT();
	
	String getNpcGroup();

	ItemStack getProduct();

	void copy(INpcRecipe recipe);

	boolean isValid();
	
	boolean equal(INpcRecipe recipe);

}
