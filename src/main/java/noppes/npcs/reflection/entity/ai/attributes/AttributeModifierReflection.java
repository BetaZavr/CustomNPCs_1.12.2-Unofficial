package noppes.npcs.reflection.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import noppes.npcs.LogWriter;
import noppes.npcs.util.ValueUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AttributeModifierReflection {

    private static Field amount;
    private static Field operation;
    private static Field name;

    public static void setOperation(AttributeModifier modifer, int newOperation) {
        if (modifer == null) { return; }
        if (operation == null) {
            Exception error = null;
            try { operation = AttributeModifier.class.getDeclaredField("field_111172_b"); } catch (Exception e) { error = e; }
            if (operation == null) {
                try {
                    operation = AttributeModifier.class.getDeclaredField("operation");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"operation\"", error);
            }
        }
        try {
            operation.setAccessible(true);

            if (Modifier.isFinal(operation.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(operation, operation.getModifiers() & ~Modifier.FINAL);
            }

            operation.set(modifer, ValueUtil.correctInt(newOperation, 0, 2));
        } catch (Exception e) {
            LogWriter.error("Error set \"operation\":\"" + newOperation + "\" in " + modifer, e);
        }
    }

    public static void setName(AttributeModifier modifer, String newName) {
        if (modifer == null || newName == null) { return; }
        if (name == null) {
            Exception error = null;
            try { name = AttributeModifier.class.getDeclaredField("field_111173_c"); } catch (Exception e) { error = e; }
            if (name == null) {
                try {
                    name = AttributeModifier.class.getDeclaredField("name");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"name\"", error);
            }
        }
        try {
            name.setAccessible(true);

            if (Modifier.isFinal(name.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(name, name.getModifiers() & ~Modifier.FINAL);
            }

            name.set(modifer, newName);
        } catch (Exception e) {
            LogWriter.error("Error set \"name\":\"" + newName + "\" in " + modifer, e);
        }
    }

    public static void setAmount(AttributeModifier modifer, double newAmount) {
        if (modifer == null) { return; }
        if (amount == null) {
            Exception error = null;
            try { amount = AttributeModifier.class.getDeclaredField("field_111174_a"); } catch (Exception e) { error = e; }
            if (amount == null) {
                try {
                    amount = AttributeModifier.class.getDeclaredField("amount");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"amount\"", error);
            }
        }
        try {
            amount.setAccessible(true);

            if (Modifier.isFinal(amount.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(amount, amount.getModifiers() & ~Modifier.FINAL);
            }

            amount.set(modifer, newAmount);
        } catch (Exception e) {
            LogWriter.error("Error set \"amount\":\"" + newAmount + "\" in " + modifer, e);
        }
    }

}
