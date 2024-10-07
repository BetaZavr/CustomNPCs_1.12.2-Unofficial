package noppes.npcs.mixin.impl.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import noppes.npcs.mixin.entity.ai.attributes.IModifiableAttributeInstanceMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModifiableAttributeInstance.class)
public class ModifiableAttributeInstanceMixin implements IModifiableAttributeInstanceMixin {

    @Final
    @Shadow(aliases = "genericAttribute")
    private IAttribute genericAttribute;

    @Override
    public IAttribute npcs$getGenericAttribute() { return genericAttribute; }

}
