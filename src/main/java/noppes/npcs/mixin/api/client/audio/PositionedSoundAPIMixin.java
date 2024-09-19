package noppes.npcs.mixin.api.client.audio;

import net.minecraft.client.audio.PositionedSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PositionedSound.class)
public interface PositionedSoundAPIMixin {

    @Mutable
    @Accessor(value="xPosF")
    void npcs$setXPosF(float newXPosF);

    @Mutable
    @Accessor(value="yPosF")
    void npcs$setYPosF(float newYPosF);

    @Mutable
    @Accessor(value="zPosF")
    void npcs$setZPosF(float newZPosF);

}
