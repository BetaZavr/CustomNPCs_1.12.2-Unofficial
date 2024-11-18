package noppes.npcs.mixin.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import noppes.npcs.api.mixin.client.model.IModelBoxMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModelBox.class)
public class ModelBoxMixin implements IModelBoxMixin {

    @Final
    @Shadow
    private PositionTextureVertex[] vertexPositions;

    @Mutable
    @Final
    @Shadow
    protected TexturedQuad[] quadList;


    @Override
    public PositionTextureVertex[] npcs$getVertexPositions() { return vertexPositions; }

    @Override
    public void npcs$setQuadList(TexturedQuad[] newQuadList) {
        if (newQuadList == null) { return; }
        quadList = newQuadList;
    }
}
