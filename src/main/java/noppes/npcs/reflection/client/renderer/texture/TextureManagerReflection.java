package noppes.npcs.reflection.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class TextureManagerReflection {

    private static Field mapTextureObjects;

    @SuppressWarnings("unchecked")
    public static Map<ResourceLocation, ITextureObject> getMapTextureObjects(TextureManager textureManager) {
        if (textureManager == null) { return new HashMap<>(); }
        if (mapTextureObjects == null) {
            Exception error = null;
            try { mapTextureObjects = TextureManager.class.getDeclaredField("field_110585_a"); } catch (Exception e) { error = e; }
            if (mapTextureObjects == null) {
                try {
                    mapTextureObjects = TextureManager.class.getDeclaredField("mapTextureObjects");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"mapTextureObjects\"", error);
                return new HashMap<>();
            }
        }
        try {
            mapTextureObjects.setAccessible(true);
            return (Map<ResourceLocation, ITextureObject>) mapTextureObjects.get(textureManager);
        } catch (Exception e) {
            LogWriter.error("Error get \"mapTextureObjects\" in " + textureManager, e);
        }
        return new HashMap<>();
    }

}
