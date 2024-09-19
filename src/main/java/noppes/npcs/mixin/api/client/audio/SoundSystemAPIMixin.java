package noppes.npcs.mixin.api.client.audio;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;

@Mixin(value = SoundSystem.class, remap = false)
public interface SoundSystemAPIMixin {

    @Accessor(value="soundLibrary")
    Library npcs$getSoundLibrary();

}
