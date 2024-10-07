package noppes.npcs.mixin.impl.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import noppes.npcs.mixin.client.audio.IMusicTickerMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MusicTicker.class)
public class MusicTickerMixin implements IMusicTickerMixin {

    @Mutable
    @Shadow(aliases = "timeUntilNextMusic")
    private int timeUntilNextMusic;

    @Mutable
    @Shadow(aliases = "currentMusic")
    private ISound currentMusic;

    @Override
    public void npcs$setTimeUntilNextMusic(int newTimeUntilNextMusic) {
        if (newTimeUntilNextMusic < 0) { newTimeUntilNextMusic *= -1; }
        timeUntilNextMusic = newTimeUntilNextMusic;
    }

    @Override
    public void npcs$setCurrentMusic(ISound newCurrentMusic) {
        currentMusic = newCurrentMusic;
    }

}
