package noppes.npcs.mixin.client.audio;

import noppes.npcs.api.mixin.client.audio.ISoundSystemMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;

@Mixin(value = SoundSystem.class, remap = false, priority = 499)
public class SoundSystemMixin implements ISoundSystemMixin {

    @Shadow
    protected Library soundLibrary;

    @Override
    public Library npcs$getSoundLibrary() { return soundLibrary; }

}
