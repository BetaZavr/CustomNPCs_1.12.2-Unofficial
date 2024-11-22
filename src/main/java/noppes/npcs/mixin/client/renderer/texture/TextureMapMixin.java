package noppes.npcs.mixin.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import noppes.npcs.api.mixin.client.renderer.texture.ITextureMapMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = TextureMap.class)
public class TextureMapMixin implements ITextureMapMixin {

    @Final
    @Shadow
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Override
    public Map<String, TextureAtlasSprite> npcs$getMapRegisteredSprites() {
        return mapRegisteredSprites;
    }

}
