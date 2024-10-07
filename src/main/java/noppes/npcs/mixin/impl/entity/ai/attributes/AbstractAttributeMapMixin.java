package noppes.npcs.mixin.impl.entity.ai.attributes;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import noppes.npcs.mixin.entity.ai.attributes.IAbstractAttributeMapMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = AbstractAttributeMap.class)
public class AbstractAttributeMapMixin implements IAbstractAttributeMapMixin {

    @Final
    @Shadow(aliases = "attributes")
    protected Map<IAttribute, IAttributeInstance> attributes;

    @Final
    @Shadow(aliases = "attributesByName")
    protected Map<String, IAttributeInstance> attributesByName;

    @Final
    @Shadow(aliases = "descendantsByParent")
    protected Multimap<IAttribute, IAttribute> descendantsByParent;

    @Override
    public Map<IAttribute, IAttributeInstance> npcs$getAttributes() { return attributes; }

    @Override
    public Map<String, IAttributeInstance> npcs$getAttributesByName() { return attributesByName; }

    @Override
    public Multimap<IAttribute, IAttribute> npcs$getDescendantsByParent() { return descendantsByParent; }

}
