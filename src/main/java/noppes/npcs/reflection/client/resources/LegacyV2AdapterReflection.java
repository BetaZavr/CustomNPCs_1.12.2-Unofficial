package noppes.npcs.reflection.client.resources;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LegacyV2Adapter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class LegacyV2AdapterReflection {

    private static Field pack;

    @SuppressWarnings("all")
    public static IResourcePack getIResourcePack(LegacyV2Adapter resourcePack) {
        if (resourcePack == null) { return null; }
        if (pack == null) {
            Exception error = null;
            try { pack = LegacyV2Adapter.class.getDeclaredField("field_191383_a"); } catch (Exception e) { error = e; }
            if (pack == null) {
                try {
                    pack = LegacyV2Adapter.class.getDeclaredField("pack");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"points\"", error);
                return null;
            }
        }
        try {
            pack.setAccessible(true);
            return (IResourcePack) pack.get(resourcePack);
        } catch (Exception e) {
            LogWriter.error("Error get \"points\" in " + resourcePack, e);
        }
        return null;
    }

}
