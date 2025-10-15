package noppes.npcs.api.handler.data;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.data.DropSet;

@SuppressWarnings("all")
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

	void set(@ParamName("product") IItemStack product, @ParamName("currencies") IItemStack[] currencies);

	void setAmount(@ParamName("amount") int amount);

	void setChance(@ParamName("chance") int chance);

	void setCount(@ParamName("min") int min, @ParamName("max") int max);

	void setIgnoreDamage(@ParamName("bo") boolean bo);

	void setIgnoreNBT(@ParamName("bo") boolean bo);

	void setMoney(@ParamName("money") int money);

	void setDonat(@ParamName("moneyIn") int moneyIn);

	void setProduct(@ParamName("product") IItemStack product);

	void setType(@ParamName("type") int type);

	int getRarityColor();

	void setRarityColor(@ParamName("color") int color);

	boolean isCase();

	void setIsCase(@ParamName("isCaseIn") boolean isCaseIn);

	int getCaseCount();

	void setCaseCount(@ParamName("count") int count);

	String getCaseName();

	void setCaseName(@ParamName("newName") String newName);

	String getCaseCommand();

	void setCaseCommand(@ParamName("command") String command);

	ResourceLocation getCaseObjModel();

	void setCaseObjModel(@ParamName("objModel") ResourceLocation objModel);

	ResourceLocation getCaseSound();

	void setCaseSound(@ParamName("sound") ResourceLocation sound);

	ResourceLocation getCaseTexture();

	void setCaseTexture(@ParamName("texture") ResourceLocation texture);

	boolean showInCase();

	void setShowInCase(@ParamName("show") boolean show);

	DropSet addCaseItem(@ParamName("item") IItemStack item, @ParamName("chance") double chance);

	ICustomDrop getCase(@ParamName("slot") int slot);

	IItemStack getCaseItem(@ParamName("slot") int slot);

	DropSet[] getCaseItems();

	boolean removeCaseItem(@ParamName("drop") ICustomDrop drop);

	boolean removeCaseItem(@ParamName("slot") int slot);

	void updateNew();

}
