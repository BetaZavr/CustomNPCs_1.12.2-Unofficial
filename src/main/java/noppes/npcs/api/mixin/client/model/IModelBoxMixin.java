package noppes.npcs.api.mixin.client.model;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;

public interface IModelBoxMixin {

    PositionTextureVertex[] npcs$getVertexPositions();

    void npcs$setQuadList(TexturedQuad[] newQuadList);

}
