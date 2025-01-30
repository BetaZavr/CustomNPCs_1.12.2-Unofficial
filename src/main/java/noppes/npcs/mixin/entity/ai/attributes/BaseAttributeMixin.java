package noppes.npcs.mixin.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.BaseAttribute;
import noppes.npcs.api.mixin.entity.ai.attributes.IBaseAttributeMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BaseAttribute.class)
public class BaseAttributeMixin implements IBaseAttributeMixin {

    @Mutable
    @Final
    @Shadow
    private double defaultValue;

    @Override
    public void npcs$setDefaultValue(double newDefaultValue) {
        defaultValue = newDefaultValue;
    }

}
