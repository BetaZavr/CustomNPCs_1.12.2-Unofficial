package noppes.npcs.api.wrapper;

import net.minecraft.entity.projectile.EntityArrow;
import noppes.npcs.api.entity.IArrow;

@SuppressWarnings("rawtypes")
public class ArrowWrapper<T extends EntityArrow> extends EntityWrapper<T> implements IArrow {
	public ArrowWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return 4;
	}

	@Override
	public boolean typeOf(int type) {
		return type == 4 || super.typeOf(type);
	}
}
