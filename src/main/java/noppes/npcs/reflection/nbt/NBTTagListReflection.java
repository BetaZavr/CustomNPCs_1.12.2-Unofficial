package noppes.npcs.reflection.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class NBTTagListReflection {

    private static Field tagList;

    @SuppressWarnings("unchecked")
    public static List<NBTBase> getTagList(NBTTagList tag) {
        if (tag == null) { return Collections.emptyList(); }
        if (tagList == null) {
            Exception error = null;
            try { tagList = NBTTagList.class.getDeclaredField("field_74747_a"); } catch (Exception e) { error = e; }
            if (tagList == null) {
                try {
                    tagList = NBTTagList.class.getDeclaredField("tagList");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"tagList\"", error);
                return Collections.emptyList();
            }
        }
        try {
            tagList.setAccessible(true);
            return (List<NBTBase>) tagList.get(tag);
        } catch (Exception e) {
            LogWriter.error("Error get \"tagList\" in " + tag, e);
        }
        return Collections.emptyList();
    }

}
