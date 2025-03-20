package noppes.npcs.reflection.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntitySenses;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class EntitySensesReflection {

    private static Field seenEntities;
    private static Field unseenEntities;

    @SuppressWarnings("unchecked")
    public static List<Entity> getSeenEntities(EntitySenses senses) {
        if (senses == null) { return Collections.emptyList(); }
        if (seenEntities == null) {
            Exception error = null;
            try { seenEntities = EntitySenses.class.getDeclaredField("field_75524_b"); } catch (Exception e) { error = e; }
            if (seenEntities == null) {
                try {
                    seenEntities = EntitySenses.class.getDeclaredField("seenEntities");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"seenEntities\"", error);
                return Collections.emptyList();
            }
        }
        try {
            seenEntities.setAccessible(true);
            return (List<Entity>) seenEntities.get(senses);
        } catch (Exception e) {
            LogWriter.error("Error get \"seenEntities\" in " + senses, e);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> getUnseenEntities(EntitySenses senses) {
        if (senses == null) { return Collections.emptyList(); }
        if (unseenEntities == null) {
            Exception error = null;
            try { unseenEntities = EntitySenses.class.getDeclaredField("field_75525_c"); } catch (Exception e) { error = e; }
            if (unseenEntities == null) {
                try {
                    unseenEntities = EntitySenses.class.getDeclaredField("unseenEntities");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"unseenEntities\"", error);
                return Collections.emptyList();
            }
        }
        try {
            unseenEntities.setAccessible(true);
            return (List<Entity>) unseenEntities.get(senses);
        } catch (Exception e) {
            LogWriter.error("Error get \"unseenEntities\" in " + senses, e);
        }
        return Collections.emptyList();
    }

}
