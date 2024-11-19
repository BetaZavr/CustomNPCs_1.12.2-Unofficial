package noppes.npcs.mixin.event.entity.living;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import noppes.npcs.api.mixin.event.entity.living.ILivingAttackEventMixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingAttackEvent.class, remap = false)
public abstract class LivingAttackEventMixin implements ILivingAttackEventMixin {

    @Mutable
    @Final
    @Shadow
    private float amount;

    @Override
    public void npcs$setAmount(float newAmount) {
        amount = newAmount;
    }

}
