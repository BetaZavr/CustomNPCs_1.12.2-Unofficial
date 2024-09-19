package noppes.npcs.mixin.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.tileentity.TileEntityBanner;
import noppes.npcs.mixin.api.client.renderer.tileentity.TileEntityItemStackRendererAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TileEntityItemStackRenderer.class)
public class TileEntityItemStackRendererMixin implements TileEntityItemStackRendererAPIMixin {

    @Mutable
    @Final
    @Shadow(aliases = "banner")
    private TileEntityBanner banner;

    @Override
    public void npcs$setBanner(TileEntityBanner newBanner) {
        if (newBanner == null) { return; }
        banner = newBanner;
    }

}
