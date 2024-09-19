package noppes.npcs.mixin.api.world.biome;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Biome.class)
public interface BiomeAPIMixin {

    @Accessor(value="biomeName")
    String npcs$getBiomeName();

}
