package noppes.npcs.api.mixin.entity;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;

public interface IEntityLivingBaseMixin {
    
    void npcs$setLastDamageSource(DamageSource newDamageSource);

    void npcs$setLastDamageStamp(long newLastDamageStamp);

    float npcs$getLastDamage();

    void npcs$setRecentlyHit(int newRecentlyHit);

    void npcs$setInterpTargetYaw(double newInterpTargetYaw);

    void npcs$setInterpTargetPitch(double newInterpTargetPitch);

    void npcs$setCurrentDamageSource(DamageSource source);

    DataParameter<Byte> npcs$getHandStates();

}

