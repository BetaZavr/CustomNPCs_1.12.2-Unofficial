package noppes.npcs.mixin.api.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = SoundManager.class)
public interface SoundManagerAPIMixin {

    @Accessor(value="playingSounds")
    Map<String, ISound> npcs$getPlayingSounds();

}
