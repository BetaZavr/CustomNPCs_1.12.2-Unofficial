package noppes.npcs.reflection.item.crafting;

import net.minecraft.item.crafting.ShapelessRecipes;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ShapelessRecipesReflection {

    private static Field group;

    public static void setGroup(ShapelessRecipes recipe, String newGroupName) {
        if (recipe == null || newGroupName == null) { newGroupName = ""; }
        if (group == null) {
            Exception error = null;
            try { group = ShapelessRecipes.class.getDeclaredField("field_194138_c"); } catch (Exception e) { error = e; }
            if (group == null) {
                try {
                    group = ShapelessRecipes.class.getDeclaredField("group");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"damageReduceAmount\"", error);
                return;
            }
        }
        try {
            group.setAccessible(true);

            if (Modifier.isFinal(group.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(group, group.getModifiers() & ~Modifier.FINAL);
            }

            group.set(recipe, newGroupName);
        } catch (Exception e) {
            LogWriter.error("Error set \"damageReduceAmount\":\"" + newGroupName + "\" to " + recipe, e);
        }
    }

}
