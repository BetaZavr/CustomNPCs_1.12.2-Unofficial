package noppes.npcs.mixin.impl.client.audio;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.util.SoundCategory;
import noppes.npcs.client.ClientTickHandler;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.mixin.client.audio.ISoundHandlerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SoundHandler.class)
public class SoundHandlerMixin implements ISoundHandlerMixin {

    @Final
    @Shadow
    private SoundManager sndManager;

    @Final
    @Shadow
    private SoundRegistry soundRegistry;

    @Override
    public SoundManager npcs$getSndManager() { return sndManager; }

    @Override
    public SoundRegistry npcs$getSoundRegistry() { return soundRegistry; }

}
