package noppes.npcs.mixin.api.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityItemStackRenderer.class)
public interface TileEntityItemStackRendererAPIMixin {

    @Mutable
    @Accessor(value="banner")
    void npcs$setBanner(TileEntityBanner newBanner);

}
