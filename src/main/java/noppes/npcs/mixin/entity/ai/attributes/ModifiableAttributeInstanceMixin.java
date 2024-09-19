package noppes.npcs.mixin.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import noppes.npcs.mixin.api.entity.ai.attributes.ModifiableAttributeInstanceAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModifiableAttributeInstance.class)
public class ModifiableAttributeInstanceMixin implements ModifiableAttributeInstanceAPIMixin {

    @Final
    @Shadow(aliases = "genericAttribute")
    private IAttribute genericAttribute;

    @Override
    public IAttribute npcs$getGenericAttribute() { return genericAttribute; }

}
