package noppes.npcs.client.layer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.items.ItemNpcWand;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.animation.GuiNpcEmotion;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.EmotionFrame;
import noppes.npcs.constants.EnumParts;

public class LayerEyes<T extends EntityLivingBase>
extends LayerInterface<T> {

	private float alpha;
	private final EyeRenderData leftData = new EyeRenderData(true);
	private final EyeRenderData rightData = new EyeRenderData(false);

    public LayerEyes(RenderLiving<?> render) {
		super(render);
		alpha = 1.0f;
	}

	private void drawBrows(Float[] browRight, Float[] browLeft, Float[] eyeRight, Float[] eyeLeft, int selectType, int hoverColor, EmotionFrame frame) {
		float oYl = 0.0f, oYr = 0.0f;
		int cld = playerdata.eyes.closed;
		if (npc.getHealth() <= 0.0f || npc.isPlayerSleeping()) { cld = 1; }
		float oUp = 0.0f;
		if (playerdata.eyes.type == 1) { oUp = 0.3f; }

		float offsetBrowLeftX = 0.0f;
		float offsetBrowLeftY = 0.0f;
		float offsetBrowRightX = 0.0f;
		float offsetBrowRightY = 0.0f;
		float offsetEyeLeftX = 0.0f;
		float offsetEyeLeftY = 0.0f;
		float offsetEyeRightX = 0.0f;
		float offsetEyeRightY = 0.0f;

		float scaleBrowLeftX = 1.0f;
		float scaleBrowLeftY = 1.0f;
		float scaleBrowRightX = 1.0f;
		float scaleBrowRightY = 1.0f;
		float scaleEyeLeftX = 1.0f;
		float scaleEyeLeftY = 1.0f;
		float scaleEyeRightX = 1.0f;
		float scaleEyeRightY = 1.0f;

		float rotateBrowLeft = 0.0f;
		float rotateBrowRight = 0.0f;
		float rotateEyeLeft = 0.0f;
		float rotateEyeRight = 0.0f;

		if (browRight != null) {
			offsetBrowRightX = browRight[0];
			offsetBrowRightY = browRight[1];
			scaleBrowRightX = browRight[2];
			scaleBrowRightY = browRight[3];
			rotateBrowRight = browRight[4];
		}
		if (browLeft != null) {
			offsetBrowLeftX = browLeft[0];
			offsetBrowLeftY = browLeft[1];
			scaleBrowLeftX = browLeft[2];
			scaleBrowLeftY = browLeft[3];
			rotateBrowLeft = browLeft[4];
		}
		if (eyeRight != null) {
			offsetEyeRightX = eyeRight[0];
			offsetEyeRightY = eyeRight[1];
			scaleEyeRightX = eyeRight[2];
			scaleEyeRightY = eyeRight[3];
			rotateEyeRight = eyeRight[4];
		}
		if (eyeLeft != null) {
			offsetEyeLeftX = eyeLeft[0];
			offsetEyeLeftY = eyeLeft[1];
			scaleEyeLeftX = eyeLeft[2];
			scaleEyeLeftY = eyeLeft[3];
			rotateEyeLeft = eyeLeft[4];
		}

		// skin close
		boolean close = false;
		if (frame != null) {
			close = playerdata.eyes.ticks > 3 && frame.isEndBlink();
		}
		if (cld != 0 || close) {
			if (cld == 1 || close || cld == 2) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(0.99 + offsetEyeLeftX, -5.0 + oUp + offsetEyeLeftY, 4.02f);
				if (rotateEyeLeft != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(1.01, ry, 0.0f);
					GlStateManager.rotate(rotateEyeLeft, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(-1.01, -ry, 0.0f);
				}
				GlStateManager.scale(scaleEyeLeftX, scaleEyeLeftY, 1.0f);
				drawRect(0, 0, 2.02, (playerdata.eyes.type != 0) ? 1.7f : 1, playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
			if (cld == 1 || close ||  cld == 3) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(-0.99 + offsetEyeRightX, -5.0 + oUp + offsetEyeRightY, 4.02f);
				if (rotateEyeRight != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(-1.01, ry, 0.0f);
					GlStateManager.rotate(rotateEyeRight, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(1.01, -ry, 0.0f);
				}
				GlStateManager.scale(scaleEyeRightX, scaleEyeRightY, 1.0f);
				drawRect(0, 0, -2.02, (playerdata.eyes.type != 0) ? 1.7f : 1, playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
		}

		// skin blink
		if (cld != 1 && playerdata.eyes.blinkStart > 0L && npc.isEntityAlive() && npc.deathTime == 0) {
			float f = (System.currentTimeMillis() - playerdata.eyes.blinkStart) / 150.0f;
			if (f > 1.0f) { f = 2.0f - f; }
			if (f < 0.0f) {
				playerdata.eyes.blinkStart = 0L;
				f = 0.0f;
			}
			if (cld == 0 || cld == 2) {
				oYl = ((playerdata.eyes.type != 0) ? 2.0f : 1.0f) * f;
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(-0.99 + offsetEyeRightX, -5.0 + oUp + offsetEyeRightY, 4.02f);
				if (rotateEyeRight != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(-1.01, ry, 0.0f);
					GlStateManager.rotate(rotateEyeRight, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(1.01, -ry, 0.0f);
				}
				GlStateManager.scale(scaleEyeRightX, scaleEyeRightY, 1.0f);
				drawRect(0, 0, -2.02, oYl, playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
			if (cld == 0 || cld == 3) {
				oYr = ((playerdata.eyes.type != 0) ? 2 : 1) * f;
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(0.99 + offsetEyeLeftX, -5.0 + oUp + offsetEyeLeftY, 4.02f);
				if (rotateEyeLeft != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(1.01, ry, 0.0f);
					GlStateManager.rotate(rotateEyeLeft, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(-1.01, -ry, 0.0f);
				}
				GlStateManager.scale(scaleEyeLeftX, scaleEyeLeftY, 1.0f);
				drawRect(0, 0, 2.02, oYr, playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
		}

		// brow
		if (playerdata.eyes.browThickness == 0) { return; }

		float thickness = playerdata.eyes.browThickness / 10.0f;
		oYl *= 0.075f;
		oYr *= 0.075f;
		if (playerdata.eyes.type == 0 ) { oYl -= 0.35f; oYr -= 0.35f; }

		// Right
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(-0.99 + offsetBrowRightX, -4.8 + oYl + offsetBrowRightY, 4.01f);
		if (rotateBrowRight != 0.0f) {
			float ry = thickness / -2.0f;
			GlStateManager.translate(-1.01, ry, 0.0f);
			GlStateManager.rotate(rotateBrowRight, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(1.01, -ry, 0.0f);
		}
		GlStateManager.scale(scaleBrowRightX, scaleBrowRightY, 1.0f);
		if (selectType == 1) {
			drawRect(0.125d, 0.125d, -2.145, -0.125 -thickness, hoverColor, 0.0D);
		}
		if (playerdata.eyes.type == 2) {
			float sH = (float) (playerdata.eyes.browThickness - 1) * 0.166667f + 0.333333f;
			float red = (float)(playerdata.eyes.browColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(playerdata.eyes.browColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(playerdata.eyes.browColor[0] & 255) / 127.5F;
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-2.2f, -0.1f - thickness, 0.005f);
			GlStateManager.scale(0.009, 0.0035f * sH, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.browRight);
			GlStateManager.color(red, green, blue, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { drawRect(0, 0, -2.02, -thickness, playerdata.eyes.browColor[0], 0.0D); }
		GlStateManager.popMatrix();

		// Left
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(0.99 + offsetBrowLeftX, -4.8 + oYr + offsetBrowLeftY, 4.01f);
		if (rotateBrowLeft != 0.0f) {
			float ry = thickness / -2.0f;
			GlStateManager.translate(1.01, ry, 0.0f);
			GlStateManager.rotate(rotateBrowLeft, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(-1.01, -ry, 0.0f);
		}
		GlStateManager.scale(scaleBrowLeftX, scaleBrowLeftY, 1.0f);
		if (selectType == 2) {
			drawRect(-0.125d, 0.125d, 2.145, -0.125 -thickness, hoverColor, 0.0D);
		}
		if (playerdata.eyes.type == 2) {
			float sH = (float) (playerdata.eyes.browThickness - 1) * 0.166667f + 0.333333f;
			float red = (float)(playerdata.eyes.browColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(playerdata.eyes.browColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(playerdata.eyes.browColor[0] & 255) / 127.5F;
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.1f, -0.1f - thickness, 0.005f);
			GlStateManager.scale(0.009, 0.0035f * sH, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.browRight);
			GlStateManager.color(red, green, blue, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
			GlStateManager.popMatrix();
		}
		else { drawRect(0, 0, 2.02, -thickness, playerdata.eyes.browColor[1], 0.0D); }
		GlStateManager.popMatrix();
	}

	private void drawLeft(int selectType, int hoverColor) {
		if (!leftData.isShow) { return; }
		// Eye
		GlStateManager.pushMatrix();
		GlStateManager.translate(leftData.eyePosX, leftData.eyePosY, 4.001f);
		GlStateManager.scale(leftData.eyeScaleX, leftData.eyeScaleY, 1.0f);
		if (selectType == 1) { drawRect(-0.125d, -0.125d, 2.135, 1.125 + leftData.eyeHoverY, hoverColor, 0.0D); }
		if (playerdata.eyes.type == 2) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.007875f, 0.0067204f, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.eyeLeft);
			GlStateManager.color(leftData.eyeColor[0], leftData.eyeColor[1], leftData.eyeColor[2], 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { drawRect(0, 0, leftData.eyeWeight, leftData.eyeHeight, playerdata.eyes.eyeColor[0], 0.0D); }

		// Pupil
		GlStateManager.pushMatrix();
		GlStateManager.translate(leftData.pupilX, leftData.pupilY, 0.0f);
		GlStateManager.scale(leftData.pupilScaleX, leftData.pupilScaleY, 1.0f);
		if (selectType == 2) { drawRect(-0.625, -0.625, 0.625, 0.625, hoverColor, 0.0D); }
		if (playerdata.eyes.type == 2) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.8f, 0.1f, 0.0f);
			GlStateManager.scale(0.003f, 0.003f, 1.0f);
			render.bindTexture(playerdata.eyes.pupilLeft);
			GlStateManager.color(leftData.pupilColor[0], leftData.pupilColor[1], leftData.pupilColor[2], 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { drawRect(leftData.pupilLeft, leftData.pupilTop, leftData.pupilRight, leftData.pupilBottom, playerdata.eyes.pupilColor[1], 0.0D); }

		// Glint
		if (playerdata.eyes.glint && npc.isEntityAlive()) {
			GlStateManager.translate(0.0f, 0.0f, 0.001f);
			if (leftData.glintShow) {
				if (playerdata.eyes.type == 2) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(-0.8f, 0.1f, 0.0f);
					GlStateManager.translate(leftData.glintLeft, leftData.glintTop, 0.0f);
					GlStateManager.scale(0.003f, 0.003f, 1.0f);
					GlStateManager.color(1.0f, 1.0f, 1.0f, leftData.glintAlpha);
					render.bindTexture(playerdata.eyes.glintRes);
					drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
					GlStateManager.popMatrix();
				}
				else {
					drawRect(leftData.glintLeft, leftData.glintTop, leftData.glintRight, leftData.glintBottom, leftData.glintColor, 0.0D);
				}
			}
		}
		GlStateManager.popMatrix(); // Pupil end

		GlStateManager.popMatrix();
	}

	private void drawRight(int selectType, int hoverColor) {
		if (!rightData.isShow) { return; }
		// Eye
		GlStateManager.pushMatrix();
		GlStateManager.translate(rightData.eyePosX, rightData.eyePosY, 4.001f);
		GlStateManager.scale(rightData.eyeScaleX, rightData.eyeScaleY, 1.0f);
		if (selectType == 1) { drawRect(-0.125d, -0.125d, 2.135, 1.125 + rightData.eyeHoverY, hoverColor, 0.0D); }
		if (playerdata.eyes.type == 2) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.007875f, 0.0067204f, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.eyeLeft);
			GlStateManager.color(rightData.eyeColor[0], rightData.eyeColor[1], rightData.eyeColor[2], 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { drawRect(0, 0, rightData.eyeWeight, rightData.eyeHeight, playerdata.eyes.eyeColor[0], 0.0D); }

		// Pupil
		GlStateManager.pushMatrix();
		GlStateManager.translate(rightData.pupilX, rightData.pupilY, 0.0f);
		GlStateManager.scale(rightData.pupilScaleX, rightData.pupilScaleY, 1.0f);
		if (selectType == 2) { drawRect(-0.625, -0.625, 0.625, 0.625, hoverColor, 0.0D); }
		if (playerdata.eyes.type == 2) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.8f, 0.1f, 0.0f);
			GlStateManager.scale(0.003f, 0.003f, 1.0f);
			render.bindTexture(playerdata.eyes.pupilLeft);
			GlStateManager.color(rightData.pupilColor[0], rightData.pupilColor[1], rightData.pupilColor[2], 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { drawRect(rightData.pupilLeft, rightData.pupilTop, rightData.pupilRight, rightData.pupilBottom, playerdata.eyes.pupilColor[1], 0.0D); }

		// Glint
		if (playerdata.eyes.glint && npc.isEntityAlive()) {
			GlStateManager.translate(0.0f, 0.0f, 0.001f);
			if (rightData.glintShow) {
				if (playerdata.eyes.type == 2) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(-0.8f, 0.1f, 0.0f);
					GlStateManager.translate(rightData.glintLeft, rightData.glintTop, 0.0f);
					GlStateManager.scale(0.003f, 0.003f, 1.0f);
					GlStateManager.color(1.0f, 1.0f, 1.0f, rightData.glintAlpha);
					render.bindTexture(playerdata.eyes.glintRes);
					drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
					GlStateManager.popMatrix();
				}
				else {
					drawRect(rightData.glintLeft, rightData.glintTop, rightData.glintRight, rightData.glintBottom, rightData.glintColor, 0.0D);
				}
			}
		}
		GlStateManager.popMatrix(); // Pupil end

		GlStateManager.popMatrix();
	}

	public void drawRect(double x, double y, double x2, double y2, int color, double z) {
		if (x < x2) {
			double j1 = x;
			x = x2;
			x2 = j1;
		}
		if (y < y2) {
			double j1 = y;
			y = y2;
			y2 = j1;
		}
		float f0 = (color >> 24 & 0xFF) / 255.0f;
		if (f0 == 0.0f) { f0 = alpha; }
		float f1 = (color >> 16 & 0xFF) / 255.0f;
		float f2 = (color >> 8 & 0xFF) / 255.0f;
		float f3 = (color & 0xFF) / 255.0f;
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x, y, z).color(f1, f2, f3, f0).endVertex();
		buffer.pos(x, y2, z).color(f1, f2, f3, f0).endVertex();
		buffer.pos(x2, y2, z).color(f1, f2, f3, f0).endVertex();
		buffer.pos(x2, y, z).color(f1, f2, f3, f0).endVertex();
		Tessellator.getInstance().draw();
	}

	@Override
	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!playerdata.eyes.isEnabled() || npc.display.getModel() != null || !npc.getClass().getSimpleName().equals("EntityCustomNpc") || !npc.animation.showParts.get(EnumParts.HEAD)) { return; }
		boolean isInvisible = false;
        Minecraft mc = Minecraft.getMinecraft();
		if (npc.display.getVisible() == 1) { isInvisible = npc.display.getAvailability().isAvailable(mc.player); }
		else if (npc.display.getVisible() == 2) { isInvisible = !(mc.player.getHeldItemMainhand().getItem() instanceof ItemNpcWand); }
		if (isInvisible) { alpha = 0.5f; }
		else if (npc.equals(ModelNpcAlt.editAnimDataSelect.displayNpc) && ModelNpcAlt.editAnimDataSelect.part != 0) {
			if (ModelNpcAlt.editAnimDataSelect.alpha >= 1.0f) { alpha = 1.0f; } else { alpha = 0.5f; }
		}
		else { alpha = 1.0f; }

		GlStateManager.pushMatrix();
		model.bipedHead.postRender(0.0625f);
		if (npc.isSneaking()) { GlStateManager.translate(0.0F, -0.2F, 0.0F); }
		GlStateManager.scale(scale, scale, -scale);
		GlStateManager.translate(0.0f, (((playerdata.eyes.type != 0) ? 1 : 2) - playerdata.eyes.eyePos), 0.0f);
		GlStateManager.enableRescaleNormal(); // Normalization of vertex normals
		GlStateManager.shadeModel(GL11.GL_SMOOTH); // Smooth Shading Mode
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate( // Setting up blending functions
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.enableCull(); // Enable invisible edge culling
		GlStateManager.depthMask(false); // Disable recording of depth values

		mc.entityRenderer.setupFogColor(true);

		// [ offsetEyeY, offsetEyeY2, offsetPupilY, offsetPupilY2, pupilPosX, pupilPosY, offsetGlintYStart, offsetGlintYEnd ]
		float[] data;
		if (playerdata.eyes.type != 0) { data = new float[] { 0.3f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.7f, 0.15f }; }
		else { data = new float[]{0.0f, 0.0f, -0.4f, 0.25f, 0.0f, 0.0f, 0.0f, 0.0f}; }

        Map<Integer, Float[]> emotionData = new HashMap<>();
		EmotionFrame frame = npc.animation.getCurrentEmotionFrame();
		EmotionConfig activeEmotion = npc.animation.getActiveEmotion();
		boolean isDisableMoved = npc.animation.isEmoted() && npc.animation.emotionIsDisableMoved();
		if (CustomNpcs.ShowCustomAnimation && activeEmotion != null) {
			emotionData = npc.animation.getEmotionData();
			if (frame != null && frame.isBlink()) {
				playerdata.eyes.ticks = (int) (npc.world.getTotalWorldTime() - npc.animation.getStartEmotionTime());
				if (playerdata.eyes.ticks == 0) { playerdata.eyes.blinkStart = System.currentTimeMillis(); }
			}
			else if (!activeEmotion.canBlink()) {
				playerdata.eyes.blinkStart = -20L;
			}
		}

		if (!isDisableMoved) {
			if (mc.currentScreen instanceof GuiNpcEmotion && activeEmotion != null && activeEmotion.isEdit && (activeEmotion.editFrame > -1 || activeEmotion.frames.size() == 1)) {
				EmotionFrame fr = npc.animation.getEmotionCurrentFrame();
				if (fr != null && fr.disable) {
					ScaledResolution sw = new ScaledResolution(mc);
					float x = sw.getScaledWidth() / 2.0f + 134.0f;
					float y = sw.getScaledHeight() / 2.0f + 41.0f;
					GuiNPCInterface gui = (GuiNPCInterface) mc.currentScreen;
					x -= gui.mouseX;
					y -= gui.mouseY;
					x = ValueUtil.correctFloat(x, -70.0f, 70.0f);
					y = ValueUtil.correctFloat(y, -70.0f, 70.0f);
					double theta = Math.atan2(y, x);
					data[4] = -0.45f * (float) Math.cos(theta);
					data[5] = -0.45f * (float) Math.sin(theta);
				}
			}
			else if (npc.lookAt != null || npc.lookPos[0] != 0 || npc.lookPos[1] != 0) {
				if (npc.lookAt != null) {
					float d0 = npc.rotationYawHead % 360.0f;
					// Yaw
					if (d0 < 0.0f) { d0 += 360.0f; }
					if (npc.ais.getStandingType() == 4 || npc.ais.getStandingType() == 2) {
						d0 = d0 - npc.ais.orientation;
						if (d0 > 180.0f) { d0 -= 360.0f; }
						if (Math.abs(d0) >= 45.0f) {
							if (npc.ais.getStandingType() == 4) {
								if (d0 < 0.0f) { d0 += 45.0f; }
								else if (d0 > 0.0f) { d0 -= 45.0f; }
								data[4] = ValueUtil.correctFloat(d0, -45.0f, 45.0f) / -45.0f;
							}
						}
					}
					// Pitch
					IRayTraceRotate rayTrace = Util.instance.getAngles3D(npc, npc.lookAt);
					if (Math.abs(rayTrace.getPitch()) >= 45.0f) {
						float d1 = npc.rotationPitch + (float) rayTrace.getPitch();
						data[5] = ValueUtil.correctFloat(d1, -45.0f, 45.0f) / 45.0f;
					}
				}
				else {
					// 45 -> 0.5
					data[4] = ValueUtil.correctFloat(npc.lookPos[0], -45.0f, 45.0f) / 45.0f;
					data[5] = ValueUtil.correctFloat(npc.lookPos[1], -45.0f, 45.0f) / -45.0f;
				}
			}
		}
		int elementType = -1;
		boolean isRight = true;
		if (mc.currentScreen instanceof GuiNpcEmotion && ((GuiNpcEmotion) mc.currentScreen).npcEmtn.equals(npc)) {
			elementType = ((GuiNpcEmotion) mc.currentScreen).elementType;
			isRight = ((GuiNpcEmotion) mc.currentScreen).isRight;
			playerdata.eyes.update(npc);
		}
		int t = (int) (npc.world.getTotalWorldTime() % 40L);
		int hoverColor = 0xFF00 | (int) Math.ceil((-12.25f * t + 255.0f) * (t > 20 ? -1.0f : 1.0f)) << 24;
		int selectType = 0;

		GlStateManager.disableLighting();
		int i = npc.getBrightnessForRender();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i % 65536.0F, i / 65536.0F);

		if (elementType < 2 && !isRight) { selectType = elementType + 1; }
		Map<Integer, Float[]> finalEmotionData = emotionData;
		CustomNPCsScheduler.runTack(() -> leftData.update(npc, playerdata.eyes, 2, data, finalEmotionData.get(1), finalEmotionData.get(3), isDisableMoved));
		drawLeft(selectType, hoverColor);
		selectType = 0;
		if (elementType < 2 && isRight) { selectType = elementType + 1; }
		CustomNPCsScheduler.runTack(() -> rightData.update(npc, playerdata.eyes, 2, data, finalEmotionData.get(0), finalEmotionData.get(2), isDisableMoved));
		drawRight(selectType, hoverColor);
		selectType = 0;
		if (elementType == 2) { selectType = isRight ? 1 : 2; }
		drawBrows(emotionData.get(4), emotionData.get(5), emotionData.get(0), emotionData.get(1), selectType, hoverColor, frame);

		mc.entityRenderer.setupFogColor(false);
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		GlStateManager.enableTexture2D();
	}

	@Override
	public void rotate(float par1, float par2, float par3, float par4, float par5, float par6) { }

	public void drawTexturedModalRect(double x, double y, double z, int textureX, int textureY, int width, int height, boolean rev) {
		float f = 0.00390625F;
		GlStateManager.enableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		if (rev) {
			bufferbuilder.pos(x, y + height, z).tex((textureX + width) * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y + height, z).tex(textureX * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y, z).tex(textureX * f, textureY * f).endVertex();
			bufferbuilder.pos(x, y, z).tex((textureX + width) * f, textureY * f).endVertex();
		} else {
			bufferbuilder.pos(x, y + height, z).tex((textureX) * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y + height, z).tex((textureX + width) * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y, z).tex((textureX + width) * f, textureY * f).endVertex();
			bufferbuilder.pos(x, y, z).tex(textureX * f, textureY* f).endVertex();
		}
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
	}

}
