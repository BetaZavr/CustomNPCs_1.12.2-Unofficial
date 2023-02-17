package noppes.npcs.api.entity.data;

import net.minecraft.entity.Entity;
import noppes.npcs.api.item.IItemStack;

public interface INPCInventory {
	// New
	ICustomDrop addDropItem(IItemStack item, double chance);

	IItemStack getArmor(int p0);

	ICustomDrop getDrop(int slot);

	ICustomDrop[] getDrops();

	int getExpMax();

	int getExpMin();

	int getExpRNG();

	IItemStack[] getItemsRNG(Entity attacking);

	// void setDropItem(int p0, IItemStack p1, int p2); Changed

	// IItemStack getDropItem(int p0); Changed

	IItemStack[] getItemsRNGL(Entity attacking);

	IItemStack getLeftHand();

	IItemStack getProjectile();

	IItemStack getRightHand();

	// IItemStack[] getItemsRNG(); Changed

	boolean getXPLootMode();

	boolean removeDrop(ICustomDrop drop);

	boolean removeDrop(int slot);

	void setArmor(int p0, IItemStack p1);

	void setExp(int p0, int p1);

	void setLeftHand(IItemStack p0);

	void setProjectile(IItemStack p0);

	void setRightHand(IItemStack p0);

	void setXPLootMode(boolean mode);
}
