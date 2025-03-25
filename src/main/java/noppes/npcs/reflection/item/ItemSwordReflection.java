package noppes.npcs.reflection.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ItemSwordReflection {

    private static Field attackDamage;
    private static Field material;

    public static void setAttackDamage(ItemSword item, float newAttackDamage) {
        if (item == null) { return; }
        try {
            Field field = getAttackDamageField();
            field.setAccessible(true);

            if (Modifier.isFinal(field.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }

            field.set(item, newAttackDamage);
        } catch (Exception e) {
            LogWriter.error("Error set \"attackDamage\":\"" + newAttackDamage + "\" to " + item, e);
        }
    }

    public static float getAttackDamage(ItemSword item) {
        try {
            Field field = getAttackDamageField();
            field.setAccessible(true);
            return (float) field.get(item);
        } catch (Exception e) {
            LogWriter.error("Error get \"attackDamage\" in " + item, e);
        }
        return 0.0f;
    }

    public static Item.ToolMaterial getMaterial(ItemSword item) {
        if (item == null) { return Item.ToolMaterial.IRON; }
        if (material == null) {
            Exception error = null;
            try { material = ItemSword.class.getDeclaredField("field_150933_b"); } catch (Exception e) { error = e; }
            if (material == null) {
                try {
                    material = ItemSword.class.getDeclaredField("material");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"material\"", error);
                return Item.ToolMaterial.IRON;
            }
        }
        try {
            material.setAccessible(true);
            return (Item.ToolMaterial) material.get(item);
        } catch (Exception e) {
            LogWriter.error("Error get \"material\" in " + item, e);
        }
        return Item.ToolMaterial.IRON;
    }

    private static Field getAttackDamageField() {
        if (attackDamage == null) {
            Exception error = null;
            try { attackDamage = ItemSword.class.getDeclaredField("field_150934_a"); } catch (Exception e) { error = e; }
            if (attackDamage == null) {
                try {
                    attackDamage = ItemSword.class.getDeclaredField("attackDamage");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"attackDamage\"", error);
            }
        }
        return attackDamage;
    }

}
