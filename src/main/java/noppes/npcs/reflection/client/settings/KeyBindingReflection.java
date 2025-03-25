package noppes.npcs.reflection.client.settings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SideOnly(Side.CLIENT)
public class KeyBindingReflection {

    private static Field keyCategory;
    private static Field keyCodeDefault;
    private static Field keyDescription;
    private static Field keyModifier;

    public static void setKeyCategory(KeyBinding keyBinding, String newKeyCategory) {
        if (keyBinding == null || newKeyCategory == null || newKeyCategory.isEmpty()) { return; }
        if (keyCategory == null) {
            Exception error = null;
            try { keyCategory = KeyBinding.class.getDeclaredField("field_151471_f"); } catch (Exception e) { error = e; }
            if (keyCategory == null) {
                try {
                    keyCategory = KeyBinding.class.getDeclaredField("keyCategory");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"keyCategory\"", error);
            }
        }
        try {
            keyCategory.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(keyCategory, keyCategory.getModifiers() & ~Modifier.FINAL);

            keyCategory.set(keyBinding, newKeyCategory);
        } catch (Exception e) {
            LogWriter.error("Error set \"keyCategory\":\"" + newKeyCategory + "\" in " + keyBinding, e);
        }
    }

    public static void setKeyCodeDefault(KeyBinding keyBinding, int newKeyCodeDefault) {
        if (keyBinding == null) { return; }
        if (newKeyCodeDefault < 0) { newKeyCodeDefault *= -1; }
        if (newKeyCodeDefault > 400) { newKeyCodeDefault = 400; }
        if (keyCodeDefault == null) {
            Exception error = null;
            try { keyCodeDefault = KeyBinding.class.getDeclaredField("field_151472_e"); } catch (Exception e) { error = e; }
            if (keyCodeDefault == null) {
                try {
                    keyCodeDefault = KeyBinding.class.getDeclaredField("keyCodeDefault");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"keyCodeDefault\"", error);
            }
        }
        try {
            keyCodeDefault.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(keyCodeDefault, keyCodeDefault.getModifiers() & ~Modifier.FINAL);

            keyCodeDefault.set(keyBinding, newKeyCodeDefault);
        } catch (Exception e) {
            LogWriter.error("Error set \"keyCodeDefault\":\"" + newKeyCodeDefault + "\" in " + keyBinding, e);
        }
    }

    public static void setKeyDescription(KeyBinding keyBinding, String newKeyDescription) {
        if (keyBinding == null || newKeyDescription == null || newKeyDescription.isEmpty()) { return; }
        if (keyDescription == null) {
            Exception error = null;
            try { keyDescription = KeyBinding.class.getDeclaredField("field_74515_c"); } catch (Exception e) { error = e; }
            if (keyDescription == null) {
                try {
                    keyDescription = KeyBinding.class.getDeclaredField("keyDescription");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"keyDescription\"", error);
            }
        }
        try {
            keyDescription.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(keyDescription, keyDescription.getModifiers() & ~Modifier.FINAL);

            keyDescription.set(keyBinding, newKeyDescription);
        } catch (Exception e) {
            LogWriter.error("Error set \"keyDescription\":\"" + newKeyDescription + "\" in " + keyBinding, e);
        }
    }

    public static void setModifier(KeyBinding keyBinding, KeyModifier newModifer) {
        if (keyBinding == null || newModifer == null) { return; }
        if (keyModifier == null) {
            try { keyModifier = KeyBinding.class.getDeclaredField("keyModifier"); }
            catch (Exception error) {
                LogWriter.error("Error found field \"keyModifier\"", error);
            }
        }
        try {
            keyModifier.setAccessible(true);
            keyModifier.set(keyBinding, newModifer);
        } catch (Exception e) {
            LogWriter.error("Error set \"keyModifier\":\"" + newModifer + "\" in " + keyBinding, e);
        }
    }

}
