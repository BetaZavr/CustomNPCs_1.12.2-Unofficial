package noppes.npcs.mixin.client.audio;

import noppes.npcs.mixin.api.client.audio.SoundSystemAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;

@Mixin(value = SoundSystem.class, remap = false)
public class SoundSystemMixin implements SoundSystemAPIMixin {

    @Shadow(aliases = "soundLibrary")
    protected Library soundLibrary;

    @Override
    public Library npcs$getSoundLibrary() { return soundLibrary; }

}
