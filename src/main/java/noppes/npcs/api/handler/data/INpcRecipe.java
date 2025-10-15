package noppes.npcs.api.handler.data;

import net.minecraft.item.ItemStack;
import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.WrapperRecipe;

@SuppressWarnings("all")
public interface INpcRecipe {

	void copy(@ParamName("recipe") INpcRecipe recipe);

	void delete();

	boolean equal(@ParamName("recipe") INpcRecipe recipe);

	IAvailability getAvailability();

	int getHeightRecipe();

	int getId();

	boolean getIgnoreDamage();

	boolean getIgnoreNBT();

	String getName();

	INbt getNbt();

	String getNpcGroup();

	IItemStack getProduct();

	IItemStack[][] getRecipe();

	int getWidthRecipe();

	boolean isGlobal();

	boolean isKnown();

	boolean isShaped();

	boolean isValid();

	void setIgnoreDamage(@ParamName("ignoreDamage") boolean ignoreDamage);

	void setIgnoreNBT(@ParamName("ignoreNBT") boolean ignoreNBT);

	void setKnown(@ParamName("known") boolean known);

	void setNbt(@ParamName("nbt") INbt nbt);

	boolean isRecipeItemsEmpty();

	WrapperRecipe getWrapperRecipe();

    boolean isMain();

	boolean isChanged();

    void setRecipeOutput(@ParamName("item") ItemStack item);

}
