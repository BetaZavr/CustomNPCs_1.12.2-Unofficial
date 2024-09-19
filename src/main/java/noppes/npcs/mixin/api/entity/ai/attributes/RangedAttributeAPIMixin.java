package noppes.npcs.mixin.api.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RangedAttribute.class)
public interface RangedAttributeAPIMixin {

    @Accessor(value="minimumValue")
    double npcs$getMinValue();

    @Accessor(value="maximumValue")
    double npcs$getMaxValue();

    @Accessor(value="minimumValue")
    @Mutable
    void npcs$setMinValue(double newMinValue);

    @Accessor(value="maximumValue")
    @Mutable
    void npcs$setMaxValue(double newMaxValue);
}
