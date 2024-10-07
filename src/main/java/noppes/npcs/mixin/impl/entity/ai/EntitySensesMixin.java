package noppes.npcs.mixin.impl.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntitySenses;
import noppes.npcs.mixin.entity.ai.IEntitySensesMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = EntitySenses.class)
public class EntitySensesMixin implements IEntitySensesMixin {

    @Shadow(aliases = "seenEntities")
    List<Entity> seenEntities;

    @Shadow(aliases = "unseenEntities")
    List<Entity> unseenEntities;

    @Override
    public List<Entity> npcs$getSeenEntities() { return seenEntities; }

    @Override
    public List<Entity> npcs$getUnseenEntities() { return unseenEntities; }

}
