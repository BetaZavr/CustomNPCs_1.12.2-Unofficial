package noppes.npcs.reflection.item;


import net.minecraft.item.ItemArmor;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ItemArmorReflection {

    private static Field damageReduceAmount;
    private static Field toughness;

    public static void setDamageReduceAmount(ItemArmor item, int newDamageReduceAmount) {
        if (item == null) { return; }
        if (newDamageReduceAmount < 0) { newDamageReduceAmount *= -1; }
        if (newDamageReduceAmount <1) { newDamageReduceAmount = 1; }
        if (damageReduceAmount == null) {
            Exception error = null;
            try { damageReduceAmount = ItemArmor.class.getDeclaredField("field_77879_b"); } catch (Exception e) { error = e; }
            if (damageReduceAmount == null) {
                try {
                    damageReduceAmount = ItemArmor.class.getDeclaredField("damageReduceAmount");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"damageReduceAmount\"", error);
                return;
            }
        }
        try {
            damageReduceAmount.setAccessible(true);

            if (Modifier.isFinal(damageReduceAmount.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(damageReduceAmount, damageReduceAmount.getModifiers() & ~Modifier.FINAL);
            }

            damageReduceAmount.set(item, newDamageReduceAmount);
        } catch (Exception e) {
            LogWriter.error("Error set \"damageReduceAmount\":\"" + newDamageReduceAmount + "\" to " + item, e);
        }
    }

    public static void setToughness(ItemArmor item, float newToughness) {
        if (item == null) { return; }
        if (toughness == null) {
            Exception error = null;
            try { toughness = ItemArmor.class.getDeclaredField("field_189415_e"); } catch (Exception e) { error = e; }
            if (toughness == null) {
                try {
                    toughness = ItemArmor.class.getDeclaredField("toughness");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"toughness\"", error);
                return;
            }
        }
        try {
            toughness.setAccessible(true);
            toughness.set(item, newToughness);
        } catch (Exception e) {
            LogWriter.error("Error set \"toughness\":\"" + newToughness + "\" to " + item, e);
        }
    }

}
