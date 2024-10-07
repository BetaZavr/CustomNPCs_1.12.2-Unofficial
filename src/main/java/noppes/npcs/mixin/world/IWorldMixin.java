package noppes.npcs.mixin.world;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathWorldListener;

import java.util.List;

public interface IWorldMixin {

    PathWorldListener npcs$getPathListener();

    List<Entity> npcs$getUnloadedEntityList();

}
