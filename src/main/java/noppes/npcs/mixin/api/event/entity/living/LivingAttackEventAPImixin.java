package noppes.npcs.mixin.api.event.entity.living;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LivingAttackEvent.class, remap = false)
public interface LivingAttackEventAPImixin {

    @Accessor(value="amount")
    @Mutable
    void npcs$setAmount(float newAmount);

}
