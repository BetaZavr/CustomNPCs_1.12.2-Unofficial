package noppes.npcs.reflection.entity.passive;

import net.minecraft.entity.passive.EntityVillager;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class EntityVillagerReflection {

    private static Field careerId;

    @SuppressWarnings("all")
    public static int getCareerID(EntityVillager villager) {
        if (villager == null) { return 0; }
        if (careerId == null) {
            Exception error = null;
            try { careerId = EntityVillager.class.getDeclaredField("field_175563_bv"); } catch (Exception e) { error = e; }
            if (careerId == null) {
                try {
                    careerId = EntityVillager.class.getDeclaredField("careerId");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"careerId\"", error);
                return 0;
            }
        }
        try {
            careerId.setAccessible(true);
            return (int) careerId.get(villager);
        } catch (Exception e) {
            LogWriter.error("Error get \"careerId\" in " + villager, e);
        }
        return 0;
    }

}
