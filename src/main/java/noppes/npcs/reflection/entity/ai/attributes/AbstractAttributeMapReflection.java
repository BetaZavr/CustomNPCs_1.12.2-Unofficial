package noppes.npcs.reflection.entity.ai.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.LowerStringMap;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AbstractAttributeMapReflection {

    private static Field attributes;
    private static Field attributesByName;
    private static Field descendantsByParent;

    @SuppressWarnings("all")
    public static Map<IAttribute, IAttributeInstance> getAttributes(AbstractAttributeMap attributeMap) {
        if (attributeMap == null) { return new HashMap<>(); }
        if (attributes == null) {
            Exception error = null;
            try { attributes = AbstractAttributeMap.class.getDeclaredField("field_111154_a"); } catch (Exception e) { error = e; }
            if (attributes == null) {
                try {
                    attributes = AbstractAttributeMap.class.getDeclaredField("attributes");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"attributes\"", error);
                return new HashMap<>();
            }
        }
        try {
            attributes.setAccessible(true);
            return (Map<IAttribute, IAttributeInstance> ) attributes.get(attributeMap);
        } catch (Exception e) {
            LogWriter.error("Error get \"attributes\" in " + attributeMap, e);
        }
        return new HashMap<>();
    }

    @SuppressWarnings("all")
    public static Map<String, IAttributeInstance> getAttributesByName(AbstractAttributeMap attributeMap) {
        if (attributeMap == null) { return new LowerStringMap<>(); }
        if (attributesByName == null) {
            Exception error = null;
            try { attributesByName = AbstractAttributeMap.class.getDeclaredField("field_111153_b"); } catch (Exception e) { error = e; }
            if (attributesByName == null) {
                try {
                    attributesByName = AbstractAttributeMap.class.getDeclaredField("attributesByName");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"attributesByName\"", error);
                return new LowerStringMap<>();
            }
        }
        try {
            attributesByName.setAccessible(true);
            return (Map<String, IAttributeInstance>) attributesByName.get(attributeMap);
        } catch (Exception e) {
            LogWriter.error("Error get \"attributesByName\" in " + attributeMap, e);
        }
        return new LowerStringMap<>();
    }

    @SuppressWarnings("all")
    public static Multimap<IAttribute, IAttribute> getDescendantsByParent(AbstractAttributeMap attributeMap) {
        if (attributeMap == null) { return HashMultimap.create(); }
        if (descendantsByParent == null) {
            Exception error = null;
            try { descendantsByParent = AbstractAttributeMap.class.getDeclaredField("field_180377_c"); } catch (Exception e) { error = e; }
            if (descendantsByParent == null) {
                try {
                    descendantsByParent = AbstractAttributeMap.class.getDeclaredField("descendantsByParent");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"descendantsByParent\"", error);
                return HashMultimap.create();
            }
        }
        try {
            descendantsByParent.setAccessible(true);
            return (Multimap<IAttribute, IAttribute>) descendantsByParent.get(attributeMap);
        } catch (Exception e) {
            LogWriter.error("Error get \"descendantsByParent\" in " + attributeMap, e);
        }
        return HashMultimap.create();
    }

}
