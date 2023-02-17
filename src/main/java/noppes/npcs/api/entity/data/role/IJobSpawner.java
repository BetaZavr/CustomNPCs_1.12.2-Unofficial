package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.IEntityLivingBase;

public interface IJobSpawner {
	
	void removeAllSpawned();

	IEntityLivingBase<?> spawnEntity(int pos, boolean isDead);
	
}
