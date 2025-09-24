package noppes.npcs.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.LogWriter;
import noppes.npcs.api.mixin.client.renderer.entity.IRenderLivingBaseMixin;
import noppes.npcs.entity.EntityCustomNpc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = RenderLivingBase.class, priority = 499)
public class RenderLivingBaseMixin<T extends EntityLivingBase> implements IRenderLivingBaseMixin {

    @Shadow
    protected List<LayerRenderer<T>> layerRenderers;

    @Override
    public LayerRenderer<T> npcs$getLayer(Class<?> layerClass) {
        for (LayerRenderer<T> layer : layerRenderers) {
            if (layerClass.isAssignableFrom(layer.getClass())) {
                return layer;
            }
        }
        return null;
    }

    @Override
    public List<LayerRenderer<?>> npcs$getLayers() { return new ArrayList<>(layerRenderers); }

}
