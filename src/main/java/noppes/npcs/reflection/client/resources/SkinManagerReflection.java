package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.SkinManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.io.File;
import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class SkinManagerReflection {

    private static Field skinCacheDir;

    @SuppressWarnings("all")
    public static File getDir(SkinManager parent) {
        if (parent == null) { return new File("."); }
        if (skinCacheDir == null) {
            Exception error = null;
            try { skinCacheDir = SkinManager.class.getDeclaredField("field_152796_d"); } catch (Exception e) { error = e; }
            if (skinCacheDir == null) {
                try {
                    skinCacheDir = SkinManager.class.getDeclaredField("skinCacheDir");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"skinsDirectory\"", error);
                return new File(".");
            }
        }
        try {
            skinCacheDir.setAccessible(true);
            return (File) skinCacheDir.get(parent);
        }
        catch (Exception e) {
            LogWriter.error("Error get \"skinsDirectory\": \"" + skinCacheDir + "\" from " + parent, e);
        }
        return new File(".");
    }

}
