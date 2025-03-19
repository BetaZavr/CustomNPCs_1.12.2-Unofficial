package noppes.npcs.reflection.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class SoundEventReflection {

    private static Field soundName;

    public static ResourceLocation getSoundName(SoundEvent event) {
        if (event == null) { return new ResourceLocation(""); }
        if (soundName == null) {
            try {
                try { soundName = SoundEvent.class.getDeclaredField("field_187506_b"); }
                catch (Exception e) { soundName = SoundEvent.class.getDeclaredField("soundName"); }
            } catch (Exception e) {
                LogWriter.error("Error found field \"soundName\"", e);
                return new ResourceLocation("");
            }
        }
        try {
            soundName.setAccessible(true);
            return (ResourceLocation) soundName.get(event);
        } catch (Exception e) {
            LogWriter.error("Error get \"soundName\" in " + event, e);
        }
        return new ResourceLocation("");
    }

}