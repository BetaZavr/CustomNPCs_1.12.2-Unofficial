package noppes.npcs.mixin.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import noppes.npcs.mixin.api.util.SoundEventAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SoundEvent.class)
public class SoundEventMixin implements SoundEventAPIMixin {

    @Final
    @Shadow(aliases = "soundName")
    private ResourceLocation soundName;

    @Override
    public ResourceLocation npcs$getSoundName() { return soundName; }

}
