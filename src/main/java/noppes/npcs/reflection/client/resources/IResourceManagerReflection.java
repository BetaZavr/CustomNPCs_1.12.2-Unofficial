package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class IResourceManagerReflection {

    private static Field domainResourceManagers;

    @SuppressWarnings("unchecked")
    public static Map<String, FallbackResourceManager> getDomainResourceManagers(IResourceManager resourceManager) {
        if (resourceManager == null) { return new HashMap<>(); }
        if (domainResourceManagers == null) {
            Exception error = null;
            try { domainResourceManagers = resourceManager.getClass().getDeclaredField("field_110548_a"); } catch (Exception e) { error = e; }
            if (domainResourceManagers == null) {
                try {
                    domainResourceManagers = resourceManager.getClass().getDeclaredField("domainResourceManagers");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (domainResourceManagers == null) {
                try {
                    for (Field field : resourceManager.getClass().getDeclaredFields()) {
                        if (Map.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            Map<?, ?> map = (Map<?, ?>) field.get(resourceManager);
                            Object key = null;
                            Object value = null;
                            for (Object k : map.keySet()) {
                                key = k;
                                value = map.get(k);
                                break;
                            }
                            if (key instanceof String && value instanceof FallbackResourceManager) {
                                domainResourceManagers = field;
                                error = null;
                                break;
                            }
                        }
                    }
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"domainResourceManagers\"", error);
                return new HashMap<>();
            }
        }
        try {
            domainResourceManagers.setAccessible(true);
            return (Map<String, FallbackResourceManager>) domainResourceManagers.get(resourceManager);
        } catch (Exception e) {
            LogWriter.error("Error get \"domainResourceManagers\" in " + resourceManager, e);
        }
        return new HashMap<>();
    }

}
