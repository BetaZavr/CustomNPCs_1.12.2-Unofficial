package noppes.npcs.mixin.impl.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import noppes.npcs.mixin.client.audio.ISoundManagerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = SoundManager.class)
public class SoundManagerMixin implements ISoundManagerMixin {

    @Final
    @Shadow
    private Map<String, ISound> playingSounds;

    @Override
    public Map<String, ISound> npcs$getPlayingSounds() { return playingSounds; }

}
