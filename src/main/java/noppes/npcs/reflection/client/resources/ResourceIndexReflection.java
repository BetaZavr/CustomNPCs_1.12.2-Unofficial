package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.ResourceIndex;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ResourceIndexReflection {

    private static Field resourceMap;

    @SuppressWarnings("all")
    public static Map<String, File> getResourceMap(ResourceIndex resourceIndex) {
        if (resourceIndex == null) { return new HashMap<>(); }
        if (resourceMap == null) {
            Exception error = null;
            try { resourceMap = ResourceIndex.class.getDeclaredField("field_152784_b"); } catch (Exception e) { error = e; }
            if (resourceMap == null) {
                try {
                    resourceMap = ResourceIndex.class.getDeclaredField("resourceMap");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"resourceMap\"", error);
                return new HashMap<>();
            }
        }
        try {
            resourceMap.setAccessible(true);
            return (Map<String, File>) resourceMap.get(resourceIndex);
        } catch (Exception e) {
            LogWriter.error("Error get \"resourceMap\" in " + resourceIndex, e);
        }
        return new HashMap<>();
    }

}
