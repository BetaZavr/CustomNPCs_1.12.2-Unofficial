package noppes.npcs.mixin.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.mixin.entity.player.IEntityPlayerMixin;
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

    @Inject(method = "applyEntityAttributes", at = @At("TAIL"))
    protected void npcs$applyEntityAttributes(CallbackInfo ci) {
        if (npcs$animation == null) { npcs$animation = new DataAnimation((EntityPlayer) (Object) this); }
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    protected void npcs$onUpdate(CallbackInfo ci) {
        if (npcs$animation != null) { npcs$animation.updateTime(); }
    }

    @Override
    public void npcs$setSpawnPos(BlockPos newSpawnPos) {
        spawnPos = newSpawnPos;
    }

    @Override
    public DataAnimation npcs$getAnimation() { return npcs$animation; }

}
