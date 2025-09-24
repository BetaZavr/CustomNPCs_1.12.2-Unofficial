package noppes.npcs.api.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import java.util.List;

public interface IRenderLivingBaseMixin {

    LayerRenderer<?> npcs$getLayer(Class<?> layerClass);

    List<LayerRenderer<?>> npcs$getLayers();
}
