package noppes.npcs.mixin.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.RangedAttribute;
import noppes.npcs.mixin.api.entity.ai.attributes.RangedAttributeAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = RangedAttribute.class)
public class RangedAttributeMixin implements RangedAttributeAPIMixin {

    @Mutable
    @Final
    @Shadow(aliases = "minimumValue")
    private double minimumValue;

    @Mutable
    @Final
    @Shadow(aliases = "maximumValue")
    private double maximumValue;

    @Override
    public double npcs$getMinValue() { return minimumValue; }

    @Override
    public double npcs$getMaxValue() { return maximumValue; }

    @Override
    public void npcs$setMinValue(double newMinValue) { minimumValue = newMinValue; }

    @Override
    public void npcs$setMaxValue(double newMaxValue) { maximumValue = newMaxValue; }

}
