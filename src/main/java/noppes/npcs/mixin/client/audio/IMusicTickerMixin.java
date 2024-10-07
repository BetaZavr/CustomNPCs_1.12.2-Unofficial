package noppes.npcs.mixin.client.audio;

import net.minecraft.client.audio.ISound;

public interface IMusicTickerMixin {

    void npcs$setTimeUntilNextMusic(int newTimeUntilNextMusic);

    void npcs$setCurrentMusic(ISound newCurrentMusic);

}
