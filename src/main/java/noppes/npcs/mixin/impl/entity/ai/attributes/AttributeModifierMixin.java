package noppes.npcs.mixin.impl.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import noppes.npcs.mixin.entity.ai.attributes.IAttributeModifierMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AttributeModifier.class)
public class AttributeModifierMixin implements IAttributeModifierMixin {

    @Mutable
    @Final
    @Shadow(aliases = "amount")
    private double amount;

    @Mutable
    @Final
    @Shadow(aliases = "operation")
    private int operation;

    @Mutable
    @Final
    @Shadow(aliases = "name")
    private String name;

    @Override
    public void npcs$setAmount(double newAmount) { amount = newAmount; }

    @Override
    public void npcs$setName(String newName) { name = newName; }

    @Override
    public void npcs$setOperation(int newOperation) { operation = newOperation; }

}
