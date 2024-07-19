package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomRegisters;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.schematics.Schematic;

import javax.annotation.Nullable;

public class BlockCopyRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

	private static final ItemStack item = new ItemStack(CustomRegisters.copy);
	public static BlockPos pos = null;
	public static Schematic schematic = null;

	public void drawSelectionBox(BlockPos pos) {
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		AxisAlignedBB bb = new AxisAlignedBB(BlockPos.ORIGIN, pos);
		GlStateManager.translate(0.001f, 0.001f, 0.001f);
		RenderGlobal.drawSelectionBoundingBox(bb, 1.0f, 0.0f, 0.0f, 1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
	}

	public void render(@Nullable TileEntity te, double x, double y, double z, float var8, int blockDamage, float alpha) {
		if (te == null) { return; }
		TileCopy tile = (TileCopy) te;

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();

		GlStateManager.translate(x, y, z);
		this.drawSelectionBox(new BlockPos(tile.width, tile.height, tile.length));
		GlStateManager.translate(0.5f, 0.5f, 0.5f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
		Minecraft.getMinecraft().getRenderItem().renderItem(BlockCopyRenderer.item,
				ItemCameraTransforms.TransformType.NONE);
		GlStateManager.popMatrix();
	}

}
