package noppes.npcs.mixin.impl.client.audio;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundRegistry;
import noppes.npcs.mixin.client.audio.ISoundHandlerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SoundHandler.class)
public class SoundHandlerMixin implements ISoundHandlerMixin {

    @Final
    @Shadow(aliases = "models")
    private SoundManager sndManager;

    @Final
    @Shadow(aliases = "soundRegistry")
    private SoundRegistry soundRegistry;

    @Override
    public SoundManager npcs$getSndManager() { return sndManager; }

    @Override
    public SoundRegistry npcs$getSoundRegistry() { return soundRegistry; }

}
