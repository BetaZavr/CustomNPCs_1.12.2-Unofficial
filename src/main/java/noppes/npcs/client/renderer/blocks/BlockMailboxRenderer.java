package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.blocks.ModelMailboxUS;
import noppes.npcs.client.model.blocks.ModelMailboxWow;

import javax.annotation.Nullable;

public class BlockMailboxRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

	public static final ResourceLocation text1 = new ResourceLocation(CustomNpcs.MODID, "textures/models/mailbox1.png");
	public static final ResourceLocation text2 = new ResourceLocation(CustomNpcs.MODID, "textures/models/mailbox2.png");
	public static final ResourceLocation text3 = new ResourceLocation(CustomNpcs.MODID, "textures/models/mailbox3.png");
	public static final ModelMailboxUS model = new ModelMailboxUS();
	public static final ModelMailboxWow model2 = new ModelMailboxWow();
	private final int type;

	public BlockMailboxRenderer(int i) {
		this.type = i;
	}

	public void render(@Nullable TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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
			model.render(0.0625f);
		}
		if (type == 1) {
			this.bindTexture(BlockMailboxRenderer.text2);
			model2.render(0.0625f);
		}
		if (type == 2) {
			this.bindTexture(BlockMailboxRenderer.text3);
			model2.render(0.0625f);
		}
		GlStateManager.popMatrix();
	}

}
