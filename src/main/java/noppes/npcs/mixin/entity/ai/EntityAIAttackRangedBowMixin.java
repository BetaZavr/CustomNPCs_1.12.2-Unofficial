package noppes.npcs.mixin.entity.ai;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.monster.EntityMob;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityAIAttackRangedBow.class)
public class EntityAIAttackRangedBowMixin<T extends EntityMob & IRangedAttackMob> {

    @Final
    @Shadow
    private T entity;

    @Inject(method = "shouldExecute", at = @At("RETURN"), cancellable = true)
    public void npcs$shouldExecute(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && entity.getAttackTarget() instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity.getAttackTarget();
            if (!npc.isEntityAlive()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

}
