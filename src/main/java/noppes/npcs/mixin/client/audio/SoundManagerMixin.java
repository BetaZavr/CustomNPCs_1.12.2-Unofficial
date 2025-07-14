package noppes.npcs.mixin.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import noppes.npcs.api.mixin.client.audio.ISoundManagerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = SoundManager.class, priority = 499)
public class SoundManagerMixin implements ISoundManagerMixin {

    @Final
    @Shadow
    private Map<String, ISound> playingSounds;

    @Override
    public Map<String, ISound> npcs$getPlayingSounds() { return playingSounds; }

}
