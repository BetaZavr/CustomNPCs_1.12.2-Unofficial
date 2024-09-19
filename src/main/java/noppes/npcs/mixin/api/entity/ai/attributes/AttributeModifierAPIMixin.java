package noppes.npcs.mixin.api.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AttributeModifier.class)
public interface AttributeModifierAPIMixin {

    @Accessor(value="amount")
    @Mutable
    void npcs$setAmount(double newAmount);

    @Accessor(value="name")
    @Mutable
    void npcs$setName(String newName);

    @Accessor(value="operation")
    @Mutable
    void npcs$setOperation(int newOperation);

}
