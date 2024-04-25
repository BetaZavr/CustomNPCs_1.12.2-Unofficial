package noppes.npcs.api.entity;

import net.minecraft.entity.item.EntityItem;
import noppes.npcs.api.item.IItemStack;

public interface IEntityItem<T extends EntityItem> extends IEntity<T> {

	long getAge();

	IItemStack getItem();

	int getLifeSpawn();

	String getOwner();

	int getPickupDelay();

	void setAge(long age);

	void setItem(IItemStack item);

	void setLifeSpawn(int age);

	void setOwner(String name);

	void setPickupDelay(int delay);

}
