package noppes.npcs.api.entity;

import net.minecraft.entity.item.EntityItem;
import noppes.npcs.api.item.IItemStack;

public interface IEntityItem<T extends EntityItem> extends IEntity<T> {
	long getAge();

	IItemStack getItem();

	int getLifeSpawn();

	String getOwner();

	int getPickupDelay();

	void setAge(long p0);

	void setItem(IItemStack p0);

	void setLifeSpawn(int p0);

	void setOwner(String p0);

	void setPickupDelay(int p0);
}
