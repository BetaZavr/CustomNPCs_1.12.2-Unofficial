package noppes.npcs.reflection.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class RenderPlayerReflection {

    private static Field smallArms;

    @SuppressWarnings("all")
    public static boolean getSmallArms(RenderPlayer render) {
        if (render == null) { return false; }
        if (smallArms == null) {
            Exception error = null;
            try { smallArms = RenderPlayer.class.getDeclaredField("field_177140_a"); } catch (Exception e) { error = e; }
            if (smallArms == null) {
                try {
                    smallArms = RenderPlayer.class.getDeclaredField("smallArms");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"smallArms\"", error);
                return false;
            }
        }
        try {
            smallArms.setAccessible(true);
            return (boolean) smallArms.get(render);
        } catch (Exception e) {
            LogWriter.error("Error get \"smallArms\" in " + render, e);
        }
        return false;
    }

}
