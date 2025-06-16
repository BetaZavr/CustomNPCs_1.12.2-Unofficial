package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class DefaultResourcePackReflection {

    private static Field resourceIndex;

    @SuppressWarnings("all")
    public static ResourceIndex getResourceIndex(DefaultResourcePack resourcePack) {
        if (resourcePack == null) { return null; }
        if (resourceIndex == null) {
            Exception error = null;
            try { resourceIndex = DefaultResourcePack.class.getDeclaredField("field_188549_b"); } catch (Exception e) { error = e; }
            if (resourceIndex == null) {
                try {
                    resourceIndex = DefaultResourcePack.class.getDeclaredField("resourceIndex");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"resourceIndex\"", error);
                return null;
            }
        }
        try {
            resourceIndex.setAccessible(true);
            return (ResourceIndex) resourceIndex.get(resourcePack);
        } catch (Exception e) {
            LogWriter.error("Error get \"resourceIndex\" in " + resourcePack, e);
        }
        return null;
    }

}
