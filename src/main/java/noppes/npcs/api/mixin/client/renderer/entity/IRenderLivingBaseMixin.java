package noppes.npcs.api.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public interface IRenderLivingBaseMixin {

    LayerRenderer<?> npcs$getLayer(Class<?> layerClass);

}
