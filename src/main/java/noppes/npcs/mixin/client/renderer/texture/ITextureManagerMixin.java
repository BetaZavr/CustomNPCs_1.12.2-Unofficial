package noppes.npcs.mixin.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public interface ITextureManagerMixin {

    Map<ResourceLocation, ITextureObject> npcs$getMapTextureObjects();

}
