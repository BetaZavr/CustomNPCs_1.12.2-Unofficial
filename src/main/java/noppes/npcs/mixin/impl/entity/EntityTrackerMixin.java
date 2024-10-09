package noppes.npcs.mixin.impl.entity;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.util.IntHashMap;
import noppes.npcs.mixin.entity.IEntityTrackerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityTracker.class)
public class EntityTrackerMixin implements IEntityTrackerMixin {

    @Final
    @Shadow
    private IntHashMap<EntityTrackerEntry> trackedEntityHashTable;

    @Override
    public IntHashMap<EntityTrackerEntry> npcs$getTrackedEntityHashTable() { return trackedEntityHashTable; }

}
