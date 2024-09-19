package noppes.npcs.mixin.api.client.particle;

import net.minecraft.client.particle.ParticleSmokeNormal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ParticleSmokeNormal.class)
public interface ParticleSmokeNormalAPIMixin {

    @Mutable
    @Accessor(value="smokeParticleScale")
    void npcs$setSmokeParticleScale(float newSmokeParticleScale);

}
