package noppes.npcs.client.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.Model2DRenderer;
import noppes.npcs.controllers.data.MarkData;

public class MarkRenderer {
	
	public static int displayList = -1;
	public static ResourceLocation markCross = new ResourceLocation(CustomNpcs.MODID, "textures/marks/cross.png");
	public static ResourceLocation markExclamation = new ResourceLocation(CustomNpcs.MODID,"textures/marks/exclamation.png");
	public static ResourceLocation markPointer = new ResourceLocation(CustomNpcs.MODID, "textures/marks/pointer.png");
	public static ResourceLocation markQuestion = new ResourceLocation(CustomNpcs.MODID, "textures/marks/question.png");
	public static ResourceLocation markSkull = new ResourceLocation(CustomNpcs.MODID, "textures/marks/skull.png");
	public static ResourceLocation markStar = new ResourceLocation(CustomNpcs.MODID, "textures/marks/star.png");

	public static void render(EntityLivingBase entity, double x, double y, double z, MarkData.Mark mark) {
		GlStateManager.pushMatrix();
		int color = mark.color;
		float red = (color >> 16 & 0xFF) / 255.0f;
		float blue = (color >> 8 & 0xFF) / 255.0f;
		float green = (color & 0xFF) / 255.0f;
		GlStateManager.color(red, blue, green);
		GlStateManager.translate(x, y + entity.height + 0.6, z);
		GlStateManager.rotate(-entity.rotationYawHead, 0.0f, 1.0f, 0.0f);
		if (mark.type == 2) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markExclamation);
		} else if (mark.type == 1) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markQuestion);
		} else if (mark.type == 3) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markPointer);
		} else if (mark.type == 5) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markCross);
		} else if (mark.type == 4) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markSkull);
		} else if (mark.type == 6) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markStar);
		}
		if (MarkRenderer.displayList >= 0) {
			if (mark.isRotate()) { // New
				GlStateManager.rotate(entity.world.getTotalWorldTime() % 360 / 0.25f, 0.0f, 1.0f, 0.0f);
			}
			GlStateManager.callList(MarkRenderer.displayList);
		} else {
			GL11.glNewList(MarkRenderer.displayList = GLAllocation.generateDisplayLists(1), 4864);
			GlStateManager.translate(-0.5, 0.0, 0.0);
			Model2DRenderer.renderItemIn2D(Tessellator.getInstance().getBuffer(), 0.0f, 0.0f, 1.0f, 1.0f, 32, 32, 0.0625f);
			GL11.glEndList();
		}
		GlStateManager.popMatrix();
	}

}
