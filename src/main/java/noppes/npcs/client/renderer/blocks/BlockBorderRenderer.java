package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.tiles.TileBorder;

import javax.annotation.Nullable;

public class BlockBorderRenderer<T extends TileBorder> extends TileEntitySpecialRenderer<T> {

    public void render(@Nullable T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || !Minecraft.getMinecraft().player.capabilities.isCreativeMode || !CustomNpcs.ShowHitboxBlockTools) { return; }
        AxisAlignedBB aabb = new AxisAlignedBB(-0.5d, 0.5d, -0.5d, 0.5d, 0.5d + te.height, 0.5d);
        AxisAlignedBB aabbPlayer = getAABBPlayerPos(te);

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.translate(x + 0.5d, y + 0.5d, z + 0.5d);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        GlStateManager.glLineWidth(1.5F);
        RenderGlobal.drawSelectionBoundingBox(aabb, 1.0f, 0.15f, 0.15f, 1.0f);
        GlStateManager.glLineWidth(1.0F);
        RenderGlobal.drawSelectionBoundingBox(aabbPlayer, 0.15f, 0.65f, 0.15f, 1.0f);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    private static <T extends TileBorder> AxisAlignedBB getAABBPlayerPos(T te) {
        AxisAlignedBB aabbPlayer = new AxisAlignedBB(-0.35d, 0.65d, -0.35d, 0.35d, 0.35d + te.height, 0.35d);
        if (te.rotation == 2) { aabbPlayer = aabbPlayer.offset(0.0d, 0.0d, 1.0d); }
        else if (te.rotation == 0) { aabbPlayer = aabbPlayer.offset(0.0d, 0.0d, -1.0d); }
        else if (te.rotation == 1) { aabbPlayer = aabbPlayer.offset(1.0d, 0.0d, 0.0d); }
        else if (te.rotation == 3) { aabbPlayer = aabbPlayer.offset(-1.0d, 0.0d, 0.0d); }
        return aabbPlayer;
    }

}
