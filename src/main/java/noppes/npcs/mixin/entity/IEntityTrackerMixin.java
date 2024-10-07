package noppes.npcs.mixin.entity;

import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.util.IntHashMap;

public interface IEntityTrackerMixin {

    IntHashMap<EntityTrackerEntry> npcs$getTrackedEntityHashTable();

}
