package noppes.npcs.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import noppes.npcs.api.mixin.client.renderer.entity.IRenderLivingBaseMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = RenderLivingBase.class, priority = 499)
public class RenderLivingBaseMixin implements IRenderLivingBaseMixin {

    @Shadow
    protected List<LayerRenderer<?>> layerRenderers;

    @Override
    public LayerRenderer<?> npcs$getLayer(Class<?> layerClass) {
        for (LayerRenderer<?> layer : layerRenderers) {
            if (layerClass.isAssignableFrom(layer.getClass())) {
                return layer;
            }
        }
        return null;
    }
}
