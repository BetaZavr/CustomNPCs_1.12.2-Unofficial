package noppes.npcs.mixin.impl.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.mixin.entity.IEntityLivingBaseMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityLivingBase.class)
public class EntityLivingBaseMixin implements IEntityLivingBaseMixin {

    @Mutable
    @Shadow(aliases = "lastDamageSource")
    private DamageSource lastDamageSource;

    @Mutable
    @Shadow(aliases = "lastDamageStamp")
    protected long lastDamageStamp;

    @Shadow(aliases = "lastDamage")
    protected float lastDamage;

    @Mutable
    @Shadow(aliases = "recentlyHit")
    protected int recentlyHit;

    @Mutable
    @Shadow(aliases = "interpTargetYaw")
    protected double interpTargetYaw;

    @Mutable
    @Shadow(aliases = "recentlyHit")
    protected double interpTargetPitch;

    @Accessor(value = "lastDamageSource")
    public void npcs$setLastDamageSource(DamageSource newDamageSource) {
        lastDamageSource = newDamageSource;
    }

    @Accessor(value = "lastDamageStamp")
    public void npcs$setLastDamageStamp(long newLastDamageStamp) { lastDamageStamp = newLastDamageStamp; }

    @Accessor(value = "lastDamage")
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
