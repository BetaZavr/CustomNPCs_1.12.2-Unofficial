package noppes.npcs.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

public class CustomWalkNodeProcessor
extends WalkNodeProcessor {
	
	public CustomWalkNodeProcessor(EntityNPCInterface npc) {
		this.canEnterDoors = npc.ais.doorInteract == 0;
		this.canOpenDoors = npc.ais.doorInteract == 1;
	}
	
	public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
		if (maxDistance > CustomNpcs.NpcNavRange) { maxDistance = CustomNpcs.NpcNavRange; }
		int pos = super.findPathOptions(pathOptions, currentPoint, targetPoint, maxDistance);
		return pos;
	}
	
}
