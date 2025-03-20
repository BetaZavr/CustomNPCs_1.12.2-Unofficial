package noppes.npcs.reflection.entity.item;

import net.minecraft.entity.item.EntityItem;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class EntityItemReflection {

    private static Field age;

    public static int getAge(EntityItem item) {
        if (item == null) { return 0; }
        try {
            Field field = getAgeField();
            field.setAccessible(true);
            return (int) field.get(item);
        } catch (Exception e) {
            LogWriter.error("Error get \"age\" in " + item, e);
        }
        return 0;
    }

    public static void setAge(EntityItem item, int newAge) {
        if (item == null) { return; }
        newAge = (int) Math.max(Math.min(newAge, 2147483647L), 0);
        try {
            Field field = getAgeField();
            field.setAccessible(true);
            field.set(item, newAge);
        } catch (Exception e) {
            LogWriter.error("Error set \"age\":\"" + newAge + "\" in " + item, e);
        }
    }

    private static Field getAgeField() {
        if (age == null) {
            Exception error = null;
            try { age = EntityItem.class.getDeclaredField("field_70292_b"); } catch (Exception e) { error = e; }
            if (age == null) {
                try {
                    age = EntityItem.class.getDeclaredField("age");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"age\"", error);
            }
        }
        return age;
    }

}
