package noppes.npcs.mixin.api.client.particle;

import net.minecraft.client.particle.ParticleFlame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ParticleFlame.class)
public interface ParticleFlameAPIMixin {

    @Mutable
    @Accessor(value="flameScale")
    void npcs$setFlameScale(float newFlameScale);

}
