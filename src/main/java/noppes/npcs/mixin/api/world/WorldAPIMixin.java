package noppes.npcs.mixin.api.world;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = World.class)
public interface WorldAPIMixin {

    @Accessor(value="pathListener")
    PathWorldListener npcs$getPathListener();

    @Accessor(value="unloadedEntityList")
    List<Entity> npcs$getUnloadedEntityList();

}
