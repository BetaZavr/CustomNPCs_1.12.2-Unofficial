package noppes.npcs.reflection.item;

import net.minecraft.item.ItemFood;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ItemFoodReflection {

    private static Field itemUseDuration;

    @SuppressWarnings("all")
    public static void setItemUseDuration(ItemFood item, int newItemUseDuration) {
        if (item == null) { return; }
        if (newItemUseDuration < 0) { newItemUseDuration *= -1; }
        if (newItemUseDuration < 1) { newItemUseDuration = 1; }
        if (itemUseDuration == null) {
            Exception error = null;
            try { itemUseDuration = ItemFood.class.getDeclaredField("field_77855_a"); } catch (Exception e) { error = e; }
            if (itemUseDuration == null) {
                try {
                    itemUseDuration = ItemFood.class.getDeclaredField("itemUseDuration");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"itemUseDuration\"", error);
                return;
            }
        }
        try {
            itemUseDuration.setAccessible(true);

            if (Modifier.isFinal(itemUseDuration.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(itemUseDuration, itemUseDuration.getModifiers() & ~Modifier.FINAL);
            }

            itemUseDuration.set(item, newItemUseDuration);
        } catch (Exception e) {
            LogWriter.error("Error set \"itemUseDuration\":\"" + newItemUseDuration + "\" to " + item, e);
        }
    }

}
