package noppes.npcs.mixin.util;

import net.minecraft.util.DamageSource;
import noppes.npcs.entity.data.Resistances;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DamageSource.class, priority = 499)
public class DamageSourceMixin {

    @Inject(at = @At("RETURN"),
            method = "<init>(Ljava/lang/String;)V")
    private void collectDamageType(CallbackInfo ci) {
        Resistances.add(((DamageSource) (Object) this).getDamageType());
    }

}
