package noppes.npcs.mixin.client.particle;

import net.minecraft.client.particle.IParticleFactory;

import java.util.Map;

public interface IParticleManagerMixin {

    Map<Integer, IParticleFactory> npcs$getParticleTypes();

}
