package noppes.npcs.api.mixin.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Map;

public interface ITextureMapMixin {

    Map<String, TextureAtlasSprite> npcs$getMapRegisteredSprites();

}
