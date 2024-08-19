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
	
	public static void renderItemIn2D(BufferBuilder worldrenderer, float minU, float maxU, float minV, float maxV, int sheetWidth, int sheetHeight, float scale) {
		Tessellator tessellator = Tessellator.getInstance();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		worldrenderer.pos(0.0, 0.0, 0.0).tex(minU, maxV).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(1.0, 0.0, 0.0).tex(minV, maxV).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(1.0, 1.0, 0.0).tex(minV, maxU).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(0.0, 1.0, 0.0).tex(minU, maxU).normal(0.0f, 0.0f, 1.0f).endVertex();
		worldrenderer.pos(0.0, 1.0, 0.0f - scale).tex(minU, maxU).normal(0.0f, 0.0f, -1.0f).endVertex();
		worldrenderer.pos(1.0, 1.0, 0.0f - scale).tex(minV, maxU).normal(0.0f, 0.0f, -1.0f).endVertex();
		worldrenderer.pos(1.0, 0.0, 0.0f - scale).tex(minV, maxV).normal(0.0f, 0.0f, -1.0f).endVertex();
		worldrenderer.pos(0.0, 0.0, 0.0f - scale).tex(minU, maxV).normal(0.0f, 0.0f, -1.0f).endVertex();
		
		float f5 = 0.5f * (minU - minV) / sheetWidth;
		float f6 = 0.5f * (maxV - maxU) / sheetHeight;
		for (int k = 0; k < sheetWidth; ++k) {
			float f7 = k / (float) sheetWidth;
			float f8 = minU + (minV - minU) * f7 - f5;
			worldrenderer.pos(f7, 0.0, 0.0f - scale).tex(f8, maxV).normal(-1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos(f7, 0.0, 0.0).tex(f8, maxV).normal(-1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos(f7, 1.0, 0.0).tex(f8, maxU).normal(-1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos(f7, 1.0, 0.0f - scale).tex(f8, maxU).normal(-1.0f, 0.0f, 0.0f).endVertex();
		}
		for (int k = 0; k < sheetWidth; ++k) {
			float f7 = k / (float) sheetWidth;
			float f8 = minU + (minV - minU) * f7 - f5;
			float f9 = f7 + 1.0f / sheetWidth;
			worldrenderer.pos(f9, 1.0, 0.0f - scale).tex(f8, maxU).normal(1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos(f9, 1.0, 0.0).tex(f8, maxU).normal(1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos(f9, 0.0, 0.0).tex(f8, maxV).normal(1.0f, 0.0f, 0.0f).endVertex();
			worldrenderer.pos(f9, 0.0, 0.0f - scale).tex(f8, maxV).normal(1.0f, 0.0f, 0.0f).endVertex();
		}
		for (int k = 0; k < sheetHeight; ++k) {
			float f7 = k / (float) sheetHeight;
			float f8 = maxV + (maxU - maxV) * f7 - f6;
			float f9 = f7 + 1.0f / sheetHeight;
			worldrenderer.pos(0.0, f9, 0.0).tex(minU, f8).normal(0.0f, 1.0f, 0.0f).endVertex();
			worldrenderer.pos(1.0, f9, 0.0).tex(minV, f8).normal(0.0f, 1.0f, 0.0f).endVertex();
			worldrenderer.pos(1.0, f9, 0.0f - scale).tex(minV, f8).normal(0.0f, 1.0f, 0.0f).endVertex();
			worldrenderer.pos(0.0, f9, 0.0f - scale).tex(minU, f8).normal(0.0f, 1.0f, 0.0f).endVertex();
		}
		for (int k = 0; k < sheetHeight; ++k) {
			float f7 = k / (float) sheetHeight;
			float f8 = maxV + (maxU - maxV) * f7 - f6;
			worldrenderer.pos(1.0, f7, 0.0).tex(minV, f8).normal(0.0f, -1.0f, 0.0f).endVertex();
			worldrenderer.pos(0.0, f7, 0.0).tex(minU, f8).normal(0.0f, -1.0f, 0.0f) .endVertex();
			worldrenderer.pos(0.0, f7, 0.0f - scale).tex(minU, f8).normal(0.0f, -1.0f, 0.0f).endVertex();
			worldrenderer.pos(1.0, f7, 0.0f - scale).tex(minV, f8).normal(0.0f, -1.0f, 0.0f).endVertex();
		}
		tessellator.draw();
	}

	private int displayList;
	private final int height;
	private boolean isCompiled;
	private float rotationOffsetX;
	private float rotationOffsetY;
	private float rotationOffsetZ;
	private float scaleX;
	private float scaleY;
	private float thickness;
	private final int width;
	private final float minU;
	private final float maxU;
	private final float minV;
	private final float maxV;

	public Model2DRenderer(ModelBase modelBase, float x, float y, int width, int height) {
		this(modelBase, x, y, width, height, modelBase.textureWidth, modelBase.textureHeight);
	}

	public Model2DRenderer(ModelBase modelBase, float x, float y, int width, int height, int textureWidth, int textureHeight) {
		super(modelBase);
		this.scaleX = 1.0f;
		this.scaleY = 1.0f;
		this.thickness = 1.0f;
		this.width = width;
		this.height = height;
		this.textureWidth = (float) textureWidth;
		this.textureHeight = (float) textureHeight;
		this.minU = x / textureWidth;
		this.minV = y / textureHeight;
		this.maxU = (x + width) / textureWidth;
		this.maxV = (y + height) / textureHeight;
	}

	@SideOnly(Side.CLIENT)
	private void compile(float scale) {
		GlStateManager.glNewList(this.displayList = GLAllocation.generateDisplayLists(1), 4864);
		GlStateManager.translate(this.rotationOffsetX * scale, this.rotationOffsetY * scale, this.rotationOffsetZ * scale);
		GlStateManager.scale(this.scaleX * this.width / this.height, this.scaleY, this.thickness);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		if (this.mirror) {
			GlStateManager.translate(0.0f, 0.0f, -1.0f * scale);
			GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
		}
		renderItemIn2D(Tessellator.getInstance().getBuffer(), this.minU, this.minV, this.maxU, this.maxV, this.width, this.height, scale);
		GL11.glEndList();
		this.isCompiled = true;
	}
	
	@Override
	public void render(float scale) {
		if (!this.showModel || this.isHidden) { return; }
		if (!this.isCompiled) { this.compile(scale); }
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
