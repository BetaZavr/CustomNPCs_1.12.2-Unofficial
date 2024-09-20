package noppes.npcs.mixin.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntitySenses;
import noppes.npcs.mixin.api.entity.ai.EntitySensesAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = EntitySenses.class)
public class EntitySensesMixin implements EntitySensesAPIMixin {

    @Shadow(aliases = "seenEntities")
    List<Entity> seenEntities;

    @Shadow(aliases = "unseenEntities")
    List<Entity> unseenEntities;

    @Override
    public List<Entity> npcs$getSeenEntities() { return seenEntities; }

    @Override
    public List<Entity> npcs$getUnseenEntities() { return unseenEntities; }

}