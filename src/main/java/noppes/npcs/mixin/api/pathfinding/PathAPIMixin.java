package noppes.npcs.mixin.api.pathfinding;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Path.class)
public interface PathAPIMixin {

    @Accessor(value="points")
    PathPoint[] npcs$getPoints();

    @Accessor(value="openSet")
    PathPoint[] npcs$getOpenSet();

    @Accessor(value="closedSet")
    PathPoint[] npcs$getClosedSet();

    @Accessor(value="currentPathIndex")
    int npcs$getCurrentPathIndex();

    @Mutable
    @Accessor(value="openSet")
    void npcs$setOpenSet(PathPoint[] newOpenSet);

    @Mutable
    @Accessor(value="closedSet")
    void npcs$setClosedSet(PathPoint[] newClosedSet);

    @Mutable
    @Accessor(value="currentPathIndex")
    void npcs$setCurrentPathIndex(int newCurrentPathIndex);

}
