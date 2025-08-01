package noppes.npcs.mixin.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.api.mixin.client.model.IModelRendererMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModelRenderer.class, priority = 499)
public class ModelRendererMixin implements IModelRendererMixin {

    @Final
    @Shadow
    private ModelBase baseModel;

    @Override
    public ModelBase npcs$getBaseModel() { return baseModel; }

}
