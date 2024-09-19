package noppes.npcs.mixin.client.particle;

import net.minecraft.client.particle.ParticleSmokeNormal;
import noppes.npcs.mixin.api.client.particle.ParticleSmokeNormalAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ParticleSmokeNormal.class)
public class ParticleSmokeNormalMixin implements ParticleSmokeNormalAPIMixin {

    @Mutable
    @Shadow(aliases = "smokeParticleScale")
    float smokeParticleScale;

    @Override
    public void npcs$setSmokeParticleScale(float newSmokeParticleScale) {
        if (newSmokeParticleScale < 0.0f) { newSmokeParticleScale *= -1.0f; }
        smokeParticleScale = newSmokeParticleScale;
    }
}
