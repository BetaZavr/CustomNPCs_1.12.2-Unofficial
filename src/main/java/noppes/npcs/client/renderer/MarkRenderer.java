package noppes.npcs.client.renderer;

import java.nio.FloatBuffer;

import noppes.npcs.LogWriter;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.Model2DRenderer;
import noppes.npcs.controllers.data.MarkData;

public class MarkRenderer {

	public static int[] displayList = new int[] { -1, -1, -1, -1, -1, -1, -1 }; // [0=2D, 1...6=3D]
	public static ResourceLocation markCross = new ResourceLocation(CustomNpcs.MODID, "textures/marks/cross.png");
	public static ResourceLocation markExclamation = new ResourceLocation(CustomNpcs.MODID, "textures/marks/exclamation.png");
	public static ResourceLocation markPointer = new ResourceLocation(CustomNpcs.MODID, "textures/marks/pointer.png");
	public static ResourceLocation markQuestion = new ResourceLocation(CustomNpcs.MODID, "textures/marks/question.png");
	public static ResourceLocation markSkull = new ResourceLocation(CustomNpcs.MODID, "textures/marks/skull.png");
	public static ResourceLocation markStar = new ResourceLocation(CustomNpcs.MODID, "textures/marks/star.png");
	public static boolean needReload = false;
	private static final FloatBuffer COLOR_BUFFER = GLAllocation.createDirectFloatBuffer(4);
	private static final Vec3d LIGHT0_POS = (new Vec3d(0.2D, 1.0D, -0.7D)).normalize();
	private static final Vec3d LIGHT1_POS = (new Vec3d(-0.2D, 1.0D, 0.7D)).normalize();

	public static void render(EntityLivingBase entity, double x, double y, double z, MarkData.Mark mark) {
		try {
			GlStateManager.pushMatrix();
			int color = mark.color;
			float red = (color >> 16 & 0xFF) / 255.0f;
			float green = (color >> 8 & 0xFF) / 255.0f;
			float blue = (color & 0xFF) / 255.0f;
			GlStateManager.color(red, green, blue);
			GlStateManager.translate(x, y + entity.height + 0.6, z);
			GlStateManager.rotate(-entity.rotationYawHead, 0.0f, 1.0f, 0.0f);
			switch (mark.type) {
			case 2:
				Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markExclamation);
				break;
			case 3:
				Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markPointer);
				break;
			case 4:
				Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markSkull);
				break;
			case 5:
				Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markCross);
				break;
			case 6:
				Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markStar);
				break;
			default:
				Minecraft.getMinecraft().getTextureManager().bindTexture(MarkRenderer.markQuestion);
			}
			int list = mark.is3D() ? MarkRenderer.displayList[mark.type] : MarkRenderer.displayList[0];
			if (MarkRenderer.needReload || list < 0) {
				MarkRenderer.needReload = false;
				if (mark.is3D()) {
					String name;
					switch (mark.type) {
					case 2:
						name = "exclamation";
						break;
					case 3:
						name = "pointer";
						break;
					case 4:
						name = "skull";
						break;
					case 5:
						name = "cross";
						break;
					case 6:
						name = "star";
						break;
					default:
						name = "question";
					}
					list = ModelBuffer.getDisplayList(
							new ResourceLocation(CustomNpcs.MODID + ":models/util/" + name + ".obj"), null, null);
					MarkRenderer.displayList[mark.type] = list;
				} else {
					GL11.glNewList(list = GLAllocation.generateDisplayLists(1), 4864);
					GlStateManager.translate(-0.5, 0.0, 0.0);
					Model2DRenderer.renderItemIn2D(Tessellator.getInstance().getBuffer(), 0.0f, 0.0f, 1.0f, 1.0f, 32,
							32, 0.0625f);
					GL11.glEndList();
					MarkRenderer.displayList[0] = list;
					if (mark.is3D()) {
						MarkRenderer.displayList[mark.type] = list;
					}
				}
			}
			if (list >= 0) {
				if (mark.isRotate()) {
					GlStateManager.rotate(entity.world.getTotalWorldTime() % 360 / 0.25f, 0.0f, 1.0f, 0.0f);
				}
				if (mark.is3D() && MarkRenderer.displayList[mark.type] != MarkRenderer.displayList[0]) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_LIGHT0);
					GL11.glEnable(GL11.GL_LIGHT1);
					GL11.glEnable(GL11.GL_COLOR_MATERIAL);
					GlStateManager.colorMaterial(1032, 5634);
					red *= 0.5f;
					green *= 0.5f;
					blue *= 0.5f;
					GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION,
							setColorBuffer((float) LIGHT0_POS.x, (float) LIGHT0_POS.y, (float) LIGHT0_POS.z, 0.0f));
					GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, setColorBuffer(red, green, blue, 1.0F));
					GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
					GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
					GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION,
							setColorBuffer((float) LIGHT1_POS.x, (float) LIGHT1_POS.y, (float) LIGHT1_POS.z, 0.0f));
					GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, setColorBuffer(red, green, blue, 1.0F));
					GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
					GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
					GL11.glShadeModel(GL11.GL_FLAT);
					GlStateManager.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, setColorBuffer(red, green, blue, 1.0F));
				}
				GlStateManager.callList(list);
			}
			GlStateManager.popMatrix();
			//RenderHelper.enableStandardItemLighting();
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	private static FloatBuffer setColorBuffer(float red, float green, float blue, float alpha) {
		COLOR_BUFFER.clear();
		COLOR_BUFFER.put(red).put(green).put(blue).put(alpha);
		COLOR_BUFFER.flip();
		return COLOR_BUFFER;
	}

}
