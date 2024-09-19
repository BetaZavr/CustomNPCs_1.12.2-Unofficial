package noppes.npcs.mixin.api.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MusicTicker.class)
public interface MusicTickerAPIMixin {

    @Mutable
    @Accessor(value="timeUntilNextMusic")
    void npcs$setTimeUntilNextMusic(int newTimeUntilNextMusic);

    @Mutable
    @Accessor(value="currentMusic")
    void npcs$setCurrentMusic(ISound newCurrentMusic);

}
