package noppes.npcs.reflection.entity.player;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class EntityPlayerMPReflection {

    private static Field language;

    @SuppressWarnings("all")
    public static String getLanguage(EntityPlayerMP player) {
        if (player == null) { return "en_us"; }
        if (language == null) {
            Exception error = null;
            try { language = EntityPlayerMP.class.getDeclaredField("field_71148_cg"); } catch (Exception e) { error = e; }
            if (language == null) {
                try {
                    language = EntityPlayerMP.class.getDeclaredField("language");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"language\"", error);
                return "en_us";
            }
        }
        try {
            language.setAccessible(true);
            return (String) language.get(player);
        } catch (Exception e) {
            LogWriter.error("Error get \"language\" in " + player, e);
        }
        return "en_us";
    }

}
