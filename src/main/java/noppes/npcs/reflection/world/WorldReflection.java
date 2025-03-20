package noppes.npcs.reflection.world;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class WorldReflection {

    private static Field pathListener;
    private static Field unloadedEntityList;

    @SuppressWarnings("unchecked")
    public static List<Entity> getUnloadedEntityList(World world) {
        if (world == null) { return Collections.emptyList(); }
        if (unloadedEntityList == null) {
            try {
                try { unloadedEntityList = World.class.getDeclaredField("field_72997_g"); }
                catch (Exception e) { unloadedEntityList = World.class.getDeclaredField("unloadedEntityList"); }
            } catch (Exception e) {
                LogWriter.debug("Not Found field \"unloadedEntityList\" in " + world);
                return Collections.emptyList();
            }
        }
        try {
            unloadedEntityList.setAccessible(true);
            return (List<Entity>) unloadedEntityList.get(world);
        } catch (Exception e) {
            LogWriter.debug("Not get \"unloadedEntityList\" in " + world);
        }
        return Collections.emptyList();
    }

    public static PathWorldListener getPathListener(World world) {
        if (world == null) { return null; }
        if (pathListener == null) {
            Exception error = null;
            try { pathListener = World.class.getDeclaredField("field_184152_t"); } catch (Exception e) { error = e; }
            if (pathListener == null) {
                try {
                    pathListener = World.class.getDeclaredField("pathListener");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"pathListener\"", error);
                return null;
            }
        }
        try {
            pathListener.setAccessible(true);
            return (PathWorldListener) pathListener.get(world);
        } catch (Exception e) {
            LogWriter.error("Error get \"pathListener\" in " + world, e);
        }
        return null;
    }

}
