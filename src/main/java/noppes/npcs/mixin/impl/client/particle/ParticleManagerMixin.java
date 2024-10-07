package noppes.npcs.mixin.impl.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import noppes.npcs.mixin.client.particle.IParticleManagerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = ParticleManager.class)
public class ParticleManagerMixin implements IParticleManagerMixin {

    @Final
    @Shadow(aliases = "particleTypes")
    private Map<Integer, IParticleFactory> particleTypes;

    @Override
    public Map<Integer, IParticleFactory> npcs$getParticleTypes() { return particleTypes; }

}
