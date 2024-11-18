package noppes.npcs.mixin.world.biome;

import net.minecraft.world.biome.Biome;
import noppes.npcs.api.mixin.world.biome.IBiomeMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Biome.class)
public class BiomeMixin implements IBiomeMixin {

    @Final
    @Shadow
    private String biomeName;

    @Override
    public String npcs$getBiomeName() { return biomeName; }

}
