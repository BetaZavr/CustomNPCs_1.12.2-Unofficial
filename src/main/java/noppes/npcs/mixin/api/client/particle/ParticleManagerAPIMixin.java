package noppes.npcs.mixin.api.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = ParticleManager.class)
public interface ParticleManagerAPIMixin {

    @Accessor(value="particleTypes")
    Map<Integer, IParticleFactory> npcs$getParticleTypes();

}
