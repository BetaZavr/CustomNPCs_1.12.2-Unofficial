package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.tiles.TileRedstoneBlock;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockNpcRedstoneRenderer<T extends TileRedstoneBlock> extends TileEntitySpecialRenderer<T> {

    public void render(@Nullable T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || !Minecraft.getMinecraft().player.capabilities.isCreativeMode || !CustomNpcs.ShowHitboxBlockTools) { return; }
        AxisAlignedBB aabbOn;
        AxisAlignedBB aabbOff;
        if (te.isDetailed) {
            aabbOn = new AxisAlignedBB(-te.onRangeX - 0.5d, -te.onRangeY - 0.5d, -te.onRangeZ - 0.5d, te.onRangeX + 0.5d, te.onRangeY + 0.5d, te.onRangeZ + 0.5d);
            aabbOff = new AxisAlignedBB(-te.offRangeX - 0.5d, -te.offRangeY - 0.5d, -te.offRangeZ - 0.5d, te.offRangeX + 0.5d, te.offRangeY + 0.5d, te.offRangeZ + 0.5d);
        } else {
            double on = te.onRange + 0.5d;
            double off = te.offRange + 0.5d;
            aabbOn = new AxisAlignedBB(-on, -on, -on, on, on, on);
            aabbOff = new AxisAlignedBB(-off, -off, -off, off, off, off);
        }
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
        RenderGlobal.drawSelectionBoundingBox(aabbOn, 1.0f, 0.5f, 0.5f, 1.0f);

        GlStateManager.glLineWidth(1.0F);
        RenderGlobal.drawSelectionBoundingBox(aabbOff, 0.75f, 0.5f, 0.5f, 1.0f);

        drawLinesBetweenCorners(aabbOn, aabbOff);

        drawLinesToCenter(aabbOn);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    private void drawLinesBetweenCorners(AxisAlignedBB aabbOn, AxisAlignedBB aabbOff) {
        GlStateManager.glLineWidth(1.0F);
        List<Vec3d> cornersOn = calculateCorners(aabbOn);
        List<Vec3d> cornersOff = calculateCorners(aabbOff);
        for (int i = 0; i < 8; i++) {
            drawLine(cornersOn.get(i), cornersOff.get(i), 0.75f);
        }
    }

    private void drawLinesToCenter(AxisAlignedBB aabbOn) {
        GlStateManager.glLineWidth(1.5F);
        List<Vec3d> cornersCenter = calculateCorners(new AxisAlignedBB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));
        List<Vec3d> cornersOn = calculateCorners(aabbOn);
        for (int i = 0; i < 8; i++) {
            drawLine(cornersOn.get(i), cornersCenter.get(i), 1.0f);
        }
    }

    private List<Vec3d> calculateCorners(AxisAlignedBB aabb) {
        List<Vec3d> corners = new ArrayList<>();
        corners.add(new Vec3d(aabb.minX, aabb.minY, aabb.minZ));
        corners.add(new Vec3d(aabb.maxX, aabb.minY, aabb.minZ));
        corners.add(new Vec3d(aabb.minX, aabb.maxY, aabb.minZ));
        corners.add(new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ));
        corners.add(new Vec3d(aabb.minX, aabb.minY, aabb.maxZ));
        corners.add(new Vec3d(aabb.maxX, aabb.minY, aabb.maxZ));
        corners.add(new Vec3d(aabb.minX, aabb.maxY, aabb.maxZ));
        corners.add(new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ));
        return corners;
    }

    private void drawLine(Vec3d start, Vec3d end, float red) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(red, 0.5f, 0.5f, 1.0f);
        GL11.glVertex3d(start.x, start.y, start.z);
        GL11.glVertex3d(end.x, end.y, end.z);
        GL11.glEnd();
    }

}
