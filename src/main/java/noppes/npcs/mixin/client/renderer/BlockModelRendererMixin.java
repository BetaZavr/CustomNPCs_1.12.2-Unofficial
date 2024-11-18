package noppes.npcs.mixin.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import noppes.npcs.api.mixin.client.renderer.IBlockModelRendererMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockModelRenderer.class)
public class BlockModelRendererMixin implements IBlockModelRendererMixin {

    @Final
    @Shadow
    private BlockColors blockColors;

    @Override
    public BlockColors npcs$getBlockColors() { return blockColors; }

}
