package noppes.npcs.mixin.client.audio;

import net.minecraft.client.audio.PositionedSound;
import noppes.npcs.api.mixin.client.audio.IPositionedSoundMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PositionedSound.class)
public class PositionedSoundMixin implements IPositionedSoundMixin {

    @Mutable
    @Shadow
    protected float xPosF;

    @Mutable
    @Shadow
    protected float yPosF;

    @Mutable
    @Shadow
    protected float zPosF;

    @Override
    public void npcs$setXPosF(float newXPosF) { xPosF = newXPosF; }

    @Override
    public void npcs$setYPosF(float newYPosF) { yPosF = newYPosF; }

    @Override
    public void npcs$setZPosF(float newZPosF) {zPosF = newZPosF; }

}
