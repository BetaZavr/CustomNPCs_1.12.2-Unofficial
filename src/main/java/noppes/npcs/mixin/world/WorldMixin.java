package noppes.npcs.mixin.world;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.world.World;
import noppes.npcs.mixin.api.world.WorldAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = World.class)
public class WorldMixin implements WorldAPIMixin {

    @Shadow(aliases = "pathListener")
    private PathWorldListener pathListener;

    @Final
    @Shadow(aliases = "unloadedEntityList")
    protected List<Entity> unloadedEntityList;

    @Override
    public PathWorldListener npcs$getPathListener() { return pathListener; }

    @Override
    public List<Entity> npcs$getUnloadedEntityList() { return unloadedEntityList; }

}
