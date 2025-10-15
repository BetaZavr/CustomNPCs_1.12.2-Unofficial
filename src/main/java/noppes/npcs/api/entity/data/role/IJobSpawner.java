package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IEntityLivingBase;

@SuppressWarnings("all")
public interface IJobSpawner {

	void removeAllSpawned();

	IEntityLivingBase<?> spawnEntity(@ParamName("pos") int pos, @ParamName("isDead") boolean isDead);

}
