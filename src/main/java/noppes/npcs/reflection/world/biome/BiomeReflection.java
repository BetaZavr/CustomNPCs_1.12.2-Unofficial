package noppes.npcs.reflection.world.biome;

import net.minecraft.world.biome.Biome;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class BiomeReflection {

    private static Field biomeName;

    public static String getBiomeName(Biome biome) {
        if (biome == null) { return ""; }
        if (biomeName == null) {
            try {
                try { biomeName = Biome.class.getDeclaredField("field_76791_y"); }
                catch (Exception e) { biomeName = Biome.class.getDeclaredField("biomeName"); }
            } catch (Exception e) {
                LogWriter.error("Error found field \"biomeName\" in ", e);
                return "";
            }
        }
        try {
            biomeName.setAccessible(true);
            return (String) biomeName.get(biome);
        } catch (Exception e) {
            LogWriter.error("Error get \"biomeName\" in " + biome, e);
        }
        return "";
    }

}
