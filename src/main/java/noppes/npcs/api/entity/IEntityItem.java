package noppes.npcs.api.entity;

import net.minecraft.entity.item.EntityItem;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IEntityItem<T extends EntityItem> extends IEntity<T> {

	long getAge();

	IItemStack getItem();

	int getLifeSpawn();

	String getOwner();

	int getPickupDelay();

	void setAge(@ParamName("age") long age);

	void setItem(@ParamName("item") IItemStack item);

	void setLifeSpawn(@ParamName("age") int age);

	void setOwner(@ParamName("name") String name);

	void setPickupDelay(@ParamName("delay") int delay);

}
