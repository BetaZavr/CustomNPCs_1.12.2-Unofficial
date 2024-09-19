package noppes.npcs.mixin.api.client.audio;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SoundHandler.class)
public interface SoundHandlerAPIMixin {

    @Accessor(value="sndManager")
    SoundManager npcs$getSndManager();

    @Accessor(value="soundRegistry")
    SoundRegistry npcs$getSoundRegistry();

}
