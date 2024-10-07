package noppes.npcs.mixin.impl.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.mixin.client.renderer.texture.ITextureManagerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = TextureManager.class)
public class TextureManagerMixin implements ITextureManagerMixin {

    @Final
    @Shadow(aliases = "mapTextureObjects")
    private Map<ResourceLocation, ITextureObject> mapTextureObjects;

    @Override
    public Map<ResourceLocation, ITextureObject> npcs$getMapTextureObjects() { return mapTextureObjects; }

}
