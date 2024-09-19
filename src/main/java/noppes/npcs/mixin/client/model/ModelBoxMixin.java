package noppes.npcs.mixin.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import noppes.npcs.mixin.api.client.model.ModelBoxAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModelBox.class)
public class ModelBoxMixin implements ModelBoxAPIMixin {

    @Final
    @Shadow(aliases = "vertexPositions")
    private PositionTextureVertex[] vertexPositions;

    @Mutable
    @Final
    @Shadow(aliases = "quadList")
    protected TexturedQuad[] quadList;


    @Override
    public PositionTextureVertex[] npcs$getVertexPositions() { return vertexPositions; }

    @Override
    public void npcs$setQuadList(TexturedQuad[] newQuadList) {
        if (newQuadList == null) { return; }
        quadList = newQuadList;
    }
}
