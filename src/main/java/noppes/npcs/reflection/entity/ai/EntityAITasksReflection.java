package noppes.npcs.reflection.entity.ai;

import net.minecraft.entity.ai.EntityAITasks;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class EntityAITasksReflection {

    private static Field tickRate;

    @SuppressWarnings("all")
    public static int getTickRate(EntityAITasks tasks) {
        if (tasks == null) { return 3; }
        if (tickRate == null) {
            Exception error = null;
            try { tickRate = EntityAITasks.class.getDeclaredField("field_75779_e"); } catch (Exception e) { error = e; }
            if (tickRate == null) {
                try {
                    tickRate = EntityAITasks.class.getDeclaredField("tickRate");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"points\"", error);
                return 3;
            }
        }
        try {
            tickRate.setAccessible(true);
            return (int) tickRate.get(tasks);
        } catch (Exception e) {
            LogWriter.error("Error get \"points\" in " + tasks, e);
        }
        return 3;
    }

}
