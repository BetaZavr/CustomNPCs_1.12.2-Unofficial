package noppes.npcs.api.handler.data;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.data.DropSet;

public interface IDeal {

	int getAmount();

	IAvailability getAvailability();

	int getChance();

	IContainer getCurrency();

	int getId();

	boolean getIgnoreDamage();

	boolean getIgnoreNBT();

	int getMaxCount();

	IInventory getMCInventoryCurrency();

	IInventory getMCInventoryProduct();

	int getMinCount();

	int getMoney();

    int getDonat();

    String getName();

	IItemStack getProduct();

	int getType();

	void set(IItemStack product, IItemStack[] currencys);

	void setAmount(int amount);

	void setChance(int chance);

	void setCount(int min, int max);

	void setIgnoreDamage(boolean bo);

	void setIgnoreNBT(boolean bo);

	void setMoney(int money);

	void setDonat(int moneyIn);

	void setProduct(IItemStack product);

	void setType(int type);

	int getRarityColor();

	void setRarityColor(int color);

	boolean isCase();

	void setIsCase(boolean isCaseIn);

	int getCaseCount();

	void setCaseCount(int count);

	String getCaseName();

	void setCaseName(String newName);

	String getCaseCommand();

	void setCaseCommand(String command);

	ResourceLocation getCaseObjModel();

	void setCaseObjModel(ResourceLocation objModel);

	ResourceLocation getCaseSound();

	void setCaseSound(ResourceLocation sound);

	ResourceLocation getCaseTexture();

	void setCaseTexture(ResourceLocation texture);

	boolean showInCase();

	void setShowInCase(boolean show);

	DropSet addCaseItem(IItemStack item, double chance);

	ICustomDrop getCase(int slot);

	IItemStack getCaseItem(int slot);

	DropSet[] getCaseItems();

	boolean removeCaseItem(ICustomDrop drop);

	boolean removeCaseItem(int slot);

	void updateNew();

}
