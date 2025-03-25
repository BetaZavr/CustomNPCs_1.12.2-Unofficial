package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.Locale;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class LocaleReflection {

    private static Field properties;

    @SuppressWarnings("unchecked")
    public static Map<String, String> getProperties(Locale locale) {
        if (locale == null) { return new HashMap<>(); }
        if (properties == null) {
            Exception error = null;
            try { properties = Locale.class.getDeclaredField("field_135032_a"); } catch (Exception e) { error = e; }
            if (properties == null) {
                try {
                    properties = Locale.class.getDeclaredField("properties");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"properties\"", error);
                return new HashMap<>();
            }
        }
        try {
            properties.setAccessible(true);
            return (Map<String, String>) properties.get(locale);
        } catch (Exception e) {
            LogWriter.error("Error get \"properties\" in " + locale, e);
        }
        return new HashMap<>();
    }

}
