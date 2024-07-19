package noppes.npcs.api.wrapper;

import net.minecraft.entity.projectile.EntityThrowable;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IThrowable;

@SuppressWarnings("rawtypes")
public class ThrowableWrapper<T extends EntityThrowable> extends EntityWrapper<T> implements IThrowable {

	public ThrowableWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return EntityType.THROWABLE.get();
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.THROWABLE.get() || super.typeOf(type);
	}

}
