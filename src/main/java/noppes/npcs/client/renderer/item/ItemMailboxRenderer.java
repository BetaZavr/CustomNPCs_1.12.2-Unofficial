package noppes.npcs.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.renderer.blocks.BlockMailboxRenderer;

import javax.annotation.Nonnull;

public class ItemMailboxRenderer extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(@Nonnull ItemStack stack) {
        int type = stack.getItemDamage() % 3;
        GlStateManager.pushMatrix();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.translate(0.5f, 1.5f, 0.5f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate((90 * type), 0.0f, 1.0f, 0.0f);
        if (type == 0) {
            Minecraft.getMinecraft().renderEngine.bindTexture(BlockMailboxRenderer.text1);
            BlockMailboxRenderer.model.render(0.0625f);
        }
        if (type == 1) {
            Minecraft.getMinecraft().renderEngine.bindTexture(BlockMailboxRenderer.text2);
            BlockMailboxRenderer.model2.render(0.0625f);
        }
        if (type == 2) {
            Minecraft.getMinecraft().renderEngine.bindTexture(BlockMailboxRenderer.text3);
            BlockMailboxRenderer.model2.render(0.0625f);
        }
        GlStateManager.popMatrix();
    }

}
