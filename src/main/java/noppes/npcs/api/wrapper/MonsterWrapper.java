package noppes.npcs.api.wrapper;

import net.minecraft.entity.monster.EntityMob;
import noppes.npcs.api.entity.IMonster;

@SuppressWarnings("rawtypes")
public class MonsterWrapper<T extends EntityMob>
extends EntityLivingWrapper<T>
implements IMonster {
	
	public MonsterWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return 3;
	}

	@Override
	public boolean typeOf(int type) {
		return type == 3 || super.typeOf(type);
	}
}
