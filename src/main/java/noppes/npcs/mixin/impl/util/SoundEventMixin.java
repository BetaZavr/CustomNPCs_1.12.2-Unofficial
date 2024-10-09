package noppes.npcs.mixin.impl.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import noppes.npcs.mixin.util.ISoundEventMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SoundEvent.class)
public class SoundEventMixin implements ISoundEventMixin {

    @Final
    @Shadow
    private ResourceLocation soundName;

    @Override
    public ResourceLocation npcs$getSoundName() { return soundName; }

}
