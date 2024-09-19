package noppes.npcs.mixin.api.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BlockModelRenderer.class)
public interface BlockModelRendererAPIMixin {

    @Accessor(value="blockColors")
    BlockColors npcs$getBlockColors();

}
