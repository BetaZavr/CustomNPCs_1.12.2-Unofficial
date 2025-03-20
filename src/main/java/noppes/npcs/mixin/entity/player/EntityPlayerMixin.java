package noppes.npcs.mixin.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.mixin.entity.player.IEntityPlayerMixin;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumAnimationStages;
import noppes.npcs.entity.data.DataAnimation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayer.class)
public abstract class EntityPlayerMixin implements IEntityPlayerMixin {

    @Mutable
    @Shadow
    protected BlockPos spawnPos;

    @Unique
    public DataAnimation npcs$animation;

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    public void npcs$attackTargetEntityWithCurrentItem(Entity targetEntity, CallbackInfo ci) {
        if (CustomNpcs.ShowCustomAnimation) {
            npcs$animation.tryRunAnimation(AnimationKind.ATTACKING);
        }
    }

    @Inject(method = "applyEntityAttributes", at = @At("TAIL"))
    protected void npcs$applyEntityAttributes(CallbackInfo ci) {
        if (npcs$animation == null) { npcs$animation = new DataAnimation((EntityPlayer) (Object) this); }
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    protected void npcs$onUpdate(CallbackInfo ci) {
        if (npcs$animation != null) { npcs$animation.updateTime(); }
    }

    @Inject(method = "jump", at = @At("TAIL"))
    public void npcs$jump(CallbackInfo ci) {
        npcs$animation.setJump(true);
    }

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    public void npcs$onLivingUpdate(CallbackInfo ci) {
        if (CustomNpcs.ShowCustomAnimation) {
            EntityPlayer player = (EntityPlayer) (Object) this;
            // Jump
            if (npcs$animation.getJump() && player.onGround && npcs$animation.getAnimationStage() != EnumAnimationStages.Started) {
                npcs$animation.setJump(false);
                if (npcs$animation.isAnimated(AnimationKind.JUMP)) {
                    npcs$animation.stopAnimation();
                }
            }
            // Swing
            if (!npcs$animation.getSwing() && player.swingProgress > 0.0f) {
                npcs$animation.setSwing(true);
                if (!npcs$animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.AIM, AnimationKind.SHOOT)) {
                    AnimationConfig anim = npcs$animation.tryRunAnimation(AnimationKind.SWING);
                    if (anim != null) {
                        player.swingProgress = 0.0f;
                        player.swingProgressInt = 0;
                        player.prevSwingProgress = 0.0f;
                        player.isSwingInProgress = false;
                    }
                }
            }
            else if (npcs$animation.getSwing() && player.swingProgress == 0.0f) {
                npcs$animation.setSwing(false);
            }
            // walking or standing
            npcs$animation.resetWalkAndStandAnimations();
        }
    }

    @Override
    public void npcs$setSpawnPos(BlockPos newSpawnPos) {
        spawnPos = newSpawnPos;
    }

    @Override
    public DataAnimation npcs$getAnimation() { return npcs$animation; }

}
