package noppes.npcs.mixin.client.particle;

import net.minecraft.client.particle.ParticleFlame;
import noppes.npcs.api.mixin.client.particle.IParticleFlameMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ParticleFlame.class, priority = 499)
public class ParticleFlameMixin implements IParticleFlameMixin {

    @Mutable
    @Final
    @Shadow
    private float flameScale;

    @Override
    public void npcs$setFlameScale(float newFlameScale) {
        if (newFlameScale < 0.0f) { newFlameScale *= -1.0f; }
        flameScale = newFlameScale;
    }

}
