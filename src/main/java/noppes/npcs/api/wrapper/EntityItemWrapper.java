package noppes.npcs.api.wrapper;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.util.ObfuscationHelper;

@SuppressWarnings("rawtypes")
public class EntityItemWrapper<T extends EntityItem>
extends EntityWrapper<T>
implements IEntityItem {
	
	public EntityItemWrapper(T entity) {
		super(entity);
	}

	@Override
	public long getAge() {
		return ObfuscationHelper.getValue(EntityItem.class, this.entity, 2);
	}

	@Override
	public IItemStack getItem() {
		return NpcAPI.Instance().getIItemStack(this.entity.getItem());
	}

	@Override
	public int getLifeSpawn() {
		return this.entity.lifespan;
	}

	@Override
	public String getOwner() {
		return this.entity.getOwner();
	}

	@Override
	public int getPickupDelay() {
		return ObfuscationHelper.getValue(EntityItem.class, this.entity, 3);
	}

	@Override
	public int getType() {
		return 6;
	}

	@Override
	public void setAge(long age) {
		age = Math.max(Math.min(age, 2147483647L), -2147483648L);
		ObfuscationHelper.setValue(EntityItem.class, this.entity, age, 2);
	}

	@Override
	public void setItem(IItemStack item) {
		ItemStack stack = (item == null) ? ItemStack.EMPTY : item.getMCItemStack();
		this.entity.setItem(stack);
	}

	@Override
	public void setLifeSpawn(int age) {
		this.entity.lifespan = age;
	}

	@Override
	public void setOwner(String name) {
		this.entity.setOwner(name);
	}

	@Override
	public void setPickupDelay(int delay) {
		this.entity.setPickupDelay(delay);
	}
}
