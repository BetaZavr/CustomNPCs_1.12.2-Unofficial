package noppes.npcs.mixin.api.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = TextureManager.class)
public interface TextureManagerAPIMixin {

    @Accessor(value="mapTextureObjects")
    Map<ResourceLocation, ITextureObject> npcs$getMapTextureObjects();

}
