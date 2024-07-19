package noppes.npcs.client.renderer.blocks;

import java.nio.FloatBuffer;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import noppes.npcs.blocks.tiles.CustomTileEntityPortal;

import javax.annotation.Nullable;

public class BlockPortalRenderer<T extends CustomTileEntityPortal> extends TileEntitySpecialRenderer<T> {

	private static final Random RANDOM = new Random(31100L);
	private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
	private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
	private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

	private FloatBuffer getBuffer(float red, float green, float blue) {
		this.buffer.clear();
		this.buffer.put(red).put(green).put(blue).put((float) 0.0);
		this.buffer.flip();
		return this.buffer;
	}

	protected int getPasses(double distance) {
		int i;

		if (distance > 36864.0D) {
			i = 1;
		} else if (distance > 25600.0D) {
			i = 3;
		} else if (distance > 16384.0D) {
			i = 5;
		} else if (distance > 9216.0D) {
			i = 7;
		} else if (distance > 4096.0D) {
			i = 9;
		} else if (distance > 1024.0D) {
			i = 11;
		} else if (distance > 576.0D) {
			i = 13;
		} else if (distance > 256.0D) {
			i = 14;
		} else {
			i = 15;
		}
		return i;
	}

	public void render(@Nullable T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te == null) { return; }
		GlStateManager.disableLighting();
		RANDOM.setSeed(31100L);
		GlStateManager.getFloat(2982, MODELVIEW);
		GlStateManager.getFloat(2983, PROJECTION);
		int i = this.getPasses(x * x + y * y + z * z); // i == layers
		boolean isPortalTexture = false;

		for (int j = 0; j < i; ++j) { // j == layer
			GlStateManager.pushMatrix();
			float f1 = (1.25f / (float) i * (float) j + 0.5f) / (float) (18 - j);
			if (j == 0) {
				this.bindTexture(te.getSkyTexture());
				f1 = 5.0f;
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			}
			if (j >= 1) {
				this.bindTexture(te.getPortalTexture());
				isPortalTexture = true;
				Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
			}
			if (j == 1) {
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			}

			GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
			GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
			GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
			GlStateManager.texGen(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F));
			GlStateManager.texGen(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F));
			GlStateManager.texGen(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F));
			GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
			GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
			GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
			GlStateManager.popMatrix();

			GlStateManager.matrixMode(5890);

			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.5F, 0.5F, 0.0F);
			GlStateManager.scale(0.5F, 0.5F, 1.0F);
			float f2 = (float) (j + 1);
			float t0 = te.speed * 1000.0F;
			GlStateManager.translate(17.0F / f2, (2.0F + f2 / 1.5F) * ((float) Minecraft.getSystemTime() % t0 / t0),
					0.0F);
			GlStateManager.rotate((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.scale(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
			GlStateManager.multMatrix(PROJECTION);
			GlStateManager.multMatrix(MODELVIEW);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
			float f3 = (RANDOM.nextFloat() * 0.5F + 0.1F) * f1;
			float f4 = (RANDOM.nextFloat() * 0.5F + 0.4F) * f1;
			float f5 = (RANDOM.nextFloat() * 0.5F + 0.5F) * f1;
			float f6 = te.alpha;
			if (f6 < 0.15f) {
				f6 = 0.15f;
			} else if (f6 > 1.0f) {
				f6 = 1.0f;
			}

			if (te.shouldRenderFace(EnumFacing.SOUTH)) {
				bufferbuilder.pos(x, y, z + 0.75D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z + 0.75D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 0.75D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x, y + 1.0D, z + 0.75D).color(f3, f4, f5, f6).endVertex();
			}
			if (te.shouldRenderFace(EnumFacing.NORTH)) {
				bufferbuilder.pos(x, y + 1.0D, z + 0.25D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 0.25D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z + 0.25D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x, y, z + 0.25D).color(f3, f4, f5, f6).endVertex();
			}
			if (te.shouldRenderFace(EnumFacing.EAST)) {
				bufferbuilder.pos(x + 0.75D, y + 1.0D, z).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 0.75D, y + 1.0D, z + 1.0D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 0.75D, y, z + 1.0D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 0.75D, y, z).color(f3, f4, f5, f6).endVertex();
			}
			if (te.shouldRenderFace(EnumFacing.WEST)) {
				bufferbuilder.pos(x + 0.25D, y, z).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 0.25D, y, z + 1.0D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 0.25D, y + 1.0D, z + 1.0D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 0.25D, y + 1.0D, z).color(f3, f4, f5, f6).endVertex();
			}
			if (te.shouldRenderFace(EnumFacing.DOWN)) {
				bufferbuilder.pos(x, y + 0.25d, z).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 0.25d, z).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 0.25d, z + 1.0D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x, y + 0.25d, z + 1.0D).color(f3, f4, f5, f6).endVertex();
			}
			if (te.shouldRenderFace(EnumFacing.UP)) {
				bufferbuilder.pos(x, y + 0.75d, z + 1.0D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 0.75d, z + 1.0D).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 0.75d, z).color(f3, f4, f5, f6).endVertex();
				bufferbuilder.pos(x, y + 0.75d, z).color(f3, f4, f5, f6).endVertex();
			}
			tessellator.draw();
			GlStateManager.popMatrix();

			GlStateManager.matrixMode(5888);
			this.bindTexture(te.getSkyTexture());
		}
		GlStateManager.disableBlend();
		GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
		GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
		GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
		GlStateManager.enableLighting();
		if (isPortalTexture) {
			Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
		}
	}

}
