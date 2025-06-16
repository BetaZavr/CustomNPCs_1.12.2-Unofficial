package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class FallbackResourceManagerReflection {

    private static Field resourcePacks;

    @SuppressWarnings("all")
    public static List<IResourcePack> getResourcePacks(FallbackResourceManager manager) {
        if (manager == null) { return Collections.emptyList(); }
        if (resourcePacks == null) {
            Exception error = null;
            try { resourcePacks = FallbackResourceManager.class.getDeclaredField("field_110540_a"); } catch (Exception e) { error = e; }
            if (resourcePacks == null) {
                try {
                    resourcePacks = FallbackResourceManager.class.getDeclaredField("resourcePacks");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"resourcePacks\"", error);
                return Collections.emptyList();
            }
        }
        try {
            resourcePacks.setAccessible(true);
            return (List<IResourcePack>) resourcePacks.get(manager);
        } catch (Exception e) {
            LogWriter.error("Error get \"resourcePacks\" in " + manager, e);
        }
        return Collections.emptyList();
    }

}
