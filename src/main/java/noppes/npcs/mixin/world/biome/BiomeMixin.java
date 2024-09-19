package noppes.npcs.mixin.world.biome;

import net.minecraft.world.biome.Biome;
import noppes.npcs.mixin.api.world.biome.BiomeAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Biome.class)
public class BiomeMixin implements BiomeAPIMixin {

    @Final
    @Shadow(aliases = "biomeName")
    private String biomeName;

    @Override
    public String npcs$getBiomeName() { return biomeName; }

}
