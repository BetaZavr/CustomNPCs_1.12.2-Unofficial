package noppes.npcs.reflection.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SideOnly(Side.CLIENT)
public class TileEntityItemStackRendererReflection {

    private static Field banner;

    public static void setBanner(TileEntityItemStackRenderer tileEntityItemStackRenderer, TileEntityBanner newTileEntityBanner) {
        if (tileEntityItemStackRenderer == null || newTileEntityBanner == null) { return; }
        if (banner == null) {
            Exception error = null;
            try { banner = TileEntityItemStackRenderer.class.getDeclaredField("field_179024_e"); } catch (Exception e) { error = e; }
            if (banner == null) {
                try {
                    banner = TileEntityItemStackRenderer.class.getDeclaredField("banner");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"banner\"", error);
            }
        }
        try {
            banner.setAccessible(true);

            if (Modifier.isFinal(banner.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(banner, banner.getModifiers() & ~Modifier.FINAL);
            }

            banner.set(tileEntityItemStackRenderer, newTileEntityBanner);
        } catch (Exception e) {
            LogWriter.error("Error set \"banner\":\"" + newTileEntityBanner + "\" in " + tileEntityItemStackRenderer, e);
        }
    }
}
