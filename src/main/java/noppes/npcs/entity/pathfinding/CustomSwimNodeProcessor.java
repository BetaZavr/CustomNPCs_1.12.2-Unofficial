package noppes.npcs.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.SwimNodeProcessor;
import noppes.npcs.entity.EntityNPCInterface;

public class CustomSwimNodeProcessor
extends SwimNodeProcessor {
	
	public CustomSwimNodeProcessor(EntityNPCInterface npc) {
		this.canEnterDoors = npc.ais.doorInteract == 0;
		this.canOpenDoors = npc.ais.doorInteract == 1;
	}
	
	public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
		return super.findPathOptions(pathOptions, currentPoint, targetPoint, maxDistance);
	}
	
}
