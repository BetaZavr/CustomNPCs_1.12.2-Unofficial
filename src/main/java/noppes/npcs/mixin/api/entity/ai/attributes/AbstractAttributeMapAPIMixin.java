package noppes.npcs.mixin.api.entity.ai.attributes;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = AbstractAttributeMap.class)
public interface AbstractAttributeMapAPIMixin {

    @Accessor(value="attributes")
    Map<IAttribute, IAttributeInstance> npcs$getAttributes();

    @Accessor(value="attributesByName")
    Map<String, IAttributeInstance> npcs$getAttributesByName();

    @Accessor(value="descendantsByParent")
    Multimap<IAttribute, IAttribute> npcs$getDescendantsByParent();

}
