package noppes.npcs.api.wrapper;

import net.minecraft.entity.projectile.EntityArrow;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IArrow;

@SuppressWarnings("rawtypes")
public class ArrowWrapper<T extends EntityArrow> extends EntityWrapper<T> implements IArrow {

	public ArrowWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return EntityType.ARROW.get();
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.ARROW.get() || super.typeOf(type);
	}
}
