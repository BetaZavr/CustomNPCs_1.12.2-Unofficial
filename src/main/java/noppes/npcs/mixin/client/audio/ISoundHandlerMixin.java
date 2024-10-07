package noppes.npcs.mixin.client.audio;

import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundRegistry;

public interface ISoundHandlerMixin {

    SoundManager npcs$getSndManager();

    SoundRegistry npcs$getSoundRegistry();

}
