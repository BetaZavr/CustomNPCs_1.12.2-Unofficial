package noppes.npcs.mixin.impl.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.tileentity.TileEntityBanner;
import noppes.npcs.mixin.client.renderer.tileentity.ITileEntityItemStackRendererMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TileEntityItemStackRenderer.class)
public class TileEntityItemStackRendererMixin implements ITileEntityItemStackRendererMixin {

    @Mutable
    @Final
    @Shadow
    private TileEntityBanner banner;

    @Override
    public void npcs$setBanner(TileEntityBanner newBanner) {
        if (newBanner == null) { return; }
        banner = newBanner;
    }

}
