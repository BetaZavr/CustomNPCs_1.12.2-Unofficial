package noppes.npcs.mixin.api.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SoundEvent.class)
public interface SoundEventAPIMixin {

    @Accessor(value="soundName")
    ResourceLocation npcs$getSoundName();

}
