package noppes.npcs.mixin.impl.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import noppes.npcs.client.ClientTickHandler;
import noppes.npcs.client.controllers.MusicController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MusicTicker.class)
public class MusicTickerMixin {

    @Mutable
    @Shadow
    private int timeUntilNextMusic;

    @Mutable
    @Shadow
    private ISound currentMusic;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void npcs$update(CallbackInfo ci) {
        if (ClientTickHandler.inGame && !MusicController.Instance.music.isEmpty() && MusicController.Instance.isPlaying(MusicController.Instance.music)) {
            ci.cancel();
        }
    }

}
