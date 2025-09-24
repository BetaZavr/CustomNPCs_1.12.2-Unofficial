package noppes.npcs.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerWitherAura;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.entity.EntityCustomNpc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = RenderLivingBase.class, priority = 499)
public abstract class RenderLivingBaseLayersMixin<T extends EntityLivingBase> {

    @Shadow
    protected List<LayerRenderer<T>> layerRenderers;

    @Final
    @Unique
    protected Map<T, List<LayerRenderer<T>>> npcs$backLayers = new HashMap<>();


    @Inject(method = "renderLayers", at = @At("HEAD"))
    private void npcs$preRenderLayers(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn, CallbackInfo ci) {
        npcs$backLayers.remove(entityIn);
        if (!entityIn.world.loadedEntityList.contains(entityIn)) {
            for (Entity entity : new ArrayList<>(entityIn.world.loadedEntityList)) {
                if (entity instanceof EntityCustomNpc && entity.getPosition().equals(entity.getPosition())) {
                    if (((EntityCustomNpc) entity).modelData != null &&
                            ((EntityCustomNpc) entity).modelData.getEntityClass() == entityIn.getClass() &&
                            ((EntityCustomNpc) entity).modelData.hasDisableLayers()) {
                        npcs$backLayers.put(entityIn, new ArrayList<>());
                        for (LayerRenderer<T> layerrenderer : new ArrayList<>(layerRenderers)) {
                            String layerName = layerrenderer.getClass().getSimpleName();
                            if (((EntityCustomNpc) entity).modelData.isDisableLayer(layerName)) {
                                npcs$backLayers.get(entityIn).add(layerrenderer);
                                layerRenderers.remove(layerrenderer);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    @Inject(method = "renderLayers", at = @At("TAIL"))
    private void npcs$postRenderLayers(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn, CallbackInfo ci) {
        if (npcs$backLayers.containsKey(entityIn)) {
            layerRenderers.addAll(npcs$backLayers.get(entityIn));
            npcs$backLayers.remove(entityIn);
        }
    }

}