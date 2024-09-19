package noppes.npcs.mixin.pathfinding;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import noppes.npcs.mixin.api.pathfinding.PathAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Path.class)
public class PathMixin implements PathAPIMixin {

    @Final
    @Shadow(aliases = "points")
    private PathPoint[] points;

    @Shadow(aliases = "openSet")
    private PathPoint[] openSet;

    @Shadow(aliases = "closedSet")
    private PathPoint[] closedSet;

    @Shadow(aliases = "currentPathIndex")
    private int currentPathIndex;

    @Override
    public PathPoint[] npcs$getPoints() { return points; }

    @Override
    public PathPoint[] npcs$getOpenSet() { return openSet; }

    @Override
    public PathPoint[] npcs$getClosedSet() { return closedSet; }

    @Override
    public int npcs$getCurrentPathIndex() { return currentPathIndex; }

    @Override
    public void npcs$setOpenSet(PathPoint[] newOpenSet) {
        if (newOpenSet == null) { return; }
        openSet = newOpenSet;
    }

    @Override
    public void npcs$setClosedSet(PathPoint[] newClosedSet) {
        if (newClosedSet == null) { return; }
        openSet = newClosedSet;
    }

    @Override
    public void npcs$setCurrentPathIndex(int newCurrentPathIndex) {
        if (newCurrentPathIndex < 0) { newCurrentPathIndex = 0; }
        currentPathIndex = newCurrentPathIndex;
    }

}
