package noppes.npcs.mixin.entity;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.util.IntHashMap;
import noppes.npcs.mixin.api.entity.EntityTrackerAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityTracker.class)
public class EntityTrackerMixin implements EntityTrackerAPIMixin {

    @Final
    @Shadow(aliases = "trackedEntityHashTable")
    private IntHashMap<EntityTrackerEntry> trackedEntityHashTable;

    @Override
    public IntHashMap<EntityTrackerEntry> npcs$getTrackedEntityHashTable() { return trackedEntityHashTable; }

}
