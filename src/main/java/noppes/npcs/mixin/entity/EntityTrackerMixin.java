package noppes.npcs.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.mixin.entity.IEntityTrackerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = EntityTracker.class)
public class EntityTrackerMixin implements IEntityTrackerMixin {

    @Final
    @Shadow
    private WorldServer world;

    @Final
    @Shadow
    private Set<EntityTrackerEntry> entries;

    @Final
    @Shadow
    private IntHashMap<EntityTrackerEntry> trackedEntityHashTable;

    /**
     * @author BetaZavr
     * @reason if entries is changed
     */
    @Overwrite
    public void tick() {
        try {
            List<EntityPlayerMP> list = new ArrayList<>();
            Set<EntityTrackerEntry> checked = new HashSet<>(entries);
            for (EntityTrackerEntry entitytrackerentry : checked) {
                entitytrackerentry.updatePlayerList(world.playerEntities);
                if (entitytrackerentry.playerEntitiesUpdated) {
                    Entity entity = entitytrackerentry.getTrackedEntity();
                    if (entity instanceof EntityPlayerMP) { list.add((EntityPlayerMP)entity); }
                }
            }
            for (EntityPlayerMP entityplayermp : list) {
                for (EntityTrackerEntry entitytrackerentry : checked) {
                    if (entitytrackerentry.getTrackedEntity() != entityplayermp) {
                        entitytrackerentry.updatePlayerEntity(entityplayermp);
                    }
                }
            }
        }
        catch (Exception e) { LogWriter.error(e); }
    }

    @Override
    public IntHashMap<EntityTrackerEntry> npcs$getTrackedEntityHashTable() { return trackedEntityHashTable; }

}
