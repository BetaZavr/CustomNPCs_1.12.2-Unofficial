package noppes.npcs.reflection.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class ModifiableAttributeInstanceReflection {

    private static Field genericAttribute;

    public static IAttribute getGenericAttribute(ModifiableAttributeInstance attribute) {
        if (attribute == null) { return null; }
        if (genericAttribute == null) {
            Exception error = null;
            try { genericAttribute = ModifiableAttributeInstance.class.getDeclaredField("field_75884_a"); } catch (Exception e) { error = e; }
            if (genericAttribute == null) {
                try {
                    genericAttribute = ModifiableAttributeInstance.class.getDeclaredField("genericAttribute");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"genericAttribute\"", error);
                return null;
            }
        }
        try {
            genericAttribute.setAccessible(true);
            return (IAttribute) genericAttribute.get(attribute);
        } catch (Exception e) {
            LogWriter.error("Error get \"genericAttribute\" in " + attribute, e);
        }
        return null;
    }

}
