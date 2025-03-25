package noppes.npcs.reflection.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class TextureMapReflection {

    private static Field mapRegisteredSprites;

    @SuppressWarnings("unchecked")
    public static Map<String, TextureAtlasSprite> getMapRegisteredSprites(TextureMap textureMapBlocks) {
        if (textureMapBlocks == null) { return new HashMap<>(); }
        if (mapRegisteredSprites == null) {
            Exception error = null;
            try { mapRegisteredSprites = TextureMap.class.getDeclaredField("field_110574_e"); } catch (Exception e) { error = e; }
            if (mapRegisteredSprites == null) {
                try {
                    mapRegisteredSprites = TextureMap.class.getDeclaredField("mapRegisteredSprites");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"mapRegisteredSprites\"", error);
                return new HashMap<>();
            }
        }
        try {
            mapRegisteredSprites.setAccessible(true);
            return (Map<String, TextureAtlasSprite>) mapRegisteredSprites.get(textureMapBlocks);
        } catch (Exception e) {
            LogWriter.error("Error get \"mapRegisteredSprites\" in " + textureMapBlocks, e);
        }
        return new HashMap<>();
    }

}
