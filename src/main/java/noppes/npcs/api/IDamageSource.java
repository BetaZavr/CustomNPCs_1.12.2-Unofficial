package noppes.npcs.api;

import net.minecraft.util.DamageSource;
import noppes.npcs.api.entity.IEntity;

public interface IDamageSource {
	
	IEntity<?> getImmediateSource();

	DamageSource getMCDamageSource();

	IEntity<?> getTrueSource();

	String getType();

	boolean isProjectile();

	boolean isUnblockable();
	
}
