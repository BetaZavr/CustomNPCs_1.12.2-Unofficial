package noppes.npcs.reflection.nbt;

import net.minecraft.nbt.NBTTagLongArray;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class TagLongArrayReflection {

    private static Field data;

    public static long[] getData(NBTTagLongArray tag) {
        if (tag == null) { return new long[0]; }
        if (data == null) {
            Exception error = null;
            try { data = NBTTagLongArray.class.getDeclaredField("field_193587_b"); } catch (Exception e) { error = e; }
            if (data == null) {
                try {
                    data = NBTTagLongArray.class.getDeclaredField("data");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"data\"", error);
                return new long[0];
            }
        }
        try {
            data.setAccessible(true);
            return (long[]) data.get(tag);
        } catch (Exception e) {
            LogWriter.error("Error get \"soundName\" in " + tag, e);
        }
        return new long[0];
    }

}
