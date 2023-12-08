package noppes.npcs.entity.pathfinding;

import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.PathPoint;
import noppes.npcs.entity.EntityNPCInterface;

public class CustomFlyingNodeProcessor
extends FlyingNodeProcessor {

	public CustomFlyingNodeProcessor(EntityNPCInterface npc) {
		this.canEnterDoors = npc.ais.doorInteract == 0;
		this.canOpenDoors = npc.ais.doorInteract == 1;
	}
	
	public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
		return super.findPathOptions(pathOptions, currentPoint, targetPoint, maxDistance);
	}
	
}
