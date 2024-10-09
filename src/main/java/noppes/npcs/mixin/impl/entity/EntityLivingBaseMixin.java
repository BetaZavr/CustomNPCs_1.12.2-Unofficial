package noppes.npcs.mixin.impl.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.mixin.entity.IEntityLivingBaseMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityLivingBase.class)
public class EntityLivingBaseMixin implements IEntityLivingBaseMixin {

    @Mutable
    @Shadow
    private DamageSource lastDamageSource;

    @Mutable
    @Shadow
    private long lastDamageStamp;

    @Shadow
    protected float lastDamage;

    @Mutable
    @Shadow
    protected int recentlyHit;

    @Mutable
    @Shadow
    protected double interpTargetYaw;

    @Mutable
    @Shadow
    protected double interpTargetPitch;

    @Override
    public void npcs$setLastDamageSource(DamageSource newDamageSource) {
        lastDamageSource = newDamageSource;
    }

    @Override
    public void npcs$setLastDamageStamp(long newLastDamageStamp) { lastDamageStamp = newLastDamageStamp; }

    @Override
    public float npcs$getLastDamage() {
        return lastDamage;
    }

    @Override
    public void npcs$setRecentlyHit(int newRecentlyHit) {
        if (newRecentlyHit < 0) { newRecentlyHit *= -1; }
        recentlyHit = newRecentlyHit;
    }

    @Override
    public void npcs$setInterpTargetYaw(double newInterpTargetYaw) { interpTargetYaw = newInterpTargetYaw; }

    @Override
    public void npcs$setInterpTargetPitch(double newInterpTargetPitch) { interpTargetPitch = newInterpTargetPitch; }

}
