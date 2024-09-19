package noppes.npcs.mixin.api.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ModelRenderer.class)
public interface ModelRendererAPIMixin {

    @Accessor(value="baseModel")
    ModelBase npcs$getBaseModel();

}
