package noppes.npcs.mixin.entity.ai;

import net.minecraft.entity.Entity;

import java.util.List;

public interface IEntitySensesMixin {

    List<Entity> npcs$getSeenEntities();

    List<Entity> npcs$getUnseenEntities();

}
