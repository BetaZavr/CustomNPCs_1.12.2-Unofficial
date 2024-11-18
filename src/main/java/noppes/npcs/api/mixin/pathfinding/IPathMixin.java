package noppes.npcs.api.mixin.pathfinding;

import net.minecraft.pathfinding.PathPoint;

public interface IPathMixin {

    PathPoint[] npcs$getPoints();

    PathPoint[] npcs$getOpenSet();

    PathPoint[] npcs$getClosedSet();

    int npcs$getCurrentPathIndex();

    void npcs$setOpenSet(PathPoint[] newOpenSet);

    void npcs$setClosedSet(PathPoint[] newClosedSet);

    void npcs$setCurrentPathIndex(int newCurrentPathIndex);

}
