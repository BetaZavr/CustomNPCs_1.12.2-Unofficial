package noppes.npcs.mixin.api.entity;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.util.IntHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityTracker.class)
public interface EntityTrackerAPIMixin {

    @Accessor(value="trackedEntityHashTable")
    IntHashMap<EntityTrackerEntry> npcs$getTrackedEntityHashTable();

}
