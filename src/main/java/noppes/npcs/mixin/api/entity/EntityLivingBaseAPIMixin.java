package noppes.npcs.mixin.api.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityLivingBase.class)
public interface EntityLivingBaseAPIMixin {

    @Accessor(value="lastDamageSource")
    @Mutable
    void npcs$setLastDamageSource(DamageSource newDamageSource);

    @Accessor(value="lastDamageStamp")
    @Mutable
    void npcs$setLastDamageStamp(long newLastDamageStamp);

    @Accessor(value="lastDamage")
    float npcs$getLastDamage();

    @Accessor(value="recentlyHit")
    void npcs$setRecentlyHit(int newRecentlyHit);

    @Accessor(value="interpTargetYaw")
    void npcs$setInterpTargetYaw(double newInterpTargetYaw);

    @Accessor(value="interpTargetPitch")
    void npcs$setInterpTargetPitch(double newInterpTargetPitch);

}
