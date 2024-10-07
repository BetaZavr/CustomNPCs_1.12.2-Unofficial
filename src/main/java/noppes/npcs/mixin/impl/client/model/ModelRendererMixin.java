package noppes.npcs.mixin.impl.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.mixin.client.model.IModelRendererMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModelRenderer.class)
public class ModelRendererMixin implements IModelRendererMixin {

    @Final
    @Shadow(aliases = "baseModel")
    private ModelBase baseModel;

    @Override
    public ModelBase npcs$getBaseModel() { return baseModel; }

}
