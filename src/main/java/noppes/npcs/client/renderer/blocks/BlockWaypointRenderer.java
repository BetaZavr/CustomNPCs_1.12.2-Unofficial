package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.blocks.tiles.TileWaypoint;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockWaypointRenderer<T extends TileWaypoint> extends TileEntitySpecialRenderer<T> {

    public void render(@Nullable T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || !Minecraft.getMinecraft().player.capabilities.isCreativeMode) { return; }
        double range = te.range + 0.5d;
        AxisAlignedBB aabb = new AxisAlignedBB(-range, -range, -range, range, range, range);
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
        RenderGlobal.drawSelectionBoundingBox(aabb, 0.25f, 0.65f, 0.25f, 1.0f);

        drawLinesToCenter(aabb);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    private void drawLinesToCenter(AxisAlignedBB aabbOn) {
        List<Vec3d> cornersCenter = calculateCorners(new AxisAlignedBB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));
        List<Vec3d> cornersOn = calculateCorners(aabbOn);
        for (int i = 0; i < 8; i++) {
            drawLine(cornersOn.get(i), cornersCenter.get(i));
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

    private void drawLine(Vec3d start, Vec3d end) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(0.25f, 0.65f, 0.25f, 1.0f);
        GL11.glVertex3d(start.x, start.y, start.z);
        GL11.glVertex3d(end.x, end.y, end.z);
        GL11.glEnd();
    }

}
