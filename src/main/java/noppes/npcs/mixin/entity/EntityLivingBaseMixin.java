package noppes.npcs.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.mixin.entity.IEntityLivingBaseMixin;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(value = EntityLivingBase.class)
public class EntityLivingBaseMixin implements IEntityLivingBaseMixin {

    @Final
    @Shadow
    protected static DataParameter<Byte> HAND_STATES;

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

    @Unique
    protected Random nps$rand = new Random();

    @Unique
    private DamageSource npcs$currentDamageSource;

    // remember the source of damage
    @Inject(method = "attackEntityFrom", at = @At("HEAD"))
    private void npcs$saveDamageSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        npcs$currentDamageSource = source;
    }

    // change recoil force from mod settings
    @Inject(method = "knockBack", at = @At("HEAD"), cancellable = true)
    private void npcs$knockBack(Entity entityIn, float strength, double xRatio, double zRatio, CallbackInfo ci) {
        EntityLivingBase parent = (EntityLivingBase) (Object) this;
        if (npcs$currentDamageSource != null && !npcs$currentDamageSource.isExplosion() && npcs$currentDamageSource.isProjectile()) { strength *= 0.375f; }
        else { strength *= 0.5f; }
        strength *= ((float) CustomNpcs.KnockBackBasePower / 100.0f);
        net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(parent, entityIn, strength, xRatio, zRatio);
        if (event.isCanceled()) { return; }
        strength = event.getStrength();
        xRatio = event.getRatioX(); zRatio = event.getRatioZ();
        if (nps$rand.nextDouble() >= parent.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
            parent.isAirBorne = true;
            double f = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio);
            parent.motionX /= 2.0d;
            parent.motionZ /= 2.0d;
            parent.motionX -= xRatio / f * (double) strength;
            parent.motionZ -= zRatio / f * (double) strength;
            if (parent.onGround) {
                parent.motionY /= 2.0D;
                parent.motionY += strength;
                if (parent.motionY > 0.4000000059604645d) {
                    parent.motionY = 0.4000000059604645d;
                }
            }
        }
        npcs$currentDamageSource = null;
        ci.cancel();
    }

    // Replace knockback force when dealing damage
    @Redirect(
            method = "attackEntityFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;knockBack(Lnet/minecraft/entity/Entity;FDD)V")
    )
    private void npcs$attackEntityFrom(EntityLivingBase instance, Entity entity, float strength, double xRatio, double zRatio) {
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            float f0, f1;
            if (npcs$currentDamageSource.isProjectile()) {
                f0 = 0.25f;
                f1 = 0.15f * (float) npc.stats.ranged.getKnockback();
            } else {
                f0 = 0.2f;
                f1 = 0.2f  * (float) npc.stats.melee.getKnockback();
            }
            if (f1 == 0.0f) { strength = 0.0f; } else { strength = f0 + f1; }
        }
        if (strength != 0) { ((EntityLivingBase) (Object)this).knockBack(entity, strength, xRatio, zRatio); }
    }



    @Inject(method = "canEntityBeSeen", at = @At("HEAD"))
    public void npcs$canEntityBeSeen(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {

    }

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

    @Override
    public void npcs$setCurrentDamageSource(DamageSource source) { npcs$currentDamageSource = source; }

    @Override
    public DataParameter<Byte> npcs$getHandStates() { return HAND_STATES; }

}
