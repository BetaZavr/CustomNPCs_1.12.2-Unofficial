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
            Exception error = null;
            try { soundName = SoundEvent.class.getDeclaredField("field_187506_b"); } catch (Exception e) { error = e; }
            if (soundName == null) {
                try {
                    soundName = SoundEvent.class.getDeclaredField("soundName");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"soundName\"", error);
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