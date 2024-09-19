package noppes.npcs.api.wrapper;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.mixin.api.entity.item.EntityItemAPIMixin;

import java.util.Objects;

@SuppressWarnings("rawtypes")
public class EntityItemWrapper<T extends EntityItem> extends EntityWrapper<T> implements IEntityItem {

	public EntityItemWrapper(T entity) {
		super(entity);
	}

	@Override
	public long getAge() {
		return ((EntityItemAPIMixin) this.entity).npcs$getAge(); // parent getAge() is only Client
	}

	@Override
	public IItemStack getItem() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(this.entity.getItem());
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
		return this.entity.pickupDelay;
	}

	@Override
	public int getType() {
		return EntityType.ITEM.get();
	}

	@Override
	public void setAge(long age) {
		((EntityItemAPIMixin) this.entity).npcs$setAge((int) Math.max(Math.min(age, 2147483647L), 0));
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

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.ITEM.get() || super.typeOf(type);
	}

}
