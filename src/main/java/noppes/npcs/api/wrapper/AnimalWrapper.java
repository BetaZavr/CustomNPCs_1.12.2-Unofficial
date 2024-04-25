package noppes.npcs.api.wrapper;

import net.minecraft.entity.passive.EntityAnimal;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IAnimal;

@SuppressWarnings("rawtypes")
public class AnimalWrapper<T extends EntityAnimal> extends EntityLivingWrapper<T> implements IAnimal {

	public AnimalWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return EntityType.ANIMAL.get();
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.ANIMAL.get() || super.typeOf(type);
	}
}
