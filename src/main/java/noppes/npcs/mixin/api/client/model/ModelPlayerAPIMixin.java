package noppes.npcs.mixin.api.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ModelPlayer.class)
public interface ModelPlayerAPIMixin {

    @Accessor(value="bipedCape")
    void npcs$setBipedCape(ModelRenderer newBipedCape);

}
