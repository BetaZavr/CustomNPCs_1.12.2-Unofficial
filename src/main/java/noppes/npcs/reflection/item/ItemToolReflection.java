package noppes.npcs.reflection.item;

import net.minecraft.item.ItemTool;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class ItemToolReflection {

    private static Field toolClass;

    public static void setToolClass(ItemTool item, String newToolClass) {
        if (item == null || newToolClass == null || newToolClass.isEmpty()) { return; }
        if (toolClass == null) {
            try { toolClass = ItemTool.class.getDeclaredField("toolClass"); }
            catch (Exception error) {
                LogWriter.error("Error found field \"toolClass\"", error);
                return;
            }
        }
        try {
            toolClass.setAccessible(true);
            toolClass.set(item, newToolClass);
        } catch (Exception e) {
            LogWriter.error("Error set \"toolClass\":\"" + newToolClass + "\" to " + item, e);
        }
    }

}
