package noppes.npcs.mixin.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import noppes.npcs.mixin.api.client.renderer.BlockModelRendererAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockModelRenderer.class)
public class BlockModelRendererMixin implements BlockModelRendererAPIMixin {

    @Final
    @Shadow(aliases = "blockColors")
    private BlockColors blockColors;

    @Override
    public BlockColors npcs$getBlockColors() { return blockColors; }

}
