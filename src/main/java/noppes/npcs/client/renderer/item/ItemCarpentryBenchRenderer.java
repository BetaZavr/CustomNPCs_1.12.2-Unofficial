package noppes.npcs.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.renderer.blocks.BlockCarpentryBenchRenderer;

import javax.annotation.Nonnull;

public class ItemCarpentryBenchRenderer extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(@Nonnull ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.translate(0.5f, 1.4f, 0.5f);
        GlStateManager.scale(0.95f, 0.95f, 0.95f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(BlockCarpentryBenchRenderer.TEXTURE);
        BlockCarpentryBenchRenderer.model.render(0.0625f);
        GlStateManager.popMatrix();
    }

}
