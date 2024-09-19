package noppes.npcs.mixin.api.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ModelBox.class)
public interface ModelBoxAPIMixin {

    @Accessor(value="vertexPositions")
    PositionTextureVertex[] npcs$getVertexPositions();

    @Mutable
    @Accessor(value="quadList")
    void npcs$setQuadList(TexturedQuad[] newQuadList);

}
