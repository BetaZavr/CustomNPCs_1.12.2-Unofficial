package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import noppes.npcs.blocks.tiles.CustomTileEntityChest;

import javax.annotation.Nullable;

public class BlockChestRenderer<T extends CustomTileEntityChest> extends TileEntitySpecialRenderer<T> {

	private final ModelChest modelChest = new ModelChest();

	public void render(@Nullable CustomTileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te == null || !te.isChest || te.chestTexture == null) {
			return;
		}
		int i = 0;
		if (te.hasWorld()) {
			i = te.getBlockMetadata();
		}
		if (destroyStage >= 0) { // add destroyStage
			this.bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4.0F, 4.0F, 1.0F);
			GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		} else {
			this.bindTexture(te.chestTexture);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		GlStateManager.translate((float) x, (float) y + 1.0F, (float) z + 1.0F);
		GlStateManager.scale(1.0F, -1.0F, -1.0F);
		GlStateManager.translate(0.5F, 0.5F, 0.5F);
		int j = 0;
		if (i == 2) {
			j = 180;
		}
        if (i == 4) {
			j = 90;
		}
		if (i == 5) {
			j = -90;
		}
		GlStateManager.rotate((float) j, 0.0F, 1.0F, 0.0F); // main place
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);

		float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
		f = 1.0F - f;
		f = 1.0F - f * f * f;
		this.modelChest.chestLid.rotateAngleX = -(f * ((float) Math.PI / 2F)); // top angle
		this.modelChest.renderAll();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if (destroyStage >= 0) { // end destroyStage
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}

}
