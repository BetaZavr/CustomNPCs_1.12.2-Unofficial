package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.io.File;
import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class AbstractResourcePackReflection {

    private static Field resourcePackFile;

    public static File getResourcePackFile(AbstractResourcePack resourcePack) {
        if (resourcePack == null) { return null; }
        if (resourcePackFile == null) {
            Exception error = null;
            try { resourcePackFile = AbstractResourcePack.class.getDeclaredField("field_110597_b"); } catch (Exception e) { error = e; }
            if (resourcePackFile == null) {
                try {
                    resourcePackFile = AbstractResourcePack.class.getDeclaredField("resourcePackFile");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"resourcePackFile\"", error);
                return null;
            }
        }
        try {
            resourcePackFile.setAccessible(true);
            return (File) resourcePackFile.get(resourcePack);
        } catch (Exception e) {
            LogWriter.error("Error get \"resourcePackFile\" in " + resourcePack, e);
        }
        return null;
    }

}
