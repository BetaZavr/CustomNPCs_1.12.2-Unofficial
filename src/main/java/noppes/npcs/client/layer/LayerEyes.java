package noppes.npcs.client.layer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.items.ItemNpcWand;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.animation.GuiNpcEmotion;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.EmotionFrame;
import noppes.npcs.constants.EnumParts;

public class LayerEyes<T extends EntityLivingBase>
extends LayerInterface<T> {

	private float alpha;

    public LayerEyes(RenderLiving<?> render) {
		super(render);
		alpha = 1.0f;
	}

	private void drawBrows(Float[] browRight, Float[] browLeft, Float[] eyeRight, Float[] eyeLeft, int h, int hoverColor, EmotionFrame frame) {
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
		if (h == 1) {
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
		if (h == 2) {
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

	private void drawLeft(float[] data, Float[] eyeData, Float[] pupilData, int h, int hoverColor, boolean isDisableMoved, float[] glintData) {
		int closedType = playerdata.eyes.closed;
		if (closedType == 1 || closedType == 2 || playerdata.eyes.pattern == 1 || npc.isDead || npc.isPlayerSleeping()) { return; }
		float eyePosX = 0.0f;
		float eyePosY = 0.0f;
		float eyeScaleX = 1.0f;
		float eyeScaleY = 1.0f;
		float eyeRotate = 0.0f;
		if (eyeData != null) {
			eyePosX = eyeData[0];
			eyePosY = eyeData[1];
			eyeScaleX = eyeData[2];
			eyeScaleY = eyeData[3];
			eyeRotate = eyeData[4];
		}
		float pupilPosX = data[4];
		float pupilPosY = data[5];
		float pupilScaleX = 1.0f;
		float pupilScaleY = 1.0f;
		float pupilRotate = 0.0f;
		if (pupilData != null) {
			if (!isDisableMoved) {
				pupilPosX = pupilData[0] * -0.005f;
				pupilPosY = pupilData[1] * -0.005f;
				/*if (playerdata.eyes.type != 0) { pupilPosY = pupilData[1] * 0.175f; } // 0.35
				else { pupilPosY = pupilData[1] * 0.075f; } // 0.15
				pupilPosY = pupilData[1] * 0.015f;*/
			}
			pupilScaleX = pupilData[2];
			pupilScaleY = pupilData[3];
			pupilRotate = pupilData[4];
		}
		// Eye
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(0.99 + eyePosX, -5.0 + data[0] + eyePosY, 4.01f);
		if (eyeRotate != 0.0f) {
			float tempPosY = (float) ((1.0 + data[1] - data[0]) / 2.0f);
			GlStateManager.translate(1.01, tempPosY, 0.0f);
			GlStateManager.rotate(eyeRotate, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(-1.01, -tempPosY, 0.0f);
		}
		GlStateManager.scale(eyeScaleX, eyeScaleY, 1.0f);
		if (h == 1) {
			drawRect(-0.125d, -0.125d, 2.135, 1.125 + data[1] - data[0], hoverColor, 0.0D);
		}
		if (playerdata.eyes.type == 2) {
			float red = (float)(playerdata.eyes.eyeColor[1] >> 16 & 255) / 127.5F;
			float green = (float)(playerdata.eyes.eyeColor[1] >> 8 & 255) / 127.5F;
			float blue = (float)(playerdata.eyes.eyeColor[1] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.007875f, 0.0067204f, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.eyeLeft);
			GlStateManager.color(red, green, blue, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else {
			drawRect(0, 0, 2.01, 1.0 + data[1] - data[0], playerdata.eyes.eyeColor[0], 0.0D);
		}

		// Pupil
		if (!playerdata.eyes.activeLeft) {
			pupilPosX = 0.0f;
			pupilPosY = 0.0f;
		}
		if (playerdata.eyes.type == 0) { pupilPosY -= 0.075f; }
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(
				(1.45 + pupilPosX) * (0.3f * pupilScaleX + 0.7f),
				(0.65 + pupilPosY) * (float) (2.0f * Math.pow(pupilScaleY, 3.0d) - 6.2f * Math.pow(pupilScaleY, 2.0d) + 5.2f * pupilScaleY) - data[0],
				0.005f);
		if (pupilRotate != 0.0f) {
			float ry = (data[3] - data[2]) / 2.0f;
			GlStateManager.translate(-0.45, ry, 0.0f);
			GlStateManager.rotate(pupilRotate, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(0.45, -ry, 0.0f);
		}
		GlStateManager.scale(pupilScaleX, pupilScaleY, 1.0f);
		if (h == 2) {drawRect(0.125d, -0.125d, -1.025, 0.125 + data[3], hoverColor, 0.0D); }
		if (playerdata.eyes.type == 2) {
			float red = (float)(playerdata.eyes.pupilColor[1] >> 16 & 255) / 127.5F;
			float green = (float)(playerdata.eyes.pupilColor[1] >> 8 & 255) / 127.5F;
			float blue = (float)(playerdata.eyes.pupilColor[1] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.8f, 0.1f, 0.0f);
			GlStateManager.scale(0.003f, 0.003f, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.pupilLeft);
			GlStateManager.color(red, green, blue, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { drawRect(0, data[2], -0.9, data[3], playerdata.eyes.pupilColor[1], 0.0D); }
		// Glint
		if (playerdata.eyes.glint && npc.isEntityAlive()) {
			GlStateManager.translate(0.0f, 0.0f, 0.001f);
			if (glintData != null) {
				if (glintData[3] != 0.0f) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(-0.8f, 0.1f, 0.0f);
					GlStateManager.translate(glintData[0], glintData[1], 0.0f);
					GlStateManager.scale(0.003f, 0.003f, 1.0f);
					GlStateManager.enableBlend();
					GlStateManager.color(1.0f, 1.0f, 1.0f, glintData[2]);
					render.bindTexture(playerdata.eyes.glintRes);
					drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
					GlStateManager.popMatrix();
				}
			}
			else { drawRect(-0.05, -0.35 + data[6], -0.3, -0.1 + data[7], 0xFFFFFFFF, 0.0D); }
		}
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

	private void drawRight(float[] data, Float[] eyeData, Float[] pupilData, int h, int hoverColor, boolean isDisableMoved, float[] glintData) {
		int cld = playerdata.eyes.closed;
		if (cld == 1 || cld == 3 || playerdata.eyes.pattern == 1 || npc.isDead || npc.isPlayerSleeping()) { return; }
		float eyePosX = 0.0f;
		float eyePosY = 0.0f;
		float eyeScaleX = 1.0f;
		float eyeScaleY = 1.0f;
		float eyeRotate = 0.0f;
		if (eyeData != null) {
			eyePosX = eyeData[0];
			eyePosY = eyeData[1];
			eyeScaleX = eyeData[2];
			eyeScaleY = eyeData[3];
			eyeRotate = eyeData[4];
		}
		float pupilPosX = data[4];
		float pupilPosY = data[5];
		float pupilScaleX = 1.0f;
		float pupilScaleY = 1.0f;
		float pr = 0.0f;
		if (pupilData != null) {
			if (!isDisableMoved) {
				pupilPosX = pupilData[0] * -0.005f; // 0.55
				pupilPosY = pupilData[1] * -0.005f;
				/*if (playerdata.eyes.type != 0) { pupilPosY = pupilData[1] * 0.175f; } // 0.35
				else { pupilPosY = pupilData[1] * 0.075f; } // 0.15
				pupilPosY = pupilData[1] * 0.015f;*/
			}
			pupilScaleX = pupilData[2];
			pupilScaleY = pupilData[3];
			pr = pupilData[4];
		}
		// Eye
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(-0.99 + eyePosX, -5.0 + data[0] + eyePosY, 4.01f);
		if (eyeRotate != 0.0f) {
			float ry = (float) ((1.0 + data[1] - data[0]) / 2.0f);
			GlStateManager.translate(-1.01, ry, 0.0f);
			GlStateManager.rotate(eyeRotate, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(1.01, -ry, 0.0f);
		}
		GlStateManager.scale(eyeScaleX, eyeScaleY, 1.0f);
		if (h == 1) {
			drawRect(0.125d, -0.125d, -2.135, 1.125 + data[1] - data[0], hoverColor, 0.0D);
		}
		if (playerdata.eyes.type == 2) {
			float red = (float)(playerdata.eyes.eyeColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(playerdata.eyes.eyeColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(playerdata.eyes.eyeColor[0] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(-2.01f, 0.0f, 0.0f);
			GlStateManager.scale(0.007875f, 0.0067204f, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.eyeRight);
			GlStateManager.color(red, green, blue, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
			GlStateManager.popMatrix();
		}
		else {
			drawRect(0, 0, -2.01, 1.0 + data[1] - data[0], playerdata.eyes.eyeColor[1], 0.0D);
		}

		// Pupil
		if (!playerdata.eyes.activeRight) {
			pupilPosX = 0.0f;
			pupilPosY = 0.0f;
		}
		if (playerdata.eyes.type == 0) { pupilPosY -= 0.075f; }
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate((-0.56 + pupilPosX) * (-0.7f * pupilScaleX + 1.7f), (0.65 + pupilPosY) * (float) (2.0f * Math.pow(pupilScaleY, 3.0d) - 6.2f * Math.pow(pupilScaleY, 2.0d) + 5.2f * pupilScaleY) - data[0], 0.005f);
		if (pr != 0.0f) {
			float ry = (data[3] - data[2]) / 2.0f;
			GlStateManager.translate(-0.45, ry, 0.0f);
			GlStateManager.rotate(pr, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(0.45, -ry, 0.0f);
		}
		GlStateManager.scale(pupilScaleX, pupilScaleY, 1.0f);
		if (h == 2) {
			drawRect(0.125d, -0.125d, -1.025, 0.125 + data[3], hoverColor, 0.0D);
		}
		if (playerdata.eyes.type == 2) {
			float red = (float)(playerdata.eyes.pupilColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(playerdata.eyes.pupilColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(playerdata.eyes.pupilColor[0] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.8f, 0.1f, 0.0f);
			GlStateManager.scale(0.003f, 0.003f, 1.0f);
			GlStateManager.enableBlend();
			render.bindTexture(playerdata.eyes.pupilRight);
			GlStateManager.color(red, green, blue, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
			GlStateManager.popMatrix();
		}
		else { drawRect(0, data[2], -0.9, data[3], playerdata.eyes.pupilColor[0], 0.0D); }
		// Glint
		if (playerdata.eyes.glint && npc.isEntityAlive()) {
			GlStateManager.translate(0.0f, 0.0f, 0.001f);
			if (glintData != null) {
				if (glintData[3] != 0.0f) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(-0.8f, 0.1f, 0.0f);
					GlStateManager.translate(glintData[0], glintData[1], 0.0f);
					GlStateManager.scale(0.003f, 0.003f, 1.0f);
					GlStateManager.enableBlend();
					render.bindTexture(playerdata.eyes.glintRes);
					GlStateManager.color(1.0f, 1.0f, 1.0f, glintData[2]);
					drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
					GlStateManager.popMatrix();
				}
			}
			else { drawRect(-0.05, -0.35 + data[6], -0.3, -0.1 + data[7], 0xFFFFFFFF, 0.0D); }
		}
		GlStateManager.popMatrix();

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
	public void render(float par2, float par3, float par4, float par5, float par6, float par7) {
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
		GlStateManager.scale(par7, par7, -par7);
		GlStateManager.translate(0.0f, (((playerdata.eyes.type != 0) ? 1 : 2) - playerdata.eyes.eyePos), 0.0f);
		GlStateManager.enableRescaleNormal();
		GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.enableCull();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);

		mc.entityRenderer.setupFogColor(true);
		// [ offsetEyeY, offsetEyeY2, offsetPupilY, offsetPupilY2, pupilPosX, pupilPosY, offsetGlintYStart, offsetGlintYEnd ]
		float[] data = new float[] { 0.0f, 0.0f, -0.4f, 0.25f, 0.0f, 0.0f, 0.0f, 0.0f };
		if (playerdata.eyes.type != 0) { data = new float[] { 0.3f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.7f, 0.15f }; }
		Map<Integer, Float[]> emotionData = new HashMap<>();
		EmotionFrame frame = npc.animation.getCurrentEmotionFrame();
		EmotionConfig activeEmotion = npc.animation.getActiveEmotion();
		boolean isDisableMoved = false;
		if (npc.animation.isEmoted()) { isDisableMoved = npc.animation.emotionIsDisableMoved(); }
		float scaleMoveX = 1.0f;
		float scaleMoveY = 1.0f;
		if (activeEmotion != null) {
			scaleMoveX = Math.max(0.05f, Math.min(1.25f, activeEmotion.scaleMoveX));
			scaleMoveY = Math.max(0.05f, Math.min(1.25f, activeEmotion.scaleMoveY));
		}
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
		if (isDisableMoved) {
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
					data[4] = -0.45f * (float) Math.cos(theta) * scaleMoveX;
					data[5] = -0.45f * (float) Math.sin(theta) * scaleMoveY;
				}
			}
			else if (npc.lookAt != null || (npc.lookPos[0] != -1 && npc.lookPos[1] != -1)) {
				float yaw, pitch;
				if (npc.lookAt != null) {
					double d0 = npc.posX - npc.lookAt.posX;
					double d1 = (npc.posY + (double) npc.getEyeHeight()) - (npc.lookAt.posY + (double) npc.lookAt.getEyeHeight());
					double d2 = npc.posZ - npc.lookAt.posZ;
					double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
					yaw = MathHelper.wrapDegrees(npc.ais.orientation - (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F) / (npc.ais.getStandingType() == 4 ? 2.0f : 1.0f);
					pitch = MathHelper.wrapDegrees(npc.rotationPitch + (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)))) / (npc.ais.getStandingType() == 4 ? 2.0f : 1.0f);
				} else {
					yaw = npc.lookPos[0];
					pitch = npc.lookPos[1];
				}
				if (yaw < -45.0f) { yaw = -45.0f; }
				else if (yaw > 45.0f) { yaw = 45.0f; }
				if (pitch < -45.0f) { pitch = -45.0f; }
				else if (pitch > 45.0f) { pitch = 45.0f; }
				if (playerdata.eyes.type == 2) {
					data[4] = (float) (Math.sin(yaw * Math.PI / 180D) * (-0.006667D * (pitch < 0 ? -pitch : pitch) + 0.8D)) * scaleMoveX;
					data[5] = (float) (Math.sin(pitch * Math.PI / -180D) * (-0.003333D * (yaw < 0 ? -yaw : yaw) + 0.6D)) * scaleMoveY;
				} else {
					data[4] = yaw / 89.0f * scaleMoveX; // 45 -> 0.505
					if (playerdata.eyes.type == 1) { data[5] = pitch / -150.0f * scaleMoveY; } // 45 -> 0.505
					else { data[5] = pitch / -320.0f * scaleMoveY; } // 45 -> 0.505
				}
			}
		}
		float[] glintData = null;
		if (playerdata.eyes.glint && npc.isEntityAlive() && playerdata.eyes.type == 2 && npc.currentAnimation != 2 && npc.world.provider.getDimension() != -1) {
			glintData = new float[] { 0.0f, 0.0f, 1.0f, 0.0f }; // [ x, y, alpha, isShow]
			long time = npc.world.getWorldTime();
			float yaw = npc.rotationYawHead < 0.0f ? npc.rotationYawHead + 360.0f: npc.rotationYawHead;
			boolean show = true;
			if ((time % 12000) < 3000) { // moonrise / sunrise
				yaw -= 270.0f;
				show = Math.abs(yaw) <= 90;
				if (show) {
					if (time > 12000) { glintData[2] = Math.abs(yaw) * -0.00333f + 0.7f; } // night
					else { glintData[2] = Math.abs(yaw) * -0.0001f + 1.0f; } // day
					yaw -= 90.0f;
					glintData[0] = -0.25f * (float) Math.cos(Math.toRadians(yaw));
					glintData[1] = 0.25f * (float) Math.sin(Math.toRadians(yaw));
				}
			}
			else if ((time % 12000) > 9000) { // moonset / sunset
				show = yaw >= 0 && yaw < 180.0f;
				if (show) {
					if (time > 12000) { glintData[2] = Math.abs(yaw - 90.0f) * -0.00333f + 0.7f; } // night
					else { glintData[2] = Math.abs(yaw - 90.0f) * -0.0001f + 1.0f; } // day
					glintData[0] = -0.25f * (float) Math.cos(Math.toRadians(yaw));
					glintData[1] = -0.25f * (float) Math.sin(Math.toRadians(yaw));
				}
			} else { // +/- (midnight / noon)
				yaw = 90.0f + (time % 12000) * -0.03f + 540.0f;
				glintData[0] = -0.25f * (float) Math.cos(Math.toRadians(yaw));
				glintData[1] = -0.25f * (float) Math.sin(Math.toRadians(yaw));
			}
			glintData[3] = show ? 1.0f : 0.0f;
		}
		int e = -1;
		boolean r = true;
		if (mc.currentScreen instanceof GuiNpcEmotion && ((GuiNpcEmotion) mc.currentScreen).npcEmtn.equals(npc)) {
			e = ((GuiNpcEmotion) mc.currentScreen).elementType;
			r = ((GuiNpcEmotion) mc.currentScreen).isRight;
			playerdata.eyes.update(npc);
		}
		int t = (int) (npc.world.getTotalWorldTime() % 40L);
		String a = Integer.toHexString((int) ((-12.25f * t + 255.0f) * (t > 20 ? -1.0f : 1.0f))) + "00FF00";
		int c = (int) Long.parseLong(a, 16);
		int h = 0;


		GlStateManager.disableLighting();
		int i = npc.getBrightnessForRender();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i % 65536.0F, i / 65536.0F);

		if (e < 2 && !r) { h = e + 1; }
		drawLeft(data, emotionData.get(1), emotionData.get(3), h, c, isDisableMoved, glintData);
		h = 0;
		if (e < 2 && r) { h = e + 1; }
		drawRight(data, emotionData.get(0), emotionData.get(2), h, c, isDisableMoved, glintData);
		h = 0;
		if (e == 2) { h = r ? 1 : 2; }
		drawBrows(emotionData.get(4), emotionData.get(5), emotionData.get(0), emotionData.get(1), h, c, frame);

		mc.entityRenderer.setupFogColor(false);
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
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
