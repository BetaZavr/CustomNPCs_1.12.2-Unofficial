package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.item.IItemStack;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public interface INPCInventory {

	ICustomDrop addDropItem(@ParamName("item") IItemStack item, @ParamName("chance") double chance);

	IItemStack getArmor(@ParamName("slot") int slot);

	ICustomDrop getDrop(@ParamName("slot") int slot);

	IItemStack getDropItem(@ParamName("slot") int slot);

	ICustomDrop[] getDrops();

	int getExpMax();

	int getExpMin();

	int getExpRNG();

	Map<IEntity<?>, List<IItemStack>> createDrops(@ParamName("lootType") int lootType, @ParamName("baseChance") double baseChance);

	IItemStack getLeftHand();

	IItemStack getProjectile();

	IItemStack getRightHand();

	boolean getXPLootMode();

	boolean removeDrop(@ParamName("drop") ICustomDrop drop);

	boolean removeDrop(@ParamName("slot") int slot);

	void setArmor(@ParamName("slot") int slot, @ParamName("item") IItemStack item);

	void setExp(@ParamName("min") int min, @ParamName("max") int max);

	void setLeftHand(@ParamName("item") IItemStack item);

	void setProjectile(@ParamName("item") IItemStack item);

	void setRightHand(@ParamName("item") IItemStack item);

	void setXPLootMode(@ParamName("mode") boolean mode);

}
