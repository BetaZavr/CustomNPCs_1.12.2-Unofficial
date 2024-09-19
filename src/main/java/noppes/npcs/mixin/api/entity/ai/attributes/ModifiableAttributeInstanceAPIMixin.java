package noppes.npcs.mixin.api.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ModifiableAttributeInstance.class)
public interface ModifiableAttributeInstanceAPIMixin {

    @Accessor(value="genericAttribute")
    IAttribute  npcs$getGenericAttribute();

}
