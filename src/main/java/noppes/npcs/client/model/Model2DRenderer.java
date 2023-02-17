package noppes.npcs.client.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Model2DRenderer extends ModelRenderer {
	public static void renderItemIn2D(BufferBuilder worldrenderer, float minU, float maxU, float minV, float maxV,
			int sheetWidth, int sheetHeight, float scale) {
		Tessellator tessellator = Tessellator.getInstance();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		worldrenderer.pos(0.0, 0.0, 0.0).tex((double) minU, (double) maxV).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(1.0, 0.0, 0.0).tex((double) minV, (double) maxV).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(1.0, 1.0, 0.0).tex((double) minV, (double) maxU).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(0.0, 1.0, 0.0).tex((double) minU, (double) maxU).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(0.0, 1.0, (double) (0.0f - scale)).tex((double) minU, (double) maxU).normal(0.0f, 0.0f, -1.0f)
				.endVertex();
		worldrenderer.pos(1.0, 1.0, (double) (0.0f - scale)).tex((double) minV, (double) maxU).normal(0.0f, 0.0f, -1.0f)
				.endVertex();
		worldrenderer.pos(1.0, 0.0, (double) (0.0f - scale)).tex((double) minV, (double) maxV).normal(0.0f, 0.0f, -1.0f)
				.endVertex();
		worldrenderer.pos(0.0, 0.0, (double) (0.0f - scale)).tex((double) minU, (double) maxV).normal(0.0f, 0.0f, -1.0f)
				.endVertex();
		float f5 = 0.5f * (minU - minV) / sheetWidth;
		float f6 = 0.5f * (maxV - maxU) / sheetHeight;
		for (int k = 0; k < sheetWidth; ++k) {
			float f7 = k / (float) sheetWidth;
			float f8 = minU + (minV - minU) * f7 - f5;
			worldrenderer.pos((double) f7, 0.0, (double) (0.0f - scale)).tex((double) f8, (double) maxV)
					.normal(-1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos((double) f7, 0.0, 0.0).tex((double) f8, (double) maxV).normal(-1.0f, 0.0f, 0.0f)
					.endVertex();
			worldrenderer.pos((double) f7, 1.0, 0.0).tex((double) f8, (double) maxU).normal(-1.0f, 0.0f, 0.0f)
					.endVertex();
			worldrenderer.pos((double) f7, 1.0, (double) (0.0f - scale)).tex((double) f8, (double) maxU)
					.normal(-1.0f, 0.0f, 0.0f).endVertex();
		}
		for (int k = 0; k < sheetWidth; ++k) {
			float f7 = k / (float) sheetWidth;
			float f8 = minU + (minV - minU) * f7 - f5;
			float f9 = f7 + 1.0f / sheetWidth;
			worldrenderer.pos((double) f9, 1.0, (double) (0.0f - scale)).tex((double) f8, (double) maxU)
					.normal(1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos((double) f9, 1.0, 0.0).tex((double) f8, (double) maxU).normal(1.0f, 0.0f, 0.0f)
					.endVertex();
			worldrenderer.pos((double) f9, 0.0, 0.0).tex((double) f8, (double) maxV).normal(1.0f, 0.0f, 0.0f)
					.endVertex();
			worldrenderer.pos((double) f9, 0.0, (double) (0.0f - scale)).tex((double) f8, (double) maxV)
					.normal(1.0f, 0.0f, 0.0f).endVertex();
		}
		for (int k = 0; k < sheetHeight; ++k) {
			float f7 = k / (float) sheetHeight;
			float f8 = maxV + (maxU - maxV) * f7 - f6;
			float f9 = f7 + 1.0f / sheetHeight;
			worldrenderer.pos(0.0, (double) f9, 0.0).tex((double) minU, (double) f8).normal(0.0f, 1.0f, 0.0f)
					.endVertex();
			worldrenderer.pos(1.0, (double) f9, 0.0).tex((double) minV, (double) f8).normal(0.0f, 1.0f, 0.0f)
					.endVertex();
			worldrenderer.pos(1.0, (double) f9, (double) (0.0f - scale)).tex((double) minV, (double) f8)
					.normal(0.0f, 1.0f, 0.0f).endVertex();
			worldrenderer.pos(0.0, (double) f9, (double) (0.0f - scale)).tex((double) minU, (double) f8)
					.normal(0.0f, 1.0f, 0.0f).endVertex();
		}
		for (int k = 0; k < sheetHeight; ++k) {
			float f7 = k / (float) sheetHeight;
			float f8 = maxV + (maxU - maxV) * f7 - f6;
			worldrenderer.pos(1.0, (double) f7, 0.0).tex((double) minV, (double) f8).normal(0.0f, -1.0f, 0.0f)
					.endVertex();
			worldrenderer.pos(0.0, (double) f7, 0.0).tex((double) minU, (double) f8).normal(0.0f, -1.0f, 0.0f)
					.endVertex();
			worldrenderer.pos(0.0, (double) f7, (double) (0.0f - scale)).tex((double) minU, (double) f8)
					.normal(0.0f, -1.0f, 0.0f).endVertex();
			worldrenderer.pos(1.0, (double) f7, (double) (0.0f - scale)).tex((double) minV, (double) f8)
					.normal(0.0f, -1.0f, 0.0f).endVertex();
		}
		tessellator.draw();
	}

	private int displayList;
	private int height;
	private boolean isCompiled;
	private float rotationOffsetX;
	private float rotationOffsetY;
	private float rotationOffsetZ;
	private float scaleX;
	private float scaleY;
	private float thickness;
	private int width;
	private float x1;
	private float x2;
	private float y1;

	private float y2;

	public Model2DRenderer(ModelBase modelBase, float x, float y, int width, int height) {
		this(modelBase, x, y, width, height, modelBase.textureWidth, modelBase.textureHeight);
	}

	public Model2DRenderer(ModelBase modelBase, float x, float y, int width, int height, int textureWidth,
			int textureHeight) {
		super(modelBase);
		this.scaleX = 1.0f;
		this.scaleY = 1.0f;
		this.thickness = 1.0f;
		this.width = width;
		this.height = height;
		this.textureWidth = (float) textureWidth;
		this.textureHeight = (float) textureHeight;
		this.x1 = x / textureWidth;
		this.y1 = y / textureHeight;
		this.x2 = (x + width) / textureWidth;
		this.y2 = (y + height) / textureHeight;
	}

	@SideOnly(Side.CLIENT)
	private void compile(float scale) {
		GlStateManager.glNewList(this.displayList = GLAllocation.generateDisplayLists(1), 4864);
		GlStateManager.translate(this.rotationOffsetX * scale, this.rotationOffsetY * scale,
				this.rotationOffsetZ * scale);
		GlStateManager.scale(this.scaleX * this.width / this.height, this.scaleY, this.thickness);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		if (this.mirror) {
			GlStateManager.translate(0.0f, 0.0f, -1.0f * scale);
			GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
		}
		renderItemIn2D(Tessellator.getInstance().getBuffer(), this.x1, this.y1, this.x2, this.y2, this.width,
				this.height, scale);
		GL11.glEndList();
		this.isCompiled = true;
	}

	public void render(float scale) {
		if (!this.showModel || this.isHidden) {
			return;
		}
		if (!this.isCompiled) {
			this.compile(scale);
		}
		GlStateManager.pushMatrix();
		this.postRender(scale);
		GlStateManager.callList(this.displayList);
		GlStateManager.popMatrix();
	}

	public void setRotationOffset(float x, float y, float z) {
		this.rotationOffsetX = x;
		this.rotationOffsetY = y;
		this.rotationOffsetZ = z;
	}

	public void setScale(float scale) {
		this.scaleX = scale;
		this.scaleY = scale;
	}

	public void setScale(float x, float y) {
		this.scaleX = x;
		this.scaleY = y;
	}

	public void setThickness(float thickness) {
		this.thickness = thickness;
	}
}
