package noppes.npcs.mixin.api.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntitySenses;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = EntitySenses.class)
public interface EntitySensesAPIMixin {

    @Accessor(value="seenEntities")
    List<Entity> npcs$getSeenEntities();

    @Accessor(value="unseenEntities")
    List<Entity> npcs$getUnseenEntities();

}
