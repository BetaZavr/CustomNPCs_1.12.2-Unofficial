package noppes.npcs.mixin.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.api.mixin.client.model.IModelPlayerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModelPlayer.class)
public class ModelPlayerMixin implements IModelPlayerMixin {

    @Mutable
    @Final
    @Shadow
    private ModelRenderer bipedCape;

    @Override
    public void npcs$setBipedCape(ModelRenderer newBipedCape) {
        if (newBipedCape == null) { return; }
        bipedCape = newBipedCape;
    }
}
