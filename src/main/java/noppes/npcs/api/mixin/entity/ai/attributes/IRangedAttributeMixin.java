package noppes.npcs.api.mixin.entity.ai.attributes;

public interface IRangedAttributeMixin {

    double npcs$getMinValue();

    double npcs$getMaxValue();

    void npcs$setMinValue(double newMinValue);

    void npcs$setMaxValue(double newMaxValue);

}
