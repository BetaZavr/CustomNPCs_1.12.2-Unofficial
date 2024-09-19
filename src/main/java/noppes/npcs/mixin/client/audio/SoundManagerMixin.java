package noppes.npcs.mixin.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import noppes.npcs.mixin.api.client.audio.SoundManagerAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = SoundManager.class)
public class SoundManagerMixin implements SoundManagerAPIMixin {

    @Final
    @Shadow(aliases = "playingSounds")
    private Map<String, ISound> playingSounds;

    @Override
    public Map<String, ISound> npcs$getPlayingSounds() { return playingSounds; }

}
