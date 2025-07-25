package noppes.npcs.ai.movement;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigateGround;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIWaterNav extends EntityAIBase {

	private final EntityNPCInterface entity;

	public EntityAIWaterNav(EntityNPCInterface iNpc) {
		this.entity = iNpc;
		((PathNavigateGround) iNpc.getNavigator()).setCanSwim(true);
	}

	public boolean shouldExecute() {
		return (this.entity.isInWater() || this.entity.isInLava())
				&& (this.entity.ais.canSwim || this.entity.collidedHorizontally);
	}

	public void updateTask() {
		CustomNpcs.debugData.start(entity);
		if (this.entity.getRNG().nextFloat() < 0.8f) {
			this.entity.getJumpHelper().setJumping();
		}
		CustomNpcs.debugData.end(entity);
	}
}
