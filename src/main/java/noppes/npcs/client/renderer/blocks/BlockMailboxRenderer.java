package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.blocks.ModelMailboxUS;
import noppes.npcs.client.model.blocks.ModelMailboxWow;

public class BlockMailboxRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

	private static ResourceLocation text1 = new ResourceLocation(CustomNpcs.MODID, "textures/models/mailbox1.png");
	private static ResourceLocation text2 = new ResourceLocation(CustomNpcs.MODID, "textures/models/mailbox2.png");
	private static ResourceLocation text3 = new ResourceLocation(CustomNpcs.MODID, "textures/models/mailbox3.png");;
	private ModelMailboxUS model;;
	private ModelMailboxWow model2;;
	private int type;

	public BlockMailboxRenderer(int i) {
		this.model = new ModelMailboxUS();
		this.model2 = new ModelMailboxWow();
		this.type = i;
	}

	public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		int meta = 0;
		int type = this.type;
		if (te != null && te.getPos() != BlockPos.ORIGIN) {
			meta = (te.getBlockMetadata() | 0x4);
			type = te.getBlockMetadata() >> 2;
		}
		GlStateManager.pushMatrix();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.translate(x + 0.5f, y + 1.5f, z + 0.5f);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		GlStateManager.rotate((90 * meta), 0.0f, 1.0f, 0.0f);
		if (type == 0) {
			this.bindTexture(BlockMailboxRenderer.text1);
			this.model.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
		}
		if (type == 1) {
			this.bindTexture(BlockMailboxRenderer.text2);
			this.model2.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
		}
		if (type == 2) {
			this.bindTexture(BlockMailboxRenderer.text3);
			this.model2.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
		}
		GlStateManager.popMatrix();
	}

}
