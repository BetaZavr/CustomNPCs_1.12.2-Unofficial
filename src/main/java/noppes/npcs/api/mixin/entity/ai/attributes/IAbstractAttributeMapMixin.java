package noppes.npcs.api.mixin.entity.ai.attributes;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.Map;

public interface IAbstractAttributeMapMixin {

    Map<IAttribute, IAttributeInstance> npcs$getAttributes();

    Map<String, IAttributeInstance> npcs$getAttributesByName();

    Multimap<IAttribute, IAttribute> npcs$getDescendantsByParent();

}
