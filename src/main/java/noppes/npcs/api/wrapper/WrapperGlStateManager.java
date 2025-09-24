package noppes.npcs.api.wrapper;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.IGlStateManager;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.client.renderer.ModelBuffer;

public class WrapperGlStateManager
implements IGlStateManager {

	private final Minecraft minecraft;
	
	public WrapperGlStateManager(Minecraft mc) { this.minecraft = mc; }

	@Override
	public void enableBlend() { GlStateManager.enableBlend(); }

	@Override
	public void disableBlend() { GlStateManager.disableBlend(); }

	@Override
	public void enableAlpha() { GlStateManager.enableAlpha(); }

	@Override
	public void disableAlpha() { GlStateManager.disableAlpha(); }

	@Override
	public void pushMatrix() { GlStateManager.pushMatrix(); }

	@Override
	public void popMatrix() { GlStateManager.popMatrix(); }

	@Override
	public void color(float red, float green, float blue, float alpha) { GlStateManager.color(red, green, blue, alpha); }

	@Override
	public void translate(float x, float y, float z) { GlStateManager.translate(x, y, z); }

	@Override
	public void scale(float x, float y, float z) { GlStateManager.scale(x, y, z); }

	@Override
	public void rotate(float angle, float axisX, float axisY, float axisZ) { GlStateManager.rotate(angle, axisX, axisY, axisZ); }
	
	@Override
	public void drawString(String text, float x, float y, int color, boolean dropShadow) {
		if (text == null || !text.isEmpty()) { return; }
		this.minecraft.fontRenderer.drawString(text, x, y, color, dropShadow);
	}

	@Override
	public void draw(double left, double top, double width, double height, int color, float alpha) {
		this.draw(left, top, width, height, (float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color & 255) / 255.0F, alpha);
	}
	
	@Override
	public void draw(double left, double top, double width, double height, float red, float green, float blue, float alpha) {
		if (alpha <= 0.0f) { return; } else if (alpha > 1.0f) { alpha = 1.0f; }
		if (red < 0.0f) { red = 0.0f; } else if (red > 1.0f) { red = 1.0f; }
		if (green < 0.0f) { green = 0.0f; } else if (green > 1.0f) { green = 1.0f; }
		if (blue < 0.0f) { blue = 0.0f; } else if (blue > 1.0f) { blue = 1.0f; }
		
		double right = left + width;
		double bottom = top + height;
		
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, alpha);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
	}
	
	@Override
	public void drawTexture(String resourceLocation, double x, double y, double z, double u, double v, double width, double height, boolean revers) {
		if (resourceLocation == null || resourceLocation.isEmpty()) { return; }
		ResourceLocation loc = new ResourceLocation(resourceLocation);
		minecraft.getTextureManager().bindTexture(loc);
        minecraft.getTextureManager().getTexture(loc);
        float w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		float h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		if (w > 256.0f) {
			w = 256.0f;
			width *= 256.0f / w;
		}
		if (h > 256.0f) {
			h = 256.0f;
			height *= 256.0f / h;
		}
		float f = 1.0f / w;
		float f1 = 1.0f / h;
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		double us = (revers ? u + width : u) * f;
		double ue = (revers ? u : u + width) * f;
		bufferbuilder.pos(x, y + height, z).tex(us, (v + height) * f1).endVertex();
		bufferbuilder.pos(x + width, y + height, z).tex(ue, (v + height) * f1).endVertex();
		bufferbuilder.pos(x + width, y, z).tex(ue, v * f1).endVertex();
		bufferbuilder.pos(x, y, z).tex(us, v * f1).endVertex();
		tessellator.draw();
	}

	@Override
	public void renderEntity(Object entity, double x, double y, double z, float yaw, float partialTicks, boolean disableDebugBoundingBox) {
		if (entity instanceof Entity) {
			minecraft.getRenderManager().renderEntity((Entity) entity, x, y, z, yaw, partialTicks, disableDebugBoundingBox);
		}
		else if (entity instanceof IEntity<?>) {
			minecraft.getRenderManager().renderEntity(((IEntity<?>) entity).getMCEntity(), x, y, z, yaw, partialTicks, disableDebugBoundingBox);
		}
	}
	
	@Override
	public void drawOBJ(String resourceLocation) {
		if (resourceLocation == null || !resourceLocation.isEmpty()) { return; }
		int displayList = ModelBuffer.getDisplayList(new ResourceLocation(resourceLocation), null, null);
		if (displayList > 0) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GlStateManager.callList(displayList);
		}
	}

}
