package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.util.ObfuscationHelper;

public class BlockScriptedRenderer<T extends TileEntity>
extends BlockRendererInterface<T> {

	private void drawText(TileScripted.TextPlane text1, double x, double y, double z) {
		if (text1.textBlock == null || text1.textHasChanged) {
			text1.textBlock = new TextBlockClient(text1.text, 336, true,
					new Object[] { Minecraft.getMinecraft().player });
			text1.textHasChanged = false;
		}
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.rotate(text1.rotationY, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(text1.rotationX, 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(text1.rotationZ, 0.0f, 0.0f, 1.0f);
		GlStateManager.scale(text1.scale, text1.scale, 1.0f);
		GlStateManager.translate(text1.offsetX, text1.offsetY, text1.offsetZ);
		float f1 = 0.6666667f;
		float f2 = 0.0133f * f1;
		GlStateManager.translate(0.0f, 0.5f, 0.01f);
		GlStateManager.scale(f2, -f2, f2);
		GlStateManager.glNormal3f(0.0f, 0.0f, -1.0f * f2);
		GlStateManager.depthMask(false);
		FontRenderer fontrenderer = this.getFontRenderer();
		float lineOffset = 0.0f;
		if (text1.textBlock.lines.size() < 14) {
			lineOffset = (14.0f - text1.textBlock.lines.size()) / 2.0f;
		}
		for (int i = 0; i < text1.textBlock.lines.size(); ++i) {
			String text2 = text1.textBlock.lines.get(i).getFormattedText();
			fontrenderer.drawString(text2, -fontrenderer.getStringWidth(text2) / 2,
					(int) ((lineOffset + i) * (fontrenderer.FONT_HEIGHT - 0.3)), 0);
		}
		GlStateManager.depthMask(true);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();
	}

	private boolean overrideModel() {
		ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
		return held != null && (held.getItem() == CustomItems.wand || held.getItem() == CustomItems.scripter);
	}
	
	public void render(TileEntity te, double x, double y, double z, float partialTicks, int blockDamage, float alpha) {
		TileScripted tile = (TileScripted) te;
		GlStateManager.pushMatrix();
		GlStateManager.disableBlend();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		if (this.overrideModel()) { // Default model
			GlStateManager.translate(0.0, 0.5, 0.0);
			this.renderItem(new ItemStack(CustomItems.scripted));
		} else {
			// Custom model
			GlStateManager.rotate(tile.rotationY, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(tile.rotationX, 1.0f, 0.0f, 0.0f);
			GlStateManager.rotate(tile.rotationZ, 0.0f, 0.0f, 1.0f);
			GlStateManager.scale(tile.scaleX, tile.scaleY, tile.scaleZ);
			Block b = tile.blockModel;
			if (b == null || b == Blocks.AIR || b == CustomItems.scripted) {
				GlStateManager.translate(0.0, 0.5, 0.0);
				this.renderItem(tile.itemModel); // Default model
			} else {
				IBlockState state = tile.getState();
				this.renderBlock(tile, b, state);
				if (b.hasTileEntity(state) && !tile.renderTileErrored) {
					try {
						if (tile.renderTile == null) {
							TileEntity entity = b.createTileEntity(this.getWorld(), state);
							entity.setPos(tile.getPos());
							entity.setWorld(this.getWorld());
							ObfuscationHelper.setValue(TileEntity.class, entity, tile.metaModel, 5);
							ObfuscationHelper.setValue(TileEntity.class, entity, b, 6);
							tile.renderTile = entity;
							if (entity instanceof ITickable) {
								tile.renderTileUpdate = (ITickable) entity;
							}
						}
						TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance
								.getRenderer(tile.renderTile);
						if (renderer != null) {
							renderer.render(tile.renderTile, -0.5, 0.0, -0.5, partialTicks, blockDamage, alpha);
						} else {
							tile.renderTileErrored = true;
						}
					} catch (Exception e) {
						tile.renderTileErrored = true;
					}
				}
			}
		}
		GlStateManager.popMatrix();
		if (!tile.text1.text.isEmpty()) {
			this.drawText(tile.text1, x, y, z);
		}
		if (!tile.text2.text.isEmpty()) {
			this.drawText(tile.text2, x, y, z);
		}
		if (!tile.text3.text.isEmpty()) {
			this.drawText(tile.text3, x, y, z);
		}
		if (!tile.text4.text.isEmpty()) {
			this.drawText(tile.text4, x, y, z);
		}
		if (!tile.text5.text.isEmpty()) {
			this.drawText(tile.text5, x, y, z);
		}
		if (!tile.text6.text.isEmpty()) {
			this.drawText(tile.text6, x, y, z);
		}
	}

	private void renderBlock(TileScripted tile, Block b, IBlockState state) {
		
		GlStateManager.pushMatrix();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.translate(-0.5f, 0.0f, 0.5f);
		
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, 1.0f);
		RenderHelper.enableStandardItemLighting();
		
		GlStateManager.popMatrix();
	}

	private void renderItem(ItemStack item) {
		Minecraft.getMinecraft().getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.NONE);
	}

}
