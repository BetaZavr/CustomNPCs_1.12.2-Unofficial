package noppes.npcs.reflection.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.RangedAttribute;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class RangedAttributeReflection {

    private static Field minimumValue;
    private static Field maximumValue;

    public static double getMinValue(RangedAttribute attribute) {
        if (attribute == null) { return 0.0d; }
        try {
            Field field = getMinimumValueField();
            field.setAccessible(true);
            return (double) field.get(attribute);
        } catch (Exception e) {
            LogWriter.error("Error get \"minimumValue\":\" in " + attribute, e);
        }
        return 0.0d;
    }

    public static double getMaxValue(RangedAttribute attribute) {
        if (attribute == null) { return 0.0d; }
        try {
            Field field = getMaximumValueField();
            field.setAccessible(true);
            return (double) field.get(attribute);
        } catch (Exception e) {
            LogWriter.error("Error get \"maximumValue\":\" in " + attribute, e);
        }
        return 0.0d;
    }

    public static void setMinValue(RangedAttribute attribute, double newMinValue) {
        if (attribute == null) { return; }
        try {
            Field field = getMinimumValueField();
            field.setAccessible(true);
            field.set(attribute, newMinValue);
        } catch (Exception e) {
            LogWriter.error("Error get \"minimumValue\" in " + attribute, e);
        }
    }

    public static void setMaxValue(RangedAttribute attribute, double newMaxValue) {
        if (attribute == null) { return; }
        try {
            Field field = getMaximumValueField();
            field.setAccessible(true);
            field.set(attribute, newMaxValue);
        } catch (Exception e) {
            LogWriter.error("Error get \"maximumValue\" in " + attribute, e);
        }
    }

    private static Field getMinimumValueField() {
        if (minimumValue == null) {
            Exception error = null;
            try { minimumValue = RangedAttribute.class.getDeclaredField("field_111120_a"); } catch (Exception e) { error = e; }
            if (minimumValue == null) {
                try {
                    minimumValue = RangedAttribute.class.getDeclaredField("minimumValue");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"points\"", error);
            }
        }
        return minimumValue;
    }

    private static Field getMaximumValueField() {
        if (maximumValue == null) {
            Exception error = null;
            try { maximumValue = RangedAttribute.class.getDeclaredField("field_111118_b"); } catch (Exception e) { error = e; }
            if (maximumValue == null) {
                try {
                    maximumValue = RangedAttribute.class.getDeclaredField("maximumValue");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"points\"", error);
            }
        }
        return maximumValue;
    }

}
