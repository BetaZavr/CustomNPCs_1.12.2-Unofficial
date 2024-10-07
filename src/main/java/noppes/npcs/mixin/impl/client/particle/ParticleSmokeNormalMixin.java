package noppes.npcs.mixin.impl.client.particle;

import net.minecraft.client.particle.ParticleSmokeNormal;
import noppes.npcs.mixin.client.particle.IParticleSmokeNormalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ParticleSmokeNormal.class)
public class ParticleSmokeNormalMixin implements IParticleSmokeNormalMixin {

    @Mutable
    @Shadow(aliases = "smokeParticleScale")
    float smokeParticleScale;

    @Override
    public void npcs$setSmokeParticleScale(float newSmokeParticleScale) {
        if (newSmokeParticleScale < 0.0f) { newSmokeParticleScale *= -1.0f; }
        smokeParticleScale = newSmokeParticleScale;
    }
}
