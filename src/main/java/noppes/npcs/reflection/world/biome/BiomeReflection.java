package noppes.npcs.reflection.world.biome;

import net.minecraft.world.biome.Biome;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class BiomeReflection {

    private static Field biomeName;

    @SuppressWarnings("all")
    public static String getBiomeName(Biome biome) {
        if (biome == null) { return ""; }
        if (biomeName == null) {
            Exception error = null;
            try { biomeName = Biome.class.getDeclaredField("field_76791_y"); } catch (Exception e) { error = e; }
            if (biomeName == null) {
                try {
                    biomeName = Biome.class.getDeclaredField("biomeName");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"biomeName\"", error);
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
